package com.devsuj.crmapi.repository;

import com.devsuj.crmapi.model.OportunidadHistorialEtapa;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OportunidadHistorialEtapaRepository extends JpaRepository<OportunidadHistorialEtapa, Integer> {

    @EntityGraph(attributePaths = "usuario")
    List<OportunidadHistorialEtapa> findByOportunidadIdOrderByFechaAscIdAsc(Integer oportunidadId);
}
