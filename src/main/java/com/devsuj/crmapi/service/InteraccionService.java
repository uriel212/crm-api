package com.devsuj.crmapi.service;

import com.devsuj.crmapi.dto.PaginaResponse;
import com.devsuj.crmapi.dto.interaccion.CambioEstadoInteraccionRequest;
import com.devsuj.crmapi.dto.interaccion.InteraccionActualizarRequest;
import com.devsuj.crmapi.dto.interaccion.InteraccionRequest;
import com.devsuj.crmapi.dto.interaccion.InteraccionResponse;
import com.devsuj.crmapi.exception.DatosInvalidosException;
import com.devsuj.crmapi.exception.RecursoNoEncontradoException;
import com.devsuj.crmapi.exception.ReglaNegocioException;
import com.devsuj.crmapi.mapper.InteraccionMapper;
import com.devsuj.crmapi.model.Cliente;
import com.devsuj.crmapi.model.EstadoInteraccion;
import com.devsuj.crmapi.model.Interaccion;
import com.devsuj.crmapi.model.TipoInteraccion;
import com.devsuj.crmapi.repository.Filtros;
import com.devsuj.crmapi.repository.InteraccionRepository;
import com.devsuj.crmapi.security.Alcance;
import com.devsuj.crmapi.security.ControlAcceso;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Historial de contacto y agenda de actividades (P6). Las interacciones no se borran: una actividad
 * PROGRAMADA se cierra como REALIZADA o CANCELADA (P7).
 */
@Service
@RequiredArgsConstructor
public class InteraccionService {

    private static final String RECURSO = "esta interacción";

    private final InteraccionRepository repository;
    private final ClienteService clienteService;
    private final UsuarioService usuarioService;
    private final InteraccionMapper mapper;
    private final ControlAcceso controlAcceso;
    private final Clock clock;

    @Transactional(readOnly = true)
    public PaginaResponse<InteraccionResponse> listar(Integer clienteId, Integer usuarioId, TipoInteraccion tipo,
            EstadoInteraccion estado, LocalDate desde, LocalDate hasta, Pageable pageable) {
        Alcance alcance = controlAcceso.alcance();
        controlAcceso.verificarFiltroUsuario(alcance, usuarioId);
        if (clienteId != null) {
            clienteService.buscar(clienteId);
        }
        Specification<Interaccion> filtro = Specification.allOf(
                Filtros.visiblePara(alcance, "usuario"),
                Filtros.igual(clienteId, "cliente", "id"),
                Filtros.igual(usuarioId, "usuario", "id"),
                Filtros.igual(tipo, "tipo"),
                Filtros.igual(estado, "estado"),
                Filtros.desde(desde == null ? null : desde.atStartOfDay(), "fecha"),
                Filtros.antesDe(hasta == null ? null : hasta.plusDays(1).atStartOfDay(), "fecha"));
        LocalDateTime ahora = ahora();
        return PaginaResponse.de(repository.findAll(filtro, pageable), i -> mapper.toResponse(i, ahora));
    }

    /** Agenda: actividades PROGRAMADA del usuario (por defecto el actual) hasta dentro de {@code dias}, incluidas las vencidas. */
    @Transactional(readOnly = true)
    public List<InteraccionResponse> proximas(Integer usuarioId, int dias) {
        Integer id = usuarioId == null ? controlAcceso.usuario().id() : usuarioId;
        controlAcceso.verificarFiltroUsuario(controlAcceso.alcance(), id);
        LocalDateTime ahora = ahora();
        return repository.findByUsuarioIdAndEstadoAndFechaBeforeOrderByFechaAsc(
                        id, EstadoInteraccion.PROGRAMADA, ahora.plusDays(dias))
                .stream().map(i -> mapper.toResponse(i, ahora)).toList();
    }

    @Transactional(readOnly = true)
    public InteraccionResponse obtener(Integer id) {
        Interaccion interaccion = buscar(id);
        controlAcceso.verificarLectura(interaccion.getUsuario(), interaccion.getCliente(), RECURSO);
        return mapper.toResponse(interaccion, ahora());
    }

    @Transactional
    public InteraccionResponse crear(Integer clienteId, InteraccionRequest request) {
        Cliente cliente = clienteService.buscarGestionable(clienteId);
        EstadoInteraccion estado = request.estado() == null ? EstadoInteraccion.REALIZADA : request.estado();
        if (estado == EstadoInteraccion.CANCELADA) {
            throw new DatosInvalidosException("Una interacción nueva solo puede ser REALIZADA o PROGRAMADA.");
        }
        LocalDateTime fecha = request.fecha() != null || estado == EstadoInteraccion.PROGRAMADA
                ? request.fecha() : ahora();
        validarFecha(estado, fecha);
        Interaccion interaccion = new Interaccion();
        interaccion.setCliente(cliente);
        interaccion.setUsuario(usuarioService.buscarActivo(controlAcceso.usuario().id()));
        interaccion.setTipo(request.tipo());
        interaccion.setEstado(estado);
        interaccion.setFecha(fecha);
        interaccion.setAsunto(request.asunto());
        interaccion.setDescripcion(request.descripcion());
        return mapper.toResponse(repository.save(interaccion), ahora());
    }

    @Transactional
    public InteraccionResponse actualizar(Integer id, InteraccionActualizarRequest request) {
        Interaccion interaccion = buscarModificable(id);
        if (interaccion.getEstado() == EstadoInteraccion.CANCELADA) {
            throw new ReglaNegocioException("Una interacción CANCELADA no se edita.");
        }
        validarFecha(interaccion.getEstado(), request.fecha());
        interaccion.setTipo(request.tipo());
        interaccion.setFecha(request.fecha());
        interaccion.setAsunto(request.asunto());
        interaccion.setDescripcion(request.descripcion());
        return mapper.toResponse(interaccion, ahora());
    }

    @Transactional
    public InteraccionResponse cambiarEstado(Integer id, CambioEstadoInteraccionRequest request) {
        Interaccion interaccion = buscarModificable(id);
        if (interaccion.getEstado() != EstadoInteraccion.PROGRAMADA) {
            throw new ReglaNegocioException("Solo una actividad PROGRAMADA cambia de estado; esta ya está "
                    + interaccion.getEstado() + ".");
        }
        if (!request.estado().esFinal()) {
            throw new DatosInvalidosException("El nuevo estado debe ser REALIZADA o CANCELADA.");
        }
        LocalDateTime ahora = ahora();
        if (request.estado() == EstadoInteraccion.REALIZADA && interaccion.getFecha().isAfter(ahora)) {
            interaccion.setFecha(ahora);
        }
        interaccion.setEstado(request.estado());
        interaccion.setResultado(request.resultado());
        return mapper.toResponse(interaccion, ahora);
    }

    private void validarFecha(EstadoInteraccion estado, LocalDateTime fecha) {
        if (estado == EstadoInteraccion.PROGRAMADA && (fecha == null || fecha.isBefore(ahora()))) {
            throw new DatosInvalidosException("Una actividad PROGRAMADA necesita una fecha futura.");
        }
        if (estado == EstadoInteraccion.REALIZADA && fecha.isAfter(ahora())) {
            throw new DatosInvalidosException(
                    "Una interacción REALIZADA no puede tener fecha futura; agéndala como PROGRAMADA.");
        }
    }

    private Interaccion buscarModificable(Integer id) {
        Interaccion interaccion = buscar(id);
        controlAcceso.verificarModificacion(interaccion.getUsuario(), RECURSO);
        return interaccion;
    }

    private Interaccion buscar(Integer id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("una interacción", id));
    }

    private LocalDateTime ahora() {
        return LocalDateTime.now(clock);
    }
}
