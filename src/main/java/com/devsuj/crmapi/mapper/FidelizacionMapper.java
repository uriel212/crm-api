package com.devsuj.crmapi.mapper;

import com.devsuj.crmapi.dto.fidelizacion.MovimientoPuntosResponse;
import com.devsuj.crmapi.dto.fidelizacion.SaldoPuntosResponse;
import com.devsuj.crmapi.model.Cliente;
import com.devsuj.crmapi.model.MovimientoPuntos;
import com.devsuj.crmapi.model.Producto;
import com.devsuj.crmapi.model.Usuario;
import com.devsuj.crmapi.repository.TotalesPuntos;
import org.springframework.stereotype.Component;

@Component
public class FidelizacionMapper {

    public SaldoPuntosResponse toResponse(Cliente cliente, TotalesPuntos totales) {
        return new SaldoPuntosResponse(cliente.getId(), cliente.getNombre(), totales.ganados(), totales.canjeados(),
                totales.saldo());
    }

    public MovimientoPuntosResponse toResponse(MovimientoPuntos m) {
        Producto producto = m.getProducto();
        Usuario usuario = m.getUsuario();
        return new MovimientoPuntosResponse(m.getId(), m.getTipo(), m.getPuntos(), m.getMotivo(), m.getFecha(),
                m.getVenta() == null ? null : m.getVenta().getId(),
                producto == null ? null : producto.getId(), producto == null ? null : producto.getNombre(),
                m.getCantidad(),
                usuario == null ? null : usuario.getId(), usuario == null ? null : usuario.getNombre());
    }
}
