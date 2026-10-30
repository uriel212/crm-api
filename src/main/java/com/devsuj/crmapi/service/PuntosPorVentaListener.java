package com.devsuj.crmapi.service;

import com.devsuj.crmapi.model.MovimientoPuntos;
import com.devsuj.crmapi.model.TipoMovimientoPuntos;
import com.devsuj.crmapi.model.Venta;
import com.devsuj.crmapi.repository.MovimientoPuntosRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Otorga los puntos de fidelización de una venta, en la misma transacción (idempotente por venta). */
@Component
@RequiredArgsConstructor
public class PuntosPorVentaListener {

    private final MovimientoPuntosRepository repository;
    private final ReglaPuntos reglaPuntos;

    @EventListener
    public void alRegistrarVenta(VentaPagadaEvent evento) {
        Venta venta = evento.venta();
        int puntos = reglaPuntos.puntosPor(venta.getTotal());
        if (puntos <= 0 || repository.existsByVentaId(venta.getId())) {
            return;
        }
        MovimientoPuntos movimiento = new MovimientoPuntos();
        movimiento.setCliente(venta.getCliente());
        movimiento.setVenta(venta);
        movimiento.setUsuario(venta.getUsuario());
        movimiento.setTipo(TipoMovimientoPuntos.GANADO);
        movimiento.setPuntos(puntos);
        movimiento.setMotivo("Compra #" + venta.getId());
        movimiento.setFecha(venta.getFecha());
        repository.save(movimiento);
    }
}
