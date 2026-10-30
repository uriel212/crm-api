package com.devsuj.crmapi.service;

import com.devsuj.crmapi.dto.PaginaResponse;
import com.devsuj.crmapi.dto.marketing.CampanaRequest;
import com.devsuj.crmapi.dto.marketing.CampanaResponse;
import com.devsuj.crmapi.dto.marketing.DestinatarioResponse;
import com.devsuj.crmapi.dto.marketing.GeneracionDestinatariosResponse;
import com.devsuj.crmapi.dto.marketing.MetricasCampanaResponse;
import com.devsuj.crmapi.exception.RecursoNoEncontradoException;
import com.devsuj.crmapi.exception.ReglaNegocioException;
import com.devsuj.crmapi.mapper.MarketingMapper;
import com.devsuj.crmapi.model.Campana;
import com.devsuj.crmapi.model.CampanaCliente;
import com.devsuj.crmapi.model.CampanaClienteId;
import com.devsuj.crmapi.model.CanalCampana;
import com.devsuj.crmapi.model.Cliente;
import com.devsuj.crmapi.model.EstadoCampana;
import com.devsuj.crmapi.model.EstadoCliente;
import com.devsuj.crmapi.model.ResultadoCampana;
import com.devsuj.crmapi.repository.CampanaClienteRepository;
import com.devsuj.crmapi.repository.CampanaRepository;
import com.devsuj.crmapi.repository.ClienteRepository;
import com.devsuj.crmapi.repository.Filtros;
import com.devsuj.crmapi.security.ControlAcceso;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Campañas de marketing (P15, P16): no se borran (se cancelan), no se envían mensajes reales; se registran
 * destinatarios y su resultado. Los destinatarios se generan del segmento de forma idempotente.
 */
@Service
@RequiredArgsConstructor
public class CampanaService {

    private static final int DIAS_SIN_COMPRA = 90;

    private final CampanaRepository repository;
    private final CampanaClienteRepository destinatarioRepository;
    private final ClienteRepository clienteRepository;
    private final ClienteService clienteService;
    private final MarketingMapper mapper;
    private final ControlAcceso controlAcceso;
    private final Clock clock;

    @Transactional(readOnly = true)
    public PaginaResponse<CampanaResponse> listar(EstadoCampana estado, CanalCampana canal, String q, Pageable pageable) {
        Specification<Campana> filtro = Specification.allOf(
                Filtros.igual(estado, "estado"),
                Filtros.igual(canal, "canal"),
                Filtros.texto(q, "nombre", "asunto"));
        Page<Campana> pagina = repository.findAll(filtro, pageable);
        Map<Integer, Map<ResultadoCampana, Long>> conteos = conteos(pagina.map(Campana::getId).getContent());
        return PaginaResponse.de(pagina, c -> mapper.toResponse(c, total(conteos.get(c.getId()))));
    }

    @Transactional(readOnly = true)
    public CampanaResponse obtener(Integer id) {
        return conTotal(buscar(id));
    }

    @Transactional
    public CampanaResponse crear(CampanaRequest request) {
        Campana campana = new Campana();
        mapper.copiar(request, campana);
        campana.setEstado(EstadoCampana.PLANEADA);
        return mapper.toResponse(repository.save(campana), 0);
    }

    @Transactional
    public CampanaResponse actualizar(Integer id, CampanaRequest request) {
        Campana campana = buscarAbierta(id, "editar");
        mapper.copiar(request, campana);
        return conTotal(campana);
    }

    /** Cambio manual de estado (P15): cualquier estado, para corregir errores; nunca se borra. */
    @Transactional
    public CampanaResponse cambiarEstado(Integer id, EstadoCampana estado) {
        Campana campana = buscar(id);
        if (campana.getEstado() == estado) {
            throw new ReglaNegocioException("La campaña ya está " + estado + ".");
        }
        campana.setEstado(estado);
        return conTotal(campana);
    }

    @Transactional(readOnly = true)
    public PaginaResponse<DestinatarioResponse> destinatarios(Integer id, ResultadoCampana resultado, Pageable pageable) {
        buscar(id);
        Page<CampanaCliente> pagina = resultado == null
                ? destinatarioRepository.findByCampanaId(id, pageable)
                : destinatarioRepository.findByCampanaIdAndResultado(id, resultado, pageable);
        return PaginaResponse.de(pagina, mapper::toResponse);
    }

    /** Agrega los clientes del segmento que aún no son destinatarios (idempotente). Solo PLANEADA o ACTIVA. */
    @Transactional
    public GeneracionDestinatariosResponse generarDestinatarios(Integer id) {
        Campana campana = buscarAbierta(id, "generar destinatarios");
        Set<Integer> actuales = destinatarioRepository.findClienteIdsDe(id);
        List<CampanaCliente> nuevos = clientesDelSegmento(campana).stream()
                .filter(c -> !actuales.contains(c.getId()))
                .map(c -> new CampanaCliente(campana, c))
                .toList();
        destinatarioRepository.saveAll(nuevos);
        return new GeneracionDestinatariosResponse(nuevos.size(), actuales.size() + nuevos.size());
    }

    @Transactional
    public DestinatarioResponse agregarDestinatario(Integer id, Integer clienteId) {
        Campana campana = buscarAbierta(id, "agregar destinatarios");
        Cliente cliente = clienteService.buscar(clienteId);
        if (destinatarioRepository.existsById(new CampanaClienteId(id, clienteId))) {
            throw new ReglaNegocioException(cliente.getNombre() + " ya es destinatario de esta campaña.");
        }
        return mapper.toResponse(destinatarioRepository.save(new CampanaCliente(campana, cliente)));
    }

    /** S15: solo mientras la campaña está PLANEADA (después ya se "envió"). */
    @Transactional
    public void quitarDestinatario(Integer id, Integer clienteId) {
        Campana campana = buscar(id);
        if (campana.getEstado() != EstadoCampana.PLANEADA) {
            throw new ReglaNegocioException("Solo se quitan destinatarios mientras la campaña está PLANEADA.");
        }
        destinatarioRepository.delete(buscarDestinatario(id, clienteId));
    }

    /**
     * Registra cómo respondió un destinatario (S15: lo hace quien gestiona al cliente). Solo avanza
     * ENVIADO → ABIERTO → RESPONDIO, y solo con la campaña ACTIVA o FINALIZADA (S27).
     */
    @Transactional
    public DestinatarioResponse cambiarResultado(Integer id, Integer clienteId, ResultadoCampana resultado) {
        Campana campana = buscar(id);
        if (campana.getEstado() != EstadoCampana.ACTIVA && campana.getEstado() != EstadoCampana.FINALIZADA) {
            throw new ReglaNegocioException("Los resultados se registran con la campaña ACTIVA o FINALIZADA; está "
                    + campana.getEstado() + ".");
        }
        CampanaCliente destinatario = buscarDestinatario(id, clienteId);
        controlAcceso.verificarGestionCliente(destinatario.getCliente());
        if (!destinatario.getResultado().puedeCambiarA(resultado)) {
            throw new ReglaNegocioException("El resultado solo avanza (ENVIADO → ABIERTO → RESPONDIO); ya está en "
                    + destinatario.getResultado() + ".");
        }
        destinatario.setResultado(resultado);
        return mapper.toResponse(destinatario);
    }

    @Transactional(readOnly = true)
    public MetricasCampanaResponse metricas(Integer id) {
        Campana campana = buscar(id);
        return mapper.toMetricas(campana, conteos(List.of(id)).getOrDefault(id, Map.of()));
    }

    /** Efectividad de todas las campañas, más reciente primero (reporte 9.8). */
    @Transactional(readOnly = true)
    public List<MetricasCampanaResponse> metricasDeTodas() {
        List<Campana> campanas = repository.findAll(Sort.by(Sort.Direction.DESC, "fechaInicio", "id"));
        Map<Integer, Map<ResultadoCampana, Long>> conteos = conteos(campanas.stream().map(Campana::getId).toList());
        return campanas.stream().map(c -> mapper.toMetricas(c, conteos.getOrDefault(c.getId(), Map.of()))).toList();
    }

    /** S26: TODOS = clientes que no están INACTIVO. SIN_COMPRA_90_DIAS según P14. */
    private List<Cliente> clientesDelSegmento(Campana campana) {
        return switch (campana.getSegmento()) {
            case TODOS -> clienteRepository.findByEstadoNot(EstadoCliente.INACTIVO);
            case PROSPECTOS -> clienteRepository.findByEstado(EstadoCliente.PROSPECTO);
            case ACTIVOS -> clienteRepository.findByEstado(EstadoCliente.ACTIVO);
            case INACTIVOS -> clienteRepository.findByEstado(EstadoCliente.INACTIVO);
            case SIN_COMPRA_90_DIAS -> clienteRepository.findSinCompraDesde(LocalDateTime.now(clock).minusDays(DIAS_SIN_COMPRA));
        };
    }

    private Map<Integer, Map<ResultadoCampana, Long>> conteos(Collection<Integer> campanaIds) {
        Map<Integer, Map<ResultadoCampana, Long>> conteos = new HashMap<>();
        if (campanaIds.isEmpty()) {
            return conteos;
        }
        for (Object[] fila : destinatarioRepository.contarPorResultado(campanaIds)) {
            conteos.computeIfAbsent((Integer) fila[0], k -> new EnumMap<>(ResultadoCampana.class))
                    .put((ResultadoCampana) fila[1], ((Number) fila[2]).longValue());
        }
        return conteos;
    }

    private static long total(Map<ResultadoCampana, Long> conteo) {
        return conteo == null ? 0 : conteo.values().stream().mapToLong(Long::longValue).sum();
    }

    private CampanaResponse conTotal(Campana campana) {
        return mapper.toResponse(campana, total(conteos(List.of(campana.getId())).get(campana.getId())));
    }

    private Campana buscarAbierta(Integer id, String accion) {
        Campana campana = buscar(id);
        if (campana.getEstado().estaCerrada()) {
            throw new ReglaNegocioException("La campaña está " + campana.getEstado() + ": no se puede " + accion
                    + ". Si fue un error, cambia su estado primero.");
        }
        return campana;
    }

    private CampanaCliente buscarDestinatario(Integer campanaId, Integer clienteId) {
        return destinatarioRepository.findById(new CampanaClienteId(campanaId, clienteId))
                .orElseThrow(() -> new RecursoNoEncontradoException("el destinatario (cliente) en esta campaña", clienteId));
    }

    private Campana buscar(Integer id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("una campaña", id));
    }
}
