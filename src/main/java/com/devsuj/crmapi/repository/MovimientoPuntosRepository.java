package com.devsuj.crmapi.repository;

import com.devsuj.crmapi.model.MovimientoPuntos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MovimientoPuntosRepository extends JpaRepository<MovimientoPuntos, Integer> {

    boolean existsByVentaId(Integer ventaId);

    Optional<MovimientoPuntos> findByVentaId(Integer ventaId);

    @EntityGraph(attributePaths = {"producto", "usuario"})
    Page<MovimientoPuntos> findByClienteId(Integer clienteId, Pageable pageable);

    /** [ganados, canjeados] del cliente en una sola consulta. */
    @Query("""
            select new com.devsuj.crmapi.repository.TotalesPuntos(
                coalesce(sum(case when m.tipo = com.devsuj.crmapi.model.TipoMovimientoPuntos.GANADO then m.puntos else 0 end), 0),
                coalesce(sum(case when m.tipo = com.devsuj.crmapi.model.TipoMovimientoPuntos.CANJEADO then m.puntos else 0 end), 0))
            from MovimientoPuntos m where m.cliente.id = :clienteId
            """)
    TotalesPuntos totalesDe(Integer clienteId);
}
