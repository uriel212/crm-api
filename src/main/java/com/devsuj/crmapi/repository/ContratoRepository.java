package com.devsuj.crmapi.repository;

import com.devsuj.crmapi.model.Contrato;
import com.devsuj.crmapi.model.EstadoContrato;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;

public interface ContratoRepository extends JpaRepository<Contrato, Integer>, JpaSpecificationExecutor<Contrato> {

    @Override
    @EntityGraph(attributePaths = {"cliente", "cliente.vendedor"})
    Page<Contrato> findAll(Specification<Contrato> filtro, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"cliente", "cliente.vendedor"})
    List<Contrato> findAll(Specification<Contrato> filtro);

    boolean existsByContratoAnteriorId(Integer contratoAnteriorId);

    /** VIGENTE con fecha de fin ya pasada (candidatos a VENCIDO, P20). */
    default Specification<Contrato> vencidosAl(LocalDate hoy) {
        return (root, query, cb) -> cb.and(cb.equal(root.get("estado"), EstadoContrato.VIGENTE),
                cb.lessThan(root.get("fechaFin"), hoy));
    }
}
