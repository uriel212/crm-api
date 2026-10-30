package com.devsuj.crmapi.service;

import com.devsuj.crmapi.exception.ReglaNegocioException;
import com.devsuj.crmapi.model.MovimientoInventario;
import com.devsuj.crmapi.model.Producto;
import com.devsuj.crmapi.model.TipoMovimientoInventario;
import com.devsuj.crmapi.model.Venta;
import com.devsuj.crmapi.repository.MovimientoInventarioRepository;
import com.devsuj.crmapi.repository.ProductoRepository;
import com.devsuj.crmapi.security.SesionActual;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Único lugar donde cambia el stock (SRP): cada cambio deja un movimiento en el kardex. Lo usan el ajuste
 * manual, las ventas y los canjes de puntos, siempre dentro de la transacción de quien lo llama.
 */
@Service
@RequiredArgsConstructor
public class InventarioService {

    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final UsuarioService usuarioService;
    private final SesionActual sesion;
    private final EntityManager entityManager;
    private final Clock clock;

    @Transactional
    public MovimientoInventario entrada(Producto producto, int cantidad, String motivo) {
        productoRepository.sumarStock(producto.getId(), cantidad);
        return registrar(producto, TipoMovimientoInventario.ENTRADA, cantidad, motivo, null);
    }

    /** Resta stock de forma atómica; 409 si no alcanza. */
    @Transactional
    public MovimientoInventario salida(Producto producto, int cantidad, TipoMovimientoInventario tipo, String motivo,
            Venta venta) {
        if (productoRepository.descontarStock(producto.getId(), cantidad) == 0) {
            entityManager.refresh(producto);
            throw new ReglaNegocioException("Stock insuficiente para " + producto.getNombre() + ": hay "
                    + producto.getStock() + " y se piden " + cantidad + ".");
        }
        return registrar(producto, tipo, cantidad, motivo, venta);
    }

    private MovimientoInventario registrar(Producto producto, TipoMovimientoInventario tipo, int cantidad,
            String motivo, Venta venta) {
        entityManager.refresh(producto);
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setUsuario(usuarioService.buscar(sesion.obtener().id()));
        movimiento.setVenta(venta);
        movimiento.setTipo(tipo);
        movimiento.setCantidad(cantidad);
        movimiento.setStockResultante(producto.getStock());
        movimiento.setMotivo(motivo);
        movimiento.setFecha(LocalDateTime.now(clock));
        return movimientoRepository.save(movimiento);
    }
}
