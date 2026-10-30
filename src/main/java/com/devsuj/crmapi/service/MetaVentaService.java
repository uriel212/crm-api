package com.devsuj.crmapi.service;

import com.devsuj.crmapi.dto.PaginaResponse;
import com.devsuj.crmapi.dto.meta.MetaVentaRequest;
import com.devsuj.crmapi.dto.meta.MetaVentaResponse;
import com.devsuj.crmapi.exception.AccesoDenegadoException;
import com.devsuj.crmapi.exception.RecursoNoEncontradoException;
import com.devsuj.crmapi.exception.ReglaNegocioException;
import com.devsuj.crmapi.mapper.MetaVentaMapper;
import com.devsuj.crmapi.model.MetaVenta;
import com.devsuj.crmapi.model.Rol;
import com.devsuj.crmapi.model.Usuario;
import com.devsuj.crmapi.repository.Filtros;
import com.devsuj.crmapi.repository.MetaVentaRepository;
import com.devsuj.crmapi.repository.VentaRepository;
import com.devsuj.crmapi.repository.VentasDelPeriodo;
import com.devsuj.crmapi.security.Alcance;
import com.devsuj.crmapi.security.ControlAcceso;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Metas mensuales de venta (P24: solo VENDEDOR activos). Una por vendedor y mes. El avance suma el total de
 * todas las ventas del vendedor en ese mes (P23). VENDEDOR ve las suyas; GERENTE las de su equipo; ADMIN todas.
 */
@Service
@RequiredArgsConstructor
public class MetaVentaService {

    private final MetaVentaRepository repository;
    private final VentaRepository ventaRepository;
    private final UsuarioService usuarioService;
    private final MetaVentaMapper mapper;
    private final ControlAcceso controlAcceso;

    @Transactional(readOnly = true)
    public PaginaResponse<MetaVentaResponse> listar(Integer usuarioId, Integer anio, Integer mes, Pageable pageable) {
        Alcance alcance = controlAcceso.alcance();
        controlAcceso.verificarFiltroUsuario(alcance, usuarioId);
        Specification<MetaVenta> filtro = Specification.allOf(
                delAlcance(alcance),
                Filtros.igual(usuarioId, "usuario", "id"),
                Filtros.igual(anio, "anio"),
                Filtros.igual(mes, "mes"));
        Page<MetaVenta> pagina = repository.findAll(filtro, pageable);
        Map<String, VentasDelPeriodo> ventas = ventasDe(pagina.getContent());
        return PaginaResponse.de(pagina, m -> mapper.toResponse(m, ventas.get(clave(m.getUsuario().getId(), m.getAnio(), m.getMes()))));
    }

    @Transactional(readOnly = true)
    public MetaVentaResponse obtener(Integer id) {
        MetaVenta meta = buscar(id);
        if (!controlAcceso.alcance().incluye(meta.getUsuario().getId())) {
            throw new AccesoDenegadoException("No tienes acceso a esta meta.");
        }
        return conAvance(meta);
    }

    @Transactional
    public MetaVentaResponse crear(MetaVentaRequest request) {
        Usuario vendedor = vendedorValido(request.usuarioId());
        if (repository.existsByUsuarioIdAndAnioAndMes(vendedor.getId(), request.anio(), request.mes())) {
            throw duplicada(vendedor, request);
        }
        MetaVenta meta = new MetaVenta();
        copiar(request, vendedor, meta);
        repository.save(meta);
        return conAvance(meta);
    }

    @Transactional
    public MetaVentaResponse actualizar(Integer id, MetaVentaRequest request) {
        MetaVenta meta = buscarGestionable(id);
        Usuario vendedor = vendedorValido(request.usuarioId());
        if (repository.existsByUsuarioIdAndAnioAndMesAndIdNot(vendedor.getId(), request.anio(), request.mes(), id)) {
            throw duplicada(vendedor, request);
        }
        copiar(request, vendedor, meta);
        return conAvance(meta);
    }

    @Transactional
    public void eliminar(Integer id) {
        repository.delete(buscarGestionable(id));
    }

    private MetaVentaResponse conAvance(MetaVenta meta) {
        return mapper.toResponse(meta, ventasDe(List.of(meta)).get(clave(meta.getUsuario().getId(), meta.getAnio(), meta.getMes())));
    }

    /** Una sola consulta agrupada para todas las metas de la página (evita N+1). */
    private Map<String, VentasDelPeriodo> ventasDe(List<MetaVenta> metas) {
        if (metas.isEmpty()) {
            return Map.of();
        }
        Set<Integer> usuarios = metas.stream().map(m -> m.getUsuario().getId()).collect(Collectors.toSet());
        YearMonth primero = metas.stream().map(MetaVentaService::periodo).min(Comparator.naturalOrder()).orElseThrow();
        YearMonth ultimo = metas.stream().map(MetaVentaService::periodo).max(Comparator.naturalOrder()).orElseThrow();
        LocalDateTime desde = primero.atDay(1).atStartOfDay();
        LocalDateTime hasta = ultimo.plusMonths(1).atDay(1).atStartOfDay();
        return ventaRepository.sumarPorVendedorYMes(usuarios, desde, hasta).stream()
                .collect(Collectors.toMap(v -> clave(v.usuarioId(), v.anio(), v.mes()), Function.identity()));
    }

    /** P24: la meta es de un VENDEDOR activo; un GERENTE solo asigna metas a su equipo. */
    private Usuario vendedorValido(Integer usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        if (!usuario.tieneRol(Rol.VENDEDOR)) {
            throw new ReglaNegocioException("Solo los VENDEDOR tienen metas; " + usuario.getNombre() + " es "
                    + usuario.getRol() + ".");
        }
        if (!controlAcceso.alcance().incluye(usuarioId)) {
            throw new AccesoDenegadoException("Solo puedes asignar metas a vendedores de tu equipo.");
        }
        return usuario;
    }

    private MetaVenta buscarGestionable(Integer id) {
        MetaVenta meta = buscar(id);
        controlAcceso.verificarModificacion(meta.getUsuario(), "esta meta");
        return meta;
    }

    private MetaVenta buscar(Integer id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("una meta", id));
    }

    private static void copiar(MetaVentaRequest request, Usuario vendedor, MetaVenta meta) {
        meta.setUsuario(vendedor);
        meta.setAnio(request.anio());
        meta.setMes(request.mes());
        meta.setMontoObjetivo(request.montoObjetivo());
    }

    private static ReglaNegocioException duplicada(Usuario vendedor, MetaVentaRequest request) {
        return new ReglaNegocioException(vendedor.getNombre() + " ya tiene una meta para " + request.mes() + "/"
                + request.anio() + ". Edítala en lugar de crear otra.");
    }

    private static Specification<MetaVenta> delAlcance(Alcance alcance) {
        if (alcance.todos()) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> root.get("usuario").get("id").in(alcance.idsParaConsulta());
    }

    private static YearMonth periodo(MetaVenta meta) {
        return YearMonth.of(meta.getAnio(), meta.getMes());
    }

    private static String clave(Integer usuarioId, int anio, int mes) {
        return usuarioId + "-" + anio + "-" + mes;
    }
}
