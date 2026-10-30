package com.devsuj.crmapi.repository;

import com.devsuj.crmapi.model.TicketSoporte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TicketSoporteRepository extends JpaRepository<TicketSoporte, Integer>, JpaSpecificationExecutor<TicketSoporte> {

    @Override
    @EntityGraph(attributePaths = {"cliente", "usuario"})
    Page<TicketSoporte> findAll(Specification<TicketSoporte> filtro, Pageable pageable);
}
