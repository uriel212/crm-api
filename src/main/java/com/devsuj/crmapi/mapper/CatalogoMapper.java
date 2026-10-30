package com.devsuj.crmapi.mapper;

import com.devsuj.crmapi.dto.catalogo.CategoriaRequest;
import com.devsuj.crmapi.dto.catalogo.CategoriaResponse;
import com.devsuj.crmapi.dto.catalogo.MovimientoInventarioResponse;
import com.devsuj.crmapi.dto.catalogo.ProductoRequest;
import com.devsuj.crmapi.dto.catalogo.ProductoResponse;
import com.devsuj.crmapi.model.Categoria;
import com.devsuj.crmapi.model.MovimientoInventario;
import com.devsuj.crmapi.model.Producto;
import org.springframework.stereotype.Component;

@Component
public class CatalogoMapper {

    public CategoriaResponse toResponse(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNombre(), categoria.getDescripcion());
    }

    public void copiar(CategoriaRequest request, Categoria categoria) {
        categoria.setNombre(request.nombre());
        categoria.setDescripcion(request.descripcion());
    }

    public ProductoResponse toResponse(Producto p) {
        return new ProductoResponse(p.getId(), p.getCategoria().getId(), p.getCategoria().getNombre(), p.getNombre(),
                p.getDescripcion(), p.getPrecio(), p.getPrecioPuntos(), p.getStock(), p.getStockMinimo(), p.isActivo(),
                p.tieneStockBajo(), p.esCanjeable());
    }

    /** Copia los datos editables; la categoría y el stock tienen reglas propias en el service. */
    public void copiar(ProductoRequest request, Producto producto) {
        producto.setNombre(request.nombre());
        producto.setDescripcion(request.descripcion());
        producto.setPrecio(request.precio());
        producto.setPrecioPuntos(request.precioPuntos());
        producto.setStockMinimo(request.stockMinimo());
    }

    public MovimientoInventarioResponse toResponse(MovimientoInventario m) {
        return new MovimientoInventarioResponse(m.getId(), m.getProducto().getId(), m.getProducto().getNombre(),
                m.getTipo(), m.getCantidad(), m.getStockResultante(), m.getMotivo(),
                m.getUsuario() == null ? null : m.getUsuario().getId(),
                m.getUsuario() == null ? null : m.getUsuario().getNombre(),
                m.getVenta() == null ? null : m.getVenta().getId(), m.getFecha());
    }
}
