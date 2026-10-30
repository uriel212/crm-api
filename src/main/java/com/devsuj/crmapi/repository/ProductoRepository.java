package com.devsuj.crmapi.repository;

import com.devsuj.crmapi.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Integer>, JpaSpecificationExecutor<Producto> {

    boolean existsByCategoriaId(Integer categoriaId);

    @Override
    @EntityGraph(attributePaths = "categoria")
    Page<Producto> findAll(Specification<Producto> filtro, Pageable pageable);

    @EntityGraph(attributePaths = "categoria")
    @Query("select p from Producto p where p.activo = true and p.stock <= p.stockMinimo order by p.stock, p.nombre")
    List<Producto> findConStockBajo();

    @Query("select count(p) from Producto p where p.activo = true and p.stock <= p.stockMinimo")
    long contarConStockBajo();

    @EntityGraph(attributePaths = "categoria")
    List<Producto> findByIdIn(Collection<Integer> ids);

    /**
     * Descuento atómico: solo resta si alcanza. Devuelve 0 si no había stock suficiente.
     * Evita vender de más si dos ventas llegan al mismo tiempo (sin columna de versión).
     */
    @Modifying(flushAutomatically = true)
    @Query("update Producto p set p.stock = p.stock - :cantidad where p.id = :id and p.stock >= :cantidad")
    int descontarStock(Integer id, int cantidad);

    @Modifying(flushAutomatically = true)
    @Query("update Producto p set p.stock = p.stock + :cantidad where p.id = :id")
    int sumarStock(Integer id, int cantidad);
}
