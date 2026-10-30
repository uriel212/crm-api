package com.devsuj.crmapi.service;

import com.devsuj.crmapi.dto.PaginaResponse;
import com.devsuj.crmapi.dto.postventa.ContratoRequest;
import com.devsuj.crmapi.dto.postventa.ContratoResponse;
import com.devsuj.crmapi.dto.postventa.RenovacionContratoRequest;
import com.devsuj.crmapi.dto.postventa.VencimientoLoteResponse;
import com.devsuj.crmapi.exception.AccesoDenegadoException;
import com.devsuj.crmapi.exception.DatosInvalidosException;
import com.devsuj.crmapi.exception.RecursoNoEncontradoException;
import com.devsuj.crmapi.exception.ReglaNegocioException;
import com.devsuj.crmapi.mapper.PostventaMapper;
import com.devsuj.crmapi.model.Cliente;
import com.devsuj.crmapi.model.Contrato;
import com.devsuj.crmapi.model.EstadoContrato;
import com.devsuj.crmapi.model.Venta;
import com.devsuj.crmapi.repository.ContratoRepository;
import com.devsuj.crmapi.repository.Filtros;
import com.devsuj.crmapi.repository.VentaRepository;
import com.devsuj.crmapi.security.Alcance;
import com.devsuj.crmapi.security.ControlAcceso;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Contratos (P19, P20). BORRADOR → VIGENTE | CANCELADO; VIGENTE → VENCIDO | CANCELADO. Solo se edita en BORRADOR;
 * un VIGENTE se extiende; una renovación es un contrato nuevo ligado (una vez por contrato). Sin borrado.
 * Se ven por el vendedor asignado al cliente (alcance) y los gestiona quien gestiona al cliente.
 */
@Service
@RequiredArgsConstructor
public class ContratoService {

    private final ContratoRepository repository;
    private final VentaRepository ventaRepository;
    private final ClienteService clienteService;
    private final PostventaMapper mapper;
    private final ControlAcceso controlAcceso;
    private final Clock clock;

    /** @param porVencerDias solo VIGENTE cuya fecha de fin cae entre hoy y hoy + N días */
    @Transactional(readOnly = true)
    public PaginaResponse<ContratoResponse> listar(Integer clienteId, EstadoContrato estado, Integer porVencerDias,
            Pageable pageable) {
        LocalDate hoy = hoy();
        Specification<Contrato> filtro = Specification.allOf(
                Filtros.clienteVisiblePara(controlAcceso.alcance()),
                Filtros.igual(clienteId, "cliente", "id"),
                Filtros.igual(estado, "estado"),
                porVencer(porVencerDias, hoy));
        return PaginaResponse.de(repository.findAll(filtro, pageable), c -> mapper.toResponse(c, hoy));
    }

    @Transactional(readOnly = true)
    public ContratoResponse obtener(Integer id) {
        Contrato contrato = buscar(id);
        verificarLectura(contrato);
        return mapper.toResponse(contrato, hoy());
    }

    @Transactional
    public ContratoResponse crear(ContratoRequest request) {
        Cliente cliente = clienteService.buscarOperable(request.clienteId(), "registrar contratos");
        Contrato contrato = new Contrato();
        contrato.setCliente(cliente);
        contrato.setEstado(EstadoContrato.BORRADOR);
        copiar(request, contrato);
        return mapper.toResponse(repository.save(contrato), hoy());
    }

    @Transactional
    public ContratoResponse actualizar(Integer id, ContratoRequest request) {
        Contrato contrato = buscarGestionable(id);
        if (contrato.getEstado() != EstadoContrato.BORRADOR) {
            throw new ReglaNegocioException("Solo se edita un contrato en BORRADOR; este está " + contrato.getEstado()
                    + ". Un VIGENTE se extiende o se renueva.");
        }
        if (!contrato.getCliente().getId().equals(request.clienteId())) {
            throw new DatosInvalidosException("El cliente de un contrato no se cambia.");
        }
        copiar(request, contrato);
        return mapper.toResponse(contrato, hoy());
    }

    /** P20: VENCIDO solo para un VIGENTE cuya fecha de fin ya pasó. */
    @Transactional
    public ContratoResponse cambiarEstado(Integer id, EstadoContrato destino) {
        Contrato contrato = buscarGestionable(id);
        if (!contrato.getEstado().puedeCambiarA(destino)) {
            throw new ReglaNegocioException("Un contrato " + contrato.getEstado() + " no puede pasar a " + destino + ".");
        }
        if (destino == EstadoContrato.VENCIDO && !contrato.getFechaFin().isBefore(hoy())) {
            throw new ReglaNegocioException("El contrato vence el " + contrato.getFechaFin()
                    + ": todavía no se puede marcar VENCIDO.");
        }
        contrato.setEstado(destino);
        return mapper.toResponse(contrato, hoy());
    }

    /** P19: solo un VIGENTE, con una fecha de fin posterior a la actual. */
    @Transactional
    public ContratoResponse extender(Integer id, LocalDate nuevaFechaFin) {
        Contrato contrato = buscarGestionable(id);
        if (contrato.getEstado() != EstadoContrato.VIGENTE) {
            throw new ReglaNegocioException("Solo se extiende un contrato VIGENTE; este está " + contrato.getEstado() + ".");
        }
        if (!nuevaFechaFin.isAfter(contrato.getFechaFin())) {
            throw new DatosInvalidosException("La nueva fecha de fin debe ser posterior al " + contrato.getFechaFin() + ".");
        }
        contrato.setFechaFin(nuevaFechaFin);
        return mapper.toResponse(contrato, hoy());
    }

    /** P19 + S28: contrato nuevo en BORRADOR ligado a un VIGENTE o VENCIDO, una sola vez por contrato. */
    @Transactional
    public ContratoResponse renovar(Integer id, RenovacionContratoRequest request) {
        Contrato anterior = buscarGestionable(id);
        if (anterior.getEstado() != EstadoContrato.VIGENTE && anterior.getEstado() != EstadoContrato.VENCIDO) {
            throw new ReglaNegocioException("Solo se renueva un contrato VIGENTE o VENCIDO; este está "
                    + anterior.getEstado() + ".");
        }
        if (repository.existsByContratoAnteriorId(id)) {
            throw new ReglaNegocioException("Este contrato ya tiene una renovación.");
        }
        clienteService.buscarOperable(anterior.getCliente().getId(), "renovar contratos");
        Contrato nuevo = new Contrato();
        nuevo.setCliente(anterior.getCliente());
        nuevo.setContratoAnterior(anterior);
        nuevo.setTitulo(request.titulo() == null || request.titulo().isBlank() ? anterior.getTitulo() : request.titulo());
        nuevo.setFechaInicio(request.fechaInicio());
        nuevo.setFechaFin(request.fechaFin());
        nuevo.setMonto(request.monto());
        nuevo.setEstado(EstadoContrato.BORRADOR);
        return mapper.toResponse(repository.save(nuevo), hoy());
    }

    /** P20: marca VENCIDO todos los VIGENTE con fecha de fin pasada dentro del alcance (sin tarea programada). */
    @Transactional
    public VencimientoLoteResponse vencerEnLote() {
        LocalDate hoy = hoy();
        List<Contrato> vencidos = repository.findAll(Specification.allOf(
                Filtros.clienteVisiblePara(controlAcceso.alcance()), repository.vencidosAl(hoy)));
        vencidos.forEach(c -> c.setEstado(EstadoContrato.VENCIDO));
        return new VencimientoLoteResponse(vencidos.size(), vencidos.stream().map(Contrato::getId).sorted().toList());
    }

    private void copiar(ContratoRequest request, Contrato contrato) {
        contrato.setVenta(request.ventaId() == null ? null : ventaDelCliente(request.ventaId(), contrato.getCliente()));
        contrato.setTitulo(request.titulo());
        contrato.setFechaInicio(request.fechaInicio());
        contrato.setFechaFin(request.fechaFin());
        contrato.setMonto(request.monto());
    }

    private Venta ventaDelCliente(Integer ventaId, Cliente cliente) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("una venta", ventaId));
        if (!venta.getCliente().getId().equals(cliente.getId())) {
            throw new ReglaNegocioException("La venta " + ventaId + " es de otro cliente.");
        }
        return venta;
    }

    private void verificarLectura(Contrato contrato) {
        Alcance alcance = controlAcceso.alcance();
        var vendedor = contrato.getCliente().getVendedor();
        if (!alcance.todos() && (vendedor == null || !alcance.incluye(vendedor.getId()))) {
            throw new AccesoDenegadoException("No tienes acceso a este contrato.");
        }
    }

    private Contrato buscarGestionable(Integer id) {
        Contrato contrato = buscar(id);
        controlAcceso.verificarGestionCliente(contrato.getCliente());
        return contrato;
    }

    private Contrato buscar(Integer id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("un contrato", id));
    }

    private static Specification<Contrato> porVencer(Integer dias, LocalDate hoy) {
        if (dias == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.and(cb.equal(root.get("estado"), EstadoContrato.VIGENTE),
                cb.between(root.get("fechaFin"), hoy, hoy.plusDays(dias)));
    }

    private LocalDate hoy() {
        return LocalDate.now(clock);
    }
}
