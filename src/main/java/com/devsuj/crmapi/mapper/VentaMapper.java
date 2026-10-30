package com.devsuj.crmapi.mapper;

import com.devsuj.crmapi.dto.venta.CalculoVentaResponse;
import com.devsuj.crmapi.dto.venta.DetalleVentaResponse;
import com.devsuj.crmapi.dto.venta.VentaResponse;
import com.devsuj.crmapi.dto.venta.VentaResumenResponse;
import com.devsuj.crmapi.model.DetalleVenta;
import com.devsuj.crmapi.model.Oportunidad;
import com.devsuj.crmapi.model.Promocion;
import com.devsuj.crmapi.model.Venta;
import com.devsuj.crmapi.service.CalculoVenta;
import org.springframework.stereotype.Component;

@Component
public class VentaMapper {

    public VentaResumenResponse toResumen(Venta v) {
        return new VentaResumenResponse(v.getId(), v.getCliente().getId(), v.getCliente().getNombre(),
                v.getUsuario().getId(), v.getUsuario().getNombre(), idDe(v.getOportunidad()), v.getFecha(),
                v.getSubtotal(), v.getDescuento(), v.getTotal());
    }

    public VentaResponse toResponse(Venta v, Integer puntosGanados) {
        return new VentaResponse(v.getId(), v.getCliente().getId(), v.getCliente().getNombre(),
                v.getUsuario().getId(), v.getUsuario().getNombre(), idDe(v.getOportunidad()), v.getFecha(),
                v.getSubtotal(), v.getDescuento(), idDe(v.getPromocion()), nombreDe(v.getPromocion()), v.getTotal(),
                puntosGanados, v.getDetalles().stream().map(this::toResponse).toList());
    }

    public CalculoVentaResponse toResponse(CalculoVenta calculo) {
        return new CalculoVentaResponse(calculo.renglones().stream().map(this::toResponse).toList(),
                calculo.subtotal(), calculo.descuento(), idDe(calculo.promocion()), nombreDe(calculo.promocion()),
                calculo.total());
    }

    /** Renglón persistible a partir del cálculo (precio y descuento quedan congelados). */
    public DetalleVenta toDetalle(CalculoVenta.Renglon r) {
        DetalleVenta detalle = new DetalleVenta();
        detalle.setProducto(r.producto());
        detalle.setCantidad(r.cantidad());
        detalle.setPrecioUnitario(r.precioUnitario());
        detalle.setPromocion(r.promocion());
        detalle.setDescuento(r.descuento());
        detalle.setSubtotal(r.subtotal());
        return detalle;
    }

    private DetalleVentaResponse toResponse(DetalleVenta d) {
        return new DetalleVentaResponse(d.getProducto().getId(), d.getProducto().getNombre(), d.getCantidad(),
                d.getPrecioUnitario(), d.getDescuento(), d.getSubtotal(), idDe(d.getPromocion()),
                nombreDe(d.getPromocion()));
    }

    private DetalleVentaResponse toResponse(CalculoVenta.Renglon r) {
        return new DetalleVentaResponse(r.producto().getId(), r.producto().getNombre(), r.cantidad(),
                r.precioUnitario(), r.descuento(), r.subtotal(), idDe(r.promocion()), nombreDe(r.promocion()));
    }

    private static Integer idDe(Promocion p) {
        return p == null ? null : p.getId();
    }

    private static Integer idDe(Oportunidad o) {
        return o == null ? null : o.getId();
    }

    private static String nombreDe(Promocion p) {
        return p == null ? null : p.getNombre();
    }
}
