package com.devsuj.crmapi.service;

import com.devsuj.crmapi.dto.PaginaResponse;
import com.devsuj.crmapi.dto.venta.CalculoVentaResponse;
import com.devsuj.crmapi.dto.venta.DetalleVentaRequest;
import com.devsuj.crmapi.dto.venta.VentaRequest;
import com.devsuj.crmapi.dto.venta.VentaResponse;
import com.devsuj.crmapi.dto.venta.VentaResumenResponse;
import com.devsuj.crmapi.exception.DatosInvalidosException;
import com.devsuj.crmapi.exception.RecursoNoEncontradoException;
import com.devsuj.crmapi.mapper.VentaMapper;
import com.devsuj.crmapi.model.Cliente;
import com.devsuj.crmapi.model.EstadoCliente;
import com.devsuj.crmapi.model.Oportunidad;
import com.devsuj.crmapi.model.Promocion;
import com.devsuj.crmapi.model.TipoMovimientoInventario;
import com.devsuj.crmapi.model.Venta;
import com.devsuj.crmapi.repository.Filtros;
import com.devsuj.crmapi.repository.MovimientoPuntosRepository;
import com.devsuj.crmapi.repository.PromocionRepository;
import com.devsuj.crmapi.repository.VentaRepository;
import com.devsuj.crmapi.security.Alcance;
import com.devsuj.crmapi.security.ControlAcceso;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Ventas concretadas (P11–P13): una venta solo existe si se pagó, no se edita ni se cancela.
 * Al registrarla, en una sola transacción: descuenta stock (kardex VENTA), gana la oportunidad ligada,
 * activa al cliente PROSPECTO y publica {@link VentaPagadaEvent} (puntos de fidelización).
 */
@Service
@RequiredArgsConstructor
public class VentaService {

    private static final String RECURSO = "esta venta";

    private final VentaRepository repository;
    private final PromocionRepository promocionRepository;
    private final MovimientoPuntosRepository puntosRepository;
    private final ClienteService clienteService;
    private final UsuarioService usuarioService;
    private final ProductoService productoService;
    private final OportunidadService oportunidadService;
    private final InventarioService inventarioService;
    private final CalculadoraVenta calculadora;
    private final VentaMapper mapper;
    private final ControlAcceso controlAcceso;
    private final ApplicationEventPublisher eventos;
    private final Clock clock;

    /** {@code desde} y {@code hasta} son días completos (ambos incluidos). */
    @Transactional(readOnly = true)
    public PaginaResponse<VentaResumenResponse> listar(Integer clienteId, Integer usuarioId, LocalDate desde,
            LocalDate hasta, Pageable pageable) {
        Periodo periodo = new Periodo(desde, hasta);
        Alcance alcance = controlAcceso.alcance();
        controlAcceso.verificarFiltroUsuario(alcance, usuarioId);
        Specification<Venta> filtro = Specification.allOf(
                Filtros.visiblePara(alcance, "usuario"),
                Filtros.igual(clienteId, "cliente", "id"),
                Filtros.igual(usuarioId, "usuario", "id"),
                Filtros.desde(periodo.inicio(), "fecha"),
                Filtros.antesDe(periodo.fin(), "fecha"));
        return PaginaResponse.de(repository.findAll(filtro, pageable), mapper::toResumen);
    }

    @Transactional(readOnly = true)
    public PaginaResponse<VentaResumenResponse> delCliente(Integer clienteId, Pageable pageable) {
        clienteService.buscar(clienteId);
        return listar(clienteId, null, null, null, pageable);
    }

    @Transactional(readOnly = true)
    public VentaResponse obtener(Integer id) {
        Venta venta = repository.findConDetallesById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("una venta", id));
        controlAcceso.verificarLectura(venta.getUsuario(), venta.getCliente(), RECURSO);
        Integer puntos = puntosRepository.findByVentaId(id).map(m -> m.getPuntos()).orElse(0);
        return mapper.toResponse(venta, puntos);
    }

    /** Simula importes y promociones de hoy sin guardar nada. */
    @Transactional(readOnly = true)
    public CalculoVentaResponse calcular(List<DetalleVentaRequest> detalles) {
        return mapper.toResponse(calcularHoy(detalles));
    }

    @Transactional
    public VentaResponse registrar(VentaRequest request) {
        Cliente cliente = clienteService.buscarOperable(request.clienteId(), "registrar ventas");
        Oportunidad oportunidad = request.oportunidadId() == null
                ? null
                : oportunidadService.buscarParaVenta(request.oportunidadId(), cliente);
        CalculoVenta calculo = calcularHoy(request.detalles());

        Venta venta = new Venta();
        venta.setCliente(cliente);
        venta.setUsuario(usuarioService.responsableParaRegistro(request.usuarioId()));
        venta.setOportunidad(oportunidad);
        venta.setFecha(LocalDateTime.now(clock));
        venta.setSubtotal(calculo.subtotal());
        venta.setDescuento(calculo.descuento());
        venta.setPromocion(calculo.promocion());
        venta.setTotal(calculo.total());
        calculo.renglones().forEach(r -> venta.agregarDetalle(mapper.toDetalle(r)));
        repository.save(venta);

        calculo.renglones().forEach(r -> inventarioService.salida(r.producto(), r.cantidad(),
                TipoMovimientoInventario.VENTA, "Venta #" + venta.getId(), venta));
        if (oportunidad != null) {
            oportunidadService.marcarGanada(oportunidad, venta.getId());
        }
        if (cliente.getEstado() == EstadoCliente.PROSPECTO) {
            cliente.setEstado(EstadoCliente.ACTIVO);
        }
        eventos.publishEvent(new VentaPagadaEvent(venta));
        Integer puntos = puntosRepository.findByVentaId(venta.getId()).map(m -> m.getPuntos()).orElse(0);
        return mapper.toResponse(venta, puntos);
    }

    private CalculoVenta calcularHoy(List<DetalleVentaRequest> detalles) {
        verificarSinRepetidos(detalles);
        List<CalculoVenta.Pedido> pedidos = detalles.stream()
                .map(d -> new CalculoVenta.Pedido(productoService.buscarVendible(d.productoId()), d.cantidad()))
                .toList();
        return calculadora.calcular(pedidos, promocionesVigentes(LocalDate.now(clock)));
    }

    private List<Promocion> promocionesVigentes(LocalDate hoy) {
        return promocionRepository.findActivasEn(hoy).stream().filter(p -> p.estaVigente(hoy)).toList();
    }

    private static void verificarSinRepetidos(List<DetalleVentaRequest> detalles) {
        Set<Integer> vistos = new HashSet<>();
        for (DetalleVentaRequest detalle : detalles) {
            if (!vistos.add(detalle.productoId())) {
                throw new DatosInvalidosException("El producto " + detalle.productoId()
                        + " está repetido: junta las cantidades en un solo renglón.");
            }
        }
    }
}
