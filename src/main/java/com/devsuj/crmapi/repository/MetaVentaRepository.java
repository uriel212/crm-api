package com.devsuj.crmapi.repository;

import com.devsuj.crmapi.model.MetaVenta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface MetaVentaRepository extends JpaRepository<MetaVenta, Integer>, JpaSpecificationExecutor<MetaVenta> {

    boolean existsByUsuarioIdAndAnioAndMes(Integer usuarioId, int anio, int mes);

    boolean existsByUsuarioIdAndAnioAndMesAndIdNot(Integer usuarioId, int anio, int mes, Integer id);

    @Override
    @EntityGraph(attributePaths = "usuario")
    Page<MetaVenta> findAll(Specification<MetaVenta> filtro, Pageable pageable);

    List<MetaVenta> findByAnioAndMes(int anio, int mes);
}
