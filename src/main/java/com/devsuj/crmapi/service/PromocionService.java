package com.devsuj.crmapi.service;

import com.devsuj.crmapi.dto.PaginaResponse;
import com.devsuj.crmapi.dto.marketing.PromocionRequest;
import com.devsuj.crmapi.dto.marketing.PromocionResponse;
import com.devsuj.crmapi.exception.DatosInvalidosException;
import com.devsuj.crmapi.exception.RecursoNoEncontradoException;
import com.devsuj.crmapi.exception.ReglaNegocioException;
import com.devsuj.crmapi.mapper.MarketingMapper;
import com.devsuj.crmapi.model.AlcancePromocion;
import com.devsuj.crmapi.model.Producto;
import com.devsuj.crmapi.model.Promocion;
import com.devsuj.crmapi.model.TipoPromocion;
import com.devsuj.crmapi.repository.CampanaRepository;
import com.devsuj.crmapi.repository.Filtros;
import com.devsuj.crmapi.repository.ProductoRepository;
import com.devsuj.crmapi.repository.PromocionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Promociones (P17, P18). Las reglas de cálculo viven en {@link CalculadoraVenta}; aquí solo se administran.
 * Una promoción usada en ventas no se borra (409): se desactiva.
 */
@Service
@RequiredArgsConstructor
public class PromocionService {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private final PromocionRepository repository;
    private final CampanaRepository campanaRepository;
    private final ProductoRepository productoRepository;
    private final MarketingMapper mapper;
    private final Clock clock;

    @Transactional(readOnly = true)
    public PaginaResponse<PromocionResponse> listar(Boolean activa, AlcancePromocion alcance, Integer campanaId,
            Integer productoId, Pageable pageable) {
        Specification<Promocion> filtro = Specification.allOf(
                Filtros.igual(activa, "activa"),
                Filtros.igual(alcance, "alcance"),
                Filtros.igual(campanaId, "campana", "id"),
                incluyeProducto(productoId));
        LocalDate hoy = hoy();
        return PaginaResponse.de(repository.findAll(filtro, pageable), p -> mapper.toResponse(p, hoy));
    }

    /** Las que aplicarían hoy en una venta, ordenadas por id. */
    @Transactional(readOnly = true)
    public List<PromocionResponse> vigentes() {
        LocalDate hoy = hoy();
        return repository.findActivasEn(hoy).stream()
                .filter(p -> p.estaVigente(hoy))
                .sorted(Comparator.comparing(Promocion::getId))
                .map(p -> mapper.toResponse(p, hoy))
                .toList();
    }

    @Transactional(readOnly = true)
    public PromocionResponse obtener(Integer id) {
        return mapper.toResponse(buscar(id), hoy());
    }

    @Transactional
    public PromocionResponse crear(PromocionRequest request) {
        Promocion promocion = new Promocion();
        copiar(request, promocion);
        return mapper.toResponse(repository.save(promocion), hoy());
    }

    /** Editar no cambia ventas pasadas: el descuento quedó congelado en cada venta. */
    @Transactional
    public PromocionResponse actualizar(Integer id, PromocionRequest request) {
        Promocion promocion = buscar(id);
        copiar(request, promocion);
        return mapper.toResponse(promocion, hoy());
    }

    @Transactional
    public PromocionResponse cambiarEstado(Integer id, boolean activa) {
        Promocion promocion = buscar(id);
        promocion.setActiva(activa);
        return mapper.toResponse(promocion, hoy());
    }

    @Transactional
    public void eliminar(Integer id) {
        Promocion promocion = buscar(id);
        if (repository.seUsoEnVentas(id)) {
            throw new ReglaNegocioException("La promoción " + promocion.getNombre()
                    + " ya se aplicó en ventas: desactívala en lugar de borrarla.");
        }
        repository.delete(promocion);
    }

    private void copiar(PromocionRequest request, Promocion promocion) {
        validarCoherencia(request);
        promocion.setCampana(request.campanaId() == null ? null : campanaRepository.findById(request.campanaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("una campaña", request.campanaId())));
        promocion.setNombre(request.nombre());
        promocion.setTipo(request.tipo());
        promocion.setAlcance(request.alcance());
        promocion.setValor(request.valor());
        promocion.setMontoMinimo(request.alcance() == AlcancePromocion.VENTA ? request.montoMinimo() : null);
        promocion.setFechaInicio(request.fechaInicio());
        promocion.setFechaFin(request.fechaFin());
        promocion.getProductos().clear();
        promocion.getProductos().addAll(productos(request));
    }

    private static void validarCoherencia(PromocionRequest request) {
        if (request.tipo() == TipoPromocion.PORCENTAJE && request.valor().compareTo(CIEN) > 0) {
            throw new DatosInvalidosException("Un porcentaje no puede pasar de 100.");
        }
        boolean conProductos = request.productoIds() != null && !request.productoIds().isEmpty();
        if (request.alcance() == AlcancePromocion.PRODUCTO && !conProductos) {
            throw new DatosInvalidosException("Una promoción de alcance PRODUCTO necesita al menos un producto.");
        }
        if (request.alcance() == AlcancePromocion.VENTA && conProductos) {
            throw new DatosInvalidosException("Una promoción de alcance VENTA se aplica al total: no lleva productos.");
        }
        if (request.alcance() == AlcancePromocion.PRODUCTO && request.montoMinimo() != null) {
            throw new DatosInvalidosException("El monto mínimo solo aplica a promociones de alcance VENTA.");
        }
    }

    private Set<Producto> productos(PromocionRequest request) {
        if (request.productoIds() == null || request.productoIds().isEmpty()) {
            return Set.of();
        }
        List<Producto> encontrados = productoRepository.findAllById(request.productoIds());
        if (encontrados.size() != request.productoIds().size()) {
            Set<Integer> ids = new HashSet<>(encontrados.stream().map(Producto::getId).toList());
            Integer faltante = request.productoIds().stream().filter(i -> !ids.contains(i)).findFirst().orElseThrow();
            throw new RecursoNoEncontradoException("un producto", faltante);
        }
        return new HashSet<>(encontrados);
    }

    private static Specification<Promocion> incluyeProducto(Integer productoId) {
        if (productoId == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.equal(root.join("productos").get("id"), productoId);
    }

    private Promocion buscar(Integer id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("una promoción", id));
    }

    private LocalDate hoy() {
        return LocalDate.now(clock);
    }
}
