package com.devsuj.crmapi.repository;

import com.devsuj.crmapi.model.CampanaCliente;
import com.devsuj.crmapi.model.CampanaClienteId;
import com.devsuj.crmapi.model.ResultadoCampana;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface CampanaClienteRepository extends JpaRepository<CampanaCliente, CampanaClienteId> {

    @EntityGraph(attributePaths = {"cliente", "cliente.vendedor"})
    Page<CampanaCliente> findByCampanaId(Integer campanaId, Pageable pageable);

    @EntityGraph(attributePaths = {"cliente", "cliente.vendedor"})
    Page<CampanaCliente> findByCampanaIdAndResultado(Integer campanaId, ResultadoCampana resultado, Pageable pageable);

    @Query("select cc.cliente.id from CampanaCliente cc where cc.campana.id = :campanaId")
    Set<Integer> findClienteIdsDe(Integer campanaId);

    /** [campanaId, resultado, cantidad] para métricas y conteos de varias campañas en una consulta. */
    @Query("""
            select cc.campana.id, cc.resultado, count(cc) from CampanaCliente cc
            where cc.campana.id in :campanaIds group by cc.campana.id, cc.resultado
            """)
    List<Object[]> contarPorResultado(Collection<Integer> campanaIds);
}
