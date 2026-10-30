package com.devsuj.crmapi.service;

import com.devsuj.crmapi.dto.PaginaResponse;
import com.devsuj.crmapi.dto.oportunidad.CambioEtapaRequest;
import com.devsuj.crmapi.dto.oportunidad.HistorialEtapaResponse;
import com.devsuj.crmapi.dto.oportunidad.OportunidadActualizarRequest;
import com.devsuj.crmapi.dto.oportunidad.OportunidadRequest;
import com.devsuj.crmapi.dto.oportunidad.OportunidadResponse;
import com.devsuj.crmapi.exception.RecursoNoEncontradoException;
import com.devsuj.crmapi.exception.ReglaNegocioException;
import com.devsuj.crmapi.mapper.OportunidadMapper;
import com.devsuj.crmapi.model.Cliente;
import com.devsuj.crmapi.model.EtapaOportunidad;
import com.devsuj.crmapi.model.Oportunidad;
import com.devsuj.crmapi.model.OportunidadHistorialEtapa;
import com.devsuj.crmapi.repository.Filtros;
import com.devsuj.crmapi.repository.OportunidadHistorialEtapaRepository;
import com.devsuj.crmapi.repository.OportunidadRepository;
import com.devsuj.crmapi.security.Alcance;
import com.devsuj.crmapi.security.ControlAcceso;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Pipeline de oportunidades (P8). Cada cambio de etapa (incluida la creación y el cierre) se guarda en
 * {@code oportunidad_historial_etapa} en la misma transacción. GANADA solo la pone una venta ligada;
 * una PERDIDA no se reabre: se crea otra con {@code oportunidadAnteriorId}.
 */
@Service
@RequiredArgsConstructor
public class OportunidadService {

    private static final String RECURSO = "esta oportunidad";

    private final OportunidadRepository repository;
    private final OportunidadHistorialEtapaRepository historialRepository;
    private final ClienteService clienteService;
    private final UsuarioService usuarioService;
    private final OportunidadMapper mapper;
    private final ControlAcceso controlAcceso;
    private final Clock clock;

    @Transactional(readOnly = true)
    public PaginaResponse<OportunidadResponse> listar(EtapaOportunidad etapa, Integer usuarioId, Integer clienteId,
            Boolean abiertas, Pageable pageable) {
        Alcance alcance = controlAcceso.alcance();
        controlAcceso.verificarFiltroUsuario(alcance, usuarioId);
        Specification<Oportunidad> filtro = Specification.allOf(
                Filtros.visiblePara(alcance, "usuario"),
                Filtros.igual(etapa, "etapa"),
                Filtros.igual(usuarioId, "usuario", "id"),
                Filtros.igual(clienteId, "cliente", "id"),
                soloAbiertas(abiertas));
        return PaginaResponse.de(repository.findAll(filtro, pageable), mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public OportunidadResponse obtener(Integer id) {
        return mapper.toResponse(buscarVisible(id));
    }

    /** Línea de tiempo en orden cronológico, con los días que estuvo en cada etapa. */
    @Transactional(readOnly = true)
    public List<HistorialEtapaResponse> historial(Integer id) {
        buscarVisible(id);
        List<OportunidadHistorialEtapa> pasos = historialRepository.findByOportunidadIdOrderByFechaAscIdAsc(id);
        LocalDateTime ahora = LocalDateTime.now(clock);
        List<HistorialEtapaResponse> linea = new ArrayList<>();
        for (int i = 0; i < pasos.size(); i++) {
            LocalDateTime fin = i + 1 < pasos.size() ? pasos.get(i + 1).getFecha() : null;
            linea.add(mapper.toResponse(pasos.get(i), fin, ahora));
        }
        return linea;
    }

    @Transactional
    public OportunidadResponse crear(OportunidadRequest request) {
        Cliente cliente = clienteService.buscarOperable(request.clienteId(), "registrar oportunidades");
        Oportunidad oportunidad = new Oportunidad();
        oportunidad.setCliente(cliente);
        oportunidad.setUsuario(usuarioService.responsableParaRegistro(request.usuarioId()));
        oportunidad.setTitulo(request.titulo());
        oportunidad.setMontoEstimado(request.montoEstimado());
        oportunidad.setFechaCierreEstimada(request.fechaCierreEstimada());
        oportunidad.setEtapa(EtapaOportunidad.PROSPECTO);
        if (request.oportunidadAnteriorId() != null) {
            oportunidad.setOportunidadAnterior(anteriorValida(request.oportunidadAnteriorId(), cliente));
        }
        repository.save(oportunidad);
        registrarPaso(oportunidad, null, "Oportunidad creada");
        return mapper.toResponse(oportunidad);
    }

    @Transactional
    public OportunidadResponse actualizar(Integer id, OportunidadActualizarRequest request) {
        Oportunidad oportunidad = buscarModificable(id);
        verificarAbierta(oportunidad);
        oportunidad.setTitulo(request.titulo());
        oportunidad.setMontoEstimado(request.montoEstimado());
        oportunidad.setFechaCierreEstimada(request.fechaCierreEstimada());
        return mapper.toResponse(oportunidad);
    }

    @Transactional
    public OportunidadResponse cambiarEtapa(Integer id, CambioEtapaRequest request) {
        Oportunidad oportunidad = buscarModificable(id);
        verificarAbierta(oportunidad);
        EtapaOportunidad destino = request.etapa();
        if (destino == EtapaOportunidad.GANADA) {
            throw new ReglaNegocioException("Una oportunidad se gana al registrar su venta (POST /api/ventas con oportunidadId).");
        }
        if (!oportunidad.getEtapa().puedeCambiarA(destino)) {
            throw new ReglaNegocioException("La oportunidad ya está en " + destino + ".");
        }
        EtapaOportunidad anterior = oportunidad.getEtapa();
        oportunidad.setEtapa(destino);
        registrarPaso(oportunidad, anterior, request.comentario());
        return mapper.toResponse(oportunidad);
    }

    /** La usa VentaService: valida que la oportunidad se pueda cerrar con una venta del cliente indicado. */
    @Transactional(readOnly = true)
    public Oportunidad buscarParaVenta(Integer id, Cliente cliente) {
        Oportunidad oportunidad = buscar(id);
        if (!oportunidad.getCliente().getId().equals(cliente.getId())) {
            throw new ReglaNegocioException("La oportunidad " + id + " es de otro cliente.");
        }
        verificarAbierta(oportunidad);
        return oportunidad;
    }

    /** La usa VentaService al registrar la venta ligada: GANADA queda en el historial. */
    @Transactional
    public void marcarGanada(Oportunidad oportunidad, Integer ventaId) {
        EtapaOportunidad anterior = oportunidad.getEtapa();
        oportunidad.setEtapa(EtapaOportunidad.GANADA);
        registrarPaso(oportunidad, anterior, "Venta #" + ventaId + " registrada");
    }

    private void registrarPaso(Oportunidad oportunidad, EtapaOportunidad anterior, String comentario) {
        OportunidadHistorialEtapa paso = new OportunidadHistorialEtapa();
        paso.setOportunidad(oportunidad);
        paso.setEtapaAnterior(anterior);
        paso.setEtapaNueva(oportunidad.getEtapa());
        paso.setUsuario(usuarioService.buscar(controlAcceso.usuario().id()));
        paso.setFecha(LocalDateTime.now(clock));
        paso.setComentario(comentario);
        historialRepository.save(paso);
    }

    private Oportunidad anteriorValida(Integer anteriorId, Cliente cliente) {
        Oportunidad anterior = buscar(anteriorId);
        if (!anterior.getCliente().getId().equals(cliente.getId()) || anterior.getEtapa() != EtapaOportunidad.PERDIDA) {
            throw new ReglaNegocioException("La oportunidad anterior debe ser una PERDIDA del mismo cliente.");
        }
        return anterior;
    }

    private static void verificarAbierta(Oportunidad oportunidad) {
        if (oportunidad.getEtapa().esCerrada()) {
            throw new ReglaNegocioException("La oportunidad está " + oportunidad.getEtapa()
                    + " y ya no cambia. Si se retoma, crea una nueva con oportunidadAnteriorId.");
        }
    }

    private static Specification<Oportunidad> soloAbiertas(Boolean abiertas) {
        if (abiertas == null) {
            return Specification.unrestricted();
        }
        List<EtapaOportunidad> cerradas = List.of(EtapaOportunidad.GANADA, EtapaOportunidad.PERDIDA);
        return (root, query, cb) -> {
            Predicate esCerrada = root.get("etapa").in(cerradas);
            return abiertas ? cb.not(esCerrada) : esCerrada;
        };
    }

    private Oportunidad buscarVisible(Integer id) {
        Oportunidad oportunidad = buscar(id);
        controlAcceso.verificarLectura(oportunidad.getUsuario(), oportunidad.getCliente(), RECURSO);
        return oportunidad;
    }

    private Oportunidad buscarModificable(Integer id) {
        Oportunidad oportunidad = buscar(id);
        controlAcceso.verificarModificacion(oportunidad.getUsuario(), RECURSO);
        return oportunidad;
    }

    private Oportunidad buscar(Integer id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("una oportunidad", id));
    }
}
