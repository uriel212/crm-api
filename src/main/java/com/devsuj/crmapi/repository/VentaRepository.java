package com.devsuj.crmapi.repository;

import com.devsuj.crmapi.model.Venta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface VentaRepository extends JpaRepository<Venta, Integer>, JpaSpecificationExecutor<Venta> {

    boolean existsByClienteId(Integer clienteId);

    boolean existsByOportunidadId(Integer oportunidadId);

    @Override
    @EntityGraph(attributePaths = {"cliente", "usuario"})
    Page<Venta> findAll(Specification<Venta> filtro, Pageable pageable);

    /** Venta con renglones y productos en una sola consulta (evita N+1). */
    @Query("""
            select distinct v from Venta v
            join fetch v.cliente c left join fetch c.vendedor
            join fetch v.usuario
            left join fetch v.promocion
            left join fetch v.detalles d
            left join fetch d.producto
            left join fetch d.promocion
            where v.id = :id
            """)
    Optional<Venta> findConDetallesById(Integer id);

    /** Total vendido por vendedor y mes en [desde, hasta): una fila por (usuarioId, anio, mes). Para el avance de metas. */
    @Query("""
            select new com.devsuj.crmapi.repository.VentasDelPeriodo(v.usuario.id, extract(year from v.fecha),
                   extract(month from v.fecha), count(v), sum(v.total))
            from Venta v
            where v.usuario.id in :usuarioIds and v.fecha >= :desde and v.fecha < :hasta
            group by v.usuario.id, extract(year from v.fecha), extract(month from v.fecha)
            """)
    List<VentasDelPeriodo> sumarPorVendedorYMes(Collection<Integer> usuarioIds, LocalDateTime desde, LocalDateTime hasta);
}
