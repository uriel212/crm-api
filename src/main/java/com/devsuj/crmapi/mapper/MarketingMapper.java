package com.devsuj.crmapi.mapper;

import com.devsuj.crmapi.dto.marketing.CampanaRequest;
import com.devsuj.crmapi.dto.marketing.CampanaResponse;
import com.devsuj.crmapi.dto.marketing.DestinatarioResponse;
import com.devsuj.crmapi.dto.marketing.MetricasCampanaResponse;
import com.devsuj.crmapi.dto.marketing.PromocionResponse;
import com.devsuj.crmapi.model.Campana;
import com.devsuj.crmapi.model.CampanaCliente;
import com.devsuj.crmapi.model.Cliente;
import com.devsuj.crmapi.model.Producto;
import com.devsuj.crmapi.model.Promocion;
import com.devsuj.crmapi.model.ResultadoCampana;
import com.devsuj.crmapi.model.Usuario;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Map;

@Component
public class MarketingMapper {

    public CampanaResponse toResponse(Campana c, long totalDestinatarios) {
        return new CampanaResponse(c.getId(), c.getNombre(), c.getCanal(), c.getAsunto(), c.getMensaje(),
                c.getSegmento(), c.getFechaInicio(), c.getFechaFin(), c.getPresupuesto(), c.getEstado(),
                totalDestinatarios);
    }

    public void copiar(CampanaRequest request, Campana campana) {
        campana.setNombre(request.nombre());
        campana.setCanal(request.canal());
        campana.setAsunto(request.asunto());
        campana.setMensaje(request.mensaje());
        campana.setSegmento(request.segmento());
        campana.setFechaInicio(request.fechaInicio());
        campana.setFechaFin(request.fechaFin());
        campana.setPresupuesto(request.presupuesto());
    }

    public DestinatarioResponse toResponse(CampanaCliente cc) {
        Cliente cliente = cc.getCliente();
        Usuario vendedor = cliente.getVendedor();
        return new DestinatarioResponse(cc.getId().campanaId(), cliente.getId(), cliente.getNombre(), cliente.getEmail(),
                vendedor == null ? null : vendedor.getId(), vendedor == null ? null : vendedor.getNombre(),
                cc.getResultado());
    }

    /** @param porResultado cuántos destinatarios hay en cada resultado */
    public MetricasCampanaResponse toMetricas(Campana c, Map<ResultadoCampana, Long> porResultado) {
        long respondieron = porResultado.getOrDefault(ResultadoCampana.RESPONDIO, 0L);
        long abiertos = porResultado.getOrDefault(ResultadoCampana.ABIERTO, 0L) + respondieron;
        long total = abiertos + porResultado.getOrDefault(ResultadoCampana.ENVIADO, 0L);
        BigDecimal costo = respondieron == 0
                ? null
                : c.getPresupuesto().divide(BigDecimal.valueOf(respondieron), 2, RoundingMode.HALF_UP);
        return new MetricasCampanaResponse(c.getId(), c.getNombre(), c.getEstado(), total, abiertos, respondieron,
                Porcentajes.de(abiertos, total), Porcentajes.de(respondieron, total), c.getPresupuesto(), costo);
    }

    public PromocionResponse toResponse(Promocion p, LocalDate hoy) {
        Campana campana = p.getCampana();
        return new PromocionResponse(p.getId(), campana == null ? null : campana.getId(),
                campana == null ? null : campana.getNombre(), p.getNombre(), p.getTipo(), p.getAlcance(), p.getValor(),
                p.getMontoMinimo(), p.getFechaInicio(), p.getFechaFin(), p.isActiva(), p.estaVigente(hoy),
                p.getProductos().stream()
                        .sorted(Comparator.comparing(Producto::getId))
                        .map(pr -> new PromocionResponse.ProductoDePromocion(pr.getId(), pr.getNombre()))
                        .toList());
    }
}
