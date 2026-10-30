package com.devsuj.crmapi.mapper;

import com.devsuj.crmapi.dto.oportunidad.HistorialEtapaResponse;
import com.devsuj.crmapi.dto.oportunidad.OportunidadResponse;
import com.devsuj.crmapi.model.Oportunidad;
import com.devsuj.crmapi.model.OportunidadHistorialEtapa;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class OportunidadMapper {

    public OportunidadResponse toResponse(Oportunidad o) {
        Oportunidad anterior = o.getOportunidadAnterior();
        return new OportunidadResponse(o.getId(), o.getCliente().getId(), o.getCliente().getNombre(),
                o.getUsuario().getId(), o.getUsuario().getNombre(), o.getTitulo(), o.getMontoEstimado(),
                o.getEtapa(), o.getFechaCierreEstimada(), o.getCreadoEn(), anterior == null ? null : anterior.getId());
    }

    /**
     * @param fin    cuándo terminó el paso (fecha del siguiente); null si es el actual
     * @param ahora  para contar los días de la etapa actual si sigue abierta
     */
    public HistorialEtapaResponse toResponse(OportunidadHistorialEtapa paso, LocalDateTime fin, LocalDateTime ahora) {
        boolean actual = fin == null;
        Long dias = actual && paso.getEtapaNueva().esCerrada()
                ? null
                : ChronoUnit.DAYS.between(paso.getFecha(), actual ? ahora : fin);
        return new HistorialEtapaResponse(paso.getId(), paso.getEtapaAnterior(), paso.getEtapaNueva(),
                paso.getFecha(), fin, dias, paso.getUsuario().getId(), paso.getUsuario().getNombre(),
                paso.getComentario(), actual);
    }
}
