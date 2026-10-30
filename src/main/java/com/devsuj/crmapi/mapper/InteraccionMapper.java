package com.devsuj.crmapi.mapper;

import com.devsuj.crmapi.dto.interaccion.InteraccionResponse;
import com.devsuj.crmapi.model.EstadoInteraccion;
import com.devsuj.crmapi.model.Interaccion;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class InteraccionMapper {

    public InteraccionResponse toResponse(Interaccion i, LocalDateTime ahora) {
        boolean vencida = i.getEstado() == EstadoInteraccion.PROGRAMADA && i.getFecha().isBefore(ahora);
        return new InteraccionResponse(i.getId(), i.getCliente().getId(), i.getCliente().getNombre(),
                i.getUsuario().getId(), i.getUsuario().getNombre(), i.getTipo(), i.getEstado(), i.getFecha(),
                i.getAsunto(), i.getDescripcion(), i.getResultado(), vencida);
    }
}
