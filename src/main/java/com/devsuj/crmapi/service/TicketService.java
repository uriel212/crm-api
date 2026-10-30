package com.devsuj.crmapi.service;

import com.devsuj.crmapi.dto.PaginaResponse;
import com.devsuj.crmapi.dto.postventa.TicketActualizarRequest;
import com.devsuj.crmapi.dto.postventa.TicketRequest;
import com.devsuj.crmapi.dto.postventa.TicketResponse;
import com.devsuj.crmapi.exception.RecursoNoEncontradoException;
import com.devsuj.crmapi.exception.ReglaNegocioException;
import com.devsuj.crmapi.mapper.PostventaMapper;
import com.devsuj.crmapi.model.Cliente;
import com.devsuj.crmapi.model.EstadoTicket;
import com.devsuj.crmapi.model.PrioridadTicket;
import com.devsuj.crmapi.model.TicketSoporte;
import com.devsuj.crmapi.model.Usuario;
import com.devsuj.crmapi.repository.Filtros;
import com.devsuj.crmapi.repository.TicketSoporteRepository;
import com.devsuj.crmapi.security.Alcance;
import com.devsuj.crmapi.security.ControlAcceso;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Tickets de soporte (P21): ABIERTO ↔ EN_PROCESO → CERRADO (también ABIERTO → CERRADO). EN_PROCESO exige
 * responsable. Un CERRADO no se edita ni se reabre: el seguimiento es un ticket nuevo ligado.
 * Visibles por responsable o por vendedor asignado al cliente (alcance); los gestiona el responsable,
 * quien gestiona al cliente, o su gerente/ADMIN.
 */
@Service
@RequiredArgsConstructor
public class TicketService {

    private static final String RECURSO = "este ticket";

    private final TicketSoporteRepository repository;
    private final ClienteService clienteService;
    private final UsuarioService usuarioService;
    private final PostventaMapper mapper;
    private final ControlAcceso controlAcceso;
    private final Clock clock;

    @Transactional(readOnly = true)
    public PaginaResponse<TicketResponse> listar(EstadoTicket estado, PrioridadTicket prioridad, Integer clienteId,
            Integer usuarioId, Boolean sinAsignar, Pageable pageable) {
        Alcance alcance = controlAcceso.alcance();
        controlAcceso.verificarFiltroUsuario(alcance, usuarioId);
        Specification<TicketSoporte> filtro = Specification.allOf(
                visiblePara(alcance),
                Filtros.igual(estado, "estado"),
                Filtros.igual(prioridad, "prioridad"),
                Filtros.igual(clienteId, "cliente", "id"),
                Filtros.igual(usuarioId, "usuario", "id"),
                soloSinAsignar(sinAsignar));
        return PaginaResponse.de(repository.findAll(filtro, pageable), mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public TicketResponse obtener(Integer id) {
        TicketSoporte ticket = buscar(id);
        controlAcceso.verificarLectura(ticket.getUsuario(), ticket.getCliente(), RECURSO);
        return mapper.toResponse(ticket);
    }

    @Transactional
    public TicketResponse crear(TicketRequest request) {
        Cliente cliente = clienteService.buscarGestionable(request.clienteId());
        TicketSoporte ticket = nuevo(cliente, request.asunto(), request.descripcion(), request.prioridad());
        ticket.setUsuario(request.usuarioId() == null ? null : responsableValido(request.usuarioId()));
        return mapper.toResponse(repository.save(ticket));
    }

    @Transactional
    public TicketResponse actualizar(Integer id, TicketActualizarRequest request) {
        TicketSoporte ticket = buscarGestionableAbierto(id, "editar");
        ticket.setAsunto(request.asunto());
        ticket.setDescripcion(request.descripcion());
        ticket.setPrioridad(request.prioridad());
        return mapper.toResponse(ticket);
    }

    /** Asigna o quita responsable (S29: GERENTE/ADMIN, dentro de su alcance). EN_PROCESO no queda sin responsable. */
    @Transactional
    public TicketResponse asignar(Integer id, Integer usuarioId) {
        TicketSoporte ticket = buscarGestionableAbierto(id, "reasignar");
        if (usuarioId == null && ticket.getEstado() == EstadoTicket.EN_PROCESO) {
            throw new ReglaNegocioException("Un ticket EN_PROCESO necesita responsable.");
        }
        ticket.setUsuario(usuarioId == null ? null : responsableValido(usuarioId));
        return mapper.toResponse(ticket);
    }

    @Transactional
    public TicketResponse cambiarEstado(Integer id, EstadoTicket destino) {
        TicketSoporte ticket = buscarGestionable(id);
        if (!ticket.getEstado().puedeCambiarA(destino)) {
            throw new ReglaNegocioException(ticket.getEstado() == EstadoTicket.CERRADO
                    ? "Un ticket CERRADO no se reabre: crea un seguimiento (POST /api/tickets/{id}/seguimiento)."
                    : "Un ticket " + ticket.getEstado() + " no puede pasar a " + destino + ".");
        }
        if (destino == EstadoTicket.EN_PROCESO && ticket.getUsuario() == null) {
            throw new ReglaNegocioException("Asigna un responsable antes de pasar el ticket a EN_PROCESO.");
        }
        ticket.setEstado(destino);
        ticket.setCerradoEn(destino == EstadoTicket.CERRADO ? LocalDateTime.now(clock) : null);
        return mapper.toResponse(ticket);
    }

    /** P21: ticket nuevo ABIERTO ligado a uno CERRADO, con el mismo cliente y responsable. */
    @Transactional
    public TicketResponse seguimiento(Integer id, TicketActualizarRequest request) {
        TicketSoporte anterior = buscarGestionable(id);
        if (anterior.getEstado() != EstadoTicket.CERRADO) {
            throw new ReglaNegocioException("El seguimiento es para tickets CERRADO; este sigue " + anterior.getEstado()
                    + ": actualízalo directamente.");
        }
        TicketSoporte ticket = nuevo(anterior.getCliente(), request.asunto(), request.descripcion(), request.prioridad());
        ticket.setTicketRelacionado(anterior);
        Usuario responsable = anterior.getUsuario();
        ticket.setUsuario(responsable != null && responsable.isActivo() ? responsable : null);
        return mapper.toResponse(repository.save(ticket));
    }

    private TicketSoporte nuevo(Cliente cliente, String asunto, String descripcion, PrioridadTicket prioridad) {
        TicketSoporte ticket = new TicketSoporte();
        ticket.setCliente(cliente);
        ticket.setAsunto(asunto);
        ticket.setDescripcion(descripcion);
        ticket.setPrioridad(prioridad);
        ticket.setEstado(EstadoTicket.ABIERTO);
        ticket.setCreadoEn(LocalDateTime.now(clock));
        return ticket;
    }

    /** Activo y dentro del alcance: un VENDEDOR solo se asigna a sí mismo (S29). */
    private Usuario responsableValido(Integer usuarioId) {
        return usuarioService.responsableParaRegistro(usuarioId);
    }

    /** El responsable (o su gerente/ADMIN) o quien gestiona al cliente. */
    private TicketSoporte buscarGestionable(Integer id) {
        TicketSoporte ticket = buscar(id);
        Usuario responsable = ticket.getUsuario();
        if (responsable == null || !controlAcceso.alcance().incluye(responsable.getId())) {
            controlAcceso.verificarGestionCliente(ticket.getCliente());
        }
        return ticket;
    }

    private TicketSoporte buscarGestionableAbierto(Integer id, String accion) {
        TicketSoporte ticket = buscarGestionable(id);
        if (ticket.getEstado() == EstadoTicket.CERRADO) {
            throw new ReglaNegocioException("Un ticket CERRADO no se puede " + accion + ": crea un seguimiento.");
        }
        return ticket;
    }

    private TicketSoporte buscar(Integer id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("un ticket", id));
    }

    /** Responsable en el alcance o cliente asignado a alguien del alcance (left join: el responsable puede faltar). */
    private static Specification<TicketSoporte> visiblePara(Alcance alcance) {
        if (alcance.todos()) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.or(
                root.join("usuario", jakarta.persistence.criteria.JoinType.LEFT).get("id").in(alcance.idsParaConsulta()),
                root.join("cliente").join("vendedor", jakarta.persistence.criteria.JoinType.LEFT).get("id")
                        .in(alcance.idsParaConsulta()));
    }

    private static Specification<TicketSoporte> soloSinAsignar(Boolean sinAsignar) {
        if (sinAsignar == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> sinAsignar ? cb.isNull(root.get("usuario")) : cb.isNotNull(root.get("usuario"));
    }
}
