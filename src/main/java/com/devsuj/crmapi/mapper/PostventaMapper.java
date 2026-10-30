package com.devsuj.crmapi.mapper;

import com.devsuj.crmapi.dto.postventa.ContratoResponse;
import com.devsuj.crmapi.dto.postventa.TicketResponse;
import com.devsuj.crmapi.model.Cliente;
import com.devsuj.crmapi.model.Contrato;
import com.devsuj.crmapi.model.EstadoContrato;
import com.devsuj.crmapi.model.TicketSoporte;
import com.devsuj.crmapi.model.Usuario;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class PostventaMapper {

    public ContratoResponse toResponse(Contrato c, LocalDate hoy) {
        Long dias = c.getEstado() == EstadoContrato.VIGENTE ? ChronoUnit.DAYS.between(hoy, c.getFechaFin()) : null;
        return new ContratoResponse(c.getId(), c.getCliente().getId(), c.getCliente().getNombre(),
                c.getVenta() == null ? null : c.getVenta().getId(),
                c.getContratoAnterior() == null ? null : c.getContratoAnterior().getId(),
                c.getTitulo(), c.getFechaInicio(), c.getFechaFin(), c.getMonto(), c.getEstado(), dias,
                vendedorDe(c.getCliente()));
    }

    public TicketResponse toResponse(TicketSoporte t) {
        Usuario responsable = t.getUsuario();
        return new TicketResponse(t.getId(), t.getCliente().getId(), t.getCliente().getNombre(),
                responsable == null ? null : responsable.getId(), responsable == null ? null : responsable.getNombre(),
                t.getAsunto(), t.getDescripcion(), t.getPrioridad(), t.getEstado(), t.getCreadoEn(), t.getCerradoEn(),
                t.getTicketRelacionado() == null ? null : t.getTicketRelacionado().getId(),
                vendedorDe(t.getCliente()));
    }

    private static Integer vendedorDe(Cliente cliente) {
        return cliente.getVendedor() == null ? null : cliente.getVendedor().getId();
    }
}
