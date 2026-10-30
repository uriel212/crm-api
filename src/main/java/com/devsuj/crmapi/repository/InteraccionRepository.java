package com.devsuj.crmapi.repository;

import com.devsuj.crmapi.model.EstadoInteraccion;
import com.devsuj.crmapi.model.Interaccion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface InteraccionRepository extends JpaRepository<Interaccion, Integer>, JpaSpecificationExecutor<Interaccion> {

    @Override
    @EntityGraph(attributePaths = {"cliente", "usuario"})
    Page<Interaccion> findAll(Specification<Interaccion> filtro, Pageable pageable);

    /** Agenda: actividades PROGRAMADA del usuario hasta {@code hasta} (incluye las vencidas). */
    @EntityGraph(attributePaths = {"cliente", "usuario"})
    List<Interaccion> findByUsuarioIdAndEstadoAndFechaBeforeOrderByFechaAsc(
            Integer usuarioId, EstadoInteraccion estado, LocalDateTime hasta);
}
