package com.devsuj.crmapi.service;

import com.devsuj.crmapi.dto.PaginaResponse;
import com.devsuj.crmapi.dto.fidelizacion.CanjeRequest;
import com.devsuj.crmapi.dto.fidelizacion.CanjeResponse;
import com.devsuj.crmapi.dto.fidelizacion.MovimientoPuntosResponse;
import com.devsuj.crmapi.dto.fidelizacion.SaldoPuntosResponse;
import com.devsuj.crmapi.exception.ReglaNegocioException;
import com.devsuj.crmapi.mapper.FidelizacionMapper;
import com.devsuj.crmapi.model.Cliente;
import com.devsuj.crmapi.model.MovimientoPuntos;
import com.devsuj.crmapi.model.Producto;
import com.devsuj.crmapi.model.TipoMovimientoInventario;
import com.devsuj.crmapi.model.TipoMovimientoPuntos;
import com.devsuj.crmapi.repository.MovimientoPuntosRepository;
import com.devsuj.crmapi.repository.TotalesPuntos;
import com.devsuj.crmapi.security.ControlAcceso;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Programa de fidelización: saldo = Σ GANADO − Σ CANJEADO. Los puntos se ganan al vender
 * ({@link PuntosPorVentaListener}) y se canjean por productos con precio en puntos (P22).
 * Todos ven el estado de cuenta (los clientes son visibles para todos, P3); canjea quien gestiona al cliente.
 */
@Service
@RequiredArgsConstructor
public class FidelizacionService {

    private final MovimientoPuntosRepository repository;
    private final ClienteService clienteService;
    private final ProductoService productoService;
    private final InventarioService inventarioService;
    private final UsuarioService usuarioService;
    private final FidelizacionMapper mapper;
    private final ControlAcceso controlAcceso;
    private final Clock clock;

    @Transactional(readOnly = true)
    public SaldoPuntosResponse saldo(Integer clienteId) {
        Cliente cliente = clienteService.buscar(clienteId);
        return mapper.toResponse(cliente, repository.totalesDe(clienteId));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<MovimientoPuntosResponse> movimientos(Integer clienteId, Pageable pageable) {
        clienteService.buscar(clienteId);
        return PaginaResponse.de(repository.findByClienteId(clienteId, pageable), mapper::toResponse);
    }

    /**
     * Canje (P22): cliente gestionable y no INACTIVO (bloqueado para que dos canjes no gasten el mismo saldo),
     * producto activo y canjeable, saldo suficiente; descuenta stock (kardex CANJE) y registra el CANJEADO.
     */
    @Transactional
    public CanjeResponse canjear(Integer clienteId, CanjeRequest request) {
        Cliente cliente = clienteService.bloquearOperable(clienteId, "canjear puntos");
        Producto producto = productoService.buscarVendible(request.productoId());
        if (!producto.esCanjeable()) {
            throw new ReglaNegocioException("El producto " + producto.getNombre() + " no se puede canjear por puntos.");
        }
        int cantidad = request.cantidad();
        long necesarios = (long) producto.getPrecioPuntos() * cantidad;
        long saldo = repository.totalesDe(clienteId).saldo();
        if (saldo < necesarios) {
            throw new ReglaNegocioException("Puntos insuficientes: el cliente tiene " + saldo + " y el canje cuesta "
                    + necesarios + ".", Map.of("saldo", saldo, "puntosNecesarios", necesarios));
        }
        String motivo = "Canje: " + cantidad + " × " + producto.getNombre();
        inventarioService.salida(producto, cantidad, TipoMovimientoInventario.CANJE, motivo, null);

        MovimientoPuntos movimiento = new MovimientoPuntos();
        movimiento.setCliente(cliente);
        movimiento.setTipo(TipoMovimientoPuntos.CANJEADO);
        movimiento.setPuntos(Math.toIntExact(necesarios));
        movimiento.setProducto(producto);
        movimiento.setCantidad(cantidad);
        movimiento.setUsuario(usuarioService.buscar(controlAcceso.usuario().id()));
        movimiento.setMotivo(motivo);
        movimiento.setFecha(LocalDateTime.now(clock));
        repository.save(movimiento);
        return new CanjeResponse(mapper.toResponse(movimiento), saldo - necesarios);
    }
}
