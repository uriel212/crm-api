package com.devsuj.crmapi.mapper;

import com.devsuj.crmapi.dto.meta.MetaVentaResponse;
import com.devsuj.crmapi.model.MetaVenta;
import com.devsuj.crmapi.repository.VentasDelPeriodo;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class MetaVentaMapper {

    /** @param ventas lo vendido en el mes de la meta; null si no vendió nada */
    public MetaVentaResponse toResponse(MetaVenta meta, VentasDelPeriodo ventas) {
        BigDecimal objetivo = meta.getMontoObjetivo();
        BigDecimal vendido = ventas == null ? BigDecimal.ZERO.setScale(2) : ventas.total();
        long numero = ventas == null ? 0 : ventas.numeroVentas();
        return new MetaVentaResponse(meta.getId(), meta.getUsuario().getId(), meta.getUsuario().getNombre(),
                meta.getAnio(), meta.getMes(), objetivo, vendido, numero, Porcentajes.de(vendido, objetivo),
                objetivo.subtract(vendido).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP),
                vendido.compareTo(objetivo) >= 0);
    }
}
