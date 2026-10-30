package com.devsuj.crmapi.repository;

import com.devsuj.crmapi.model.MovimientoInventario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Integer> {

    @EntityGraph(attributePaths = {"producto", "usuario", "venta"})
    Page<MovimientoInventario> findByProductoId(Integer productoId, Pageable pageable);
}
