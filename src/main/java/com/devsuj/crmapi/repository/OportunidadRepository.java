package com.devsuj.crmapi.repository;

import com.devsuj.crmapi.model.Oportunidad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OportunidadRepository extends JpaRepository<Oportunidad, Integer>, JpaSpecificationExecutor<Oportunidad> {

    @Override
    @EntityGraph(attributePaths = {"cliente", "usuario"})
    Page<Oportunidad> findAll(Specification<Oportunidad> filtro, Pageable pageable);
}
