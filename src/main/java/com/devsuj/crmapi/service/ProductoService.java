package com.devsuj.crmapi.service;

import com.devsuj.crmapi.dto.PaginaResponse;
import com.devsuj.crmapi.dto.catalogo.MovimientoInventarioRequest;
import com.devsuj.crmapi.dto.catalogo.MovimientoInventarioResponse;
import com.devsuj.crmapi.dto.catalogo.ProductoRequest;
import com.devsuj.crmapi.dto.catalogo.ProductoResponse;
import com.devsuj.crmapi.exception.DatosInvalidosException;
import com.devsuj.crmapi.exception.RecursoNoEncontradoException;
import com.devsuj.crmapi.exception.ReglaNegocioException;
import com.devsuj.crmapi.mapper.CatalogoMapper;
import com.devsuj.crmapi.model.MovimientoInventario;
import com.devsuj.crmapi.model.Producto;
import com.devsuj.crmapi.model.TipoMovimientoInventario;
import com.devsuj.crmapi.repository.Filtros;
import com.devsuj.crmapi.repository.MovimientoInventarioRepository;
import com.devsuj.crmapi.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Catálogo. Sin borrado físico (se desactiva). Cambiar el precio no afecta ventas pasadas: cada renglón
 * de venta guarda una copia del precio. El stock solo cambia con movimientos de inventario.
 */
@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository repository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final CategoriaService categoriaService;
    private final InventarioService inventarioService;
    private final CatalogoMapper mapper;

    @Transactional(readOnly = true)
    public PaginaResponse<ProductoResponse> listar(Integer categoriaId, Boolean activo, Boolean canjeables, String q,
            Pageable pageable) {
        Specification<Producto> filtro = Specification.allOf(
                Filtros.igual(categoriaId, "categoria", "id"),
                Filtros.igual(activo, "activo"),
                Filtros.texto(q, "nombre", "descripcion"),
                soloCanjeables(canjeables));
        return PaginaResponse.de(repository.findAll(filtro, pageable), mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> stockBajo() {
        return repository.findConStockBajo().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(Integer id) {
        return mapper.toResponse(buscar(id));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        Producto producto = new Producto();
        mapper.copiar(request, producto);
        producto.setCategoria(categoriaService.buscar(request.categoriaId()));
        producto.setStock(0);
        repository.save(producto);
        int inicial = request.stockInicial() == null ? 0 : request.stockInicial();
        if (inicial > 0) {
            inventarioService.entrada(producto, inicial, "Inventario inicial");
        }
        return mapper.toResponse(producto);
    }

    @Transactional
    public ProductoResponse actualizar(Integer id, ProductoRequest request) {
        Producto producto = buscar(id);
        mapper.copiar(request, producto);
        producto.setCategoria(categoriaService.buscar(request.categoriaId()));
        return mapper.toResponse(producto);
    }

    @Transactional
    public ProductoResponse cambiarEstado(Integer id, boolean activo) {
        Producto producto = buscar(id);
        producto.setActivo(activo);
        return mapper.toResponse(producto);
    }

    /** Ajuste manual (P9): ENTRADA suma, SALIDA resta (nunca deja stock negativo). */
    @Transactional
    public MovimientoInventarioResponse ajustarInventario(Integer id, MovimientoInventarioRequest request) {
        Producto producto = buscar(id);
        MovimientoInventario movimiento = switch (request.tipo()) {
            case ENTRADA -> inventarioService.entrada(producto, request.cantidad(), request.motivo());
            case SALIDA -> inventarioService.salida(producto, request.cantidad(), TipoMovimientoInventario.SALIDA,
                    request.motivo(), null);
            case VENTA, CANJE -> throw new DatosInvalidosException(
                    "Los movimientos VENTA y CANJE los registra el sistema; usa ENTRADA o SALIDA.");
        };
        return mapper.toResponse(movimiento);
    }

    @Transactional(readOnly = true)
    public PaginaResponse<MovimientoInventarioResponse> movimientos(Integer id, Pageable pageable) {
        buscar(id);
        return PaginaResponse.de(movimientoRepository.findByProductoId(id, pageable), mapper::toResponse);
    }

    /** Para ventas y canjes: el producto existe y está activo. */
    @Transactional(readOnly = true)
    public Producto buscarVendible(Integer id) {
        Producto producto = buscar(id);
        if (!producto.isActivo()) {
            throw new ReglaNegocioException("El producto " + producto.getNombre() + " está desactivado.");
        }
        return producto;
    }

    @Transactional(readOnly = true)
    public Producto buscar(Integer id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("un producto", id));
    }

    private static Specification<Producto> soloCanjeables(Boolean canjeables) {
        if (canjeables == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> canjeables
                ? cb.isNotNull(root.get("precioPuntos"))
                : cb.isNull(root.get("precioPuntos"));
    }
}
