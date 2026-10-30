package com.devsuj.crmapi.repository;

import com.devsuj.crmapi.model.Promocion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface PromocionRepository extends JpaRepository<Promocion, Integer>, JpaSpecificationExecutor<Promocion> {

    /** Promociones activas cuyas fechas incluyen el día indicado, con productos y campaña (evita N+1). */
    @Query("""
            select distinct p from Promocion p
            left join fetch p.productos
            left join fetch p.campana
            where p.activa = true and :dia between p.fechaInicio and p.fechaFin
            """)
    List<Promocion> findActivasEn(LocalDate dia);

    @Override
    @EntityGraph(attributePaths = "campana")
    Page<Promocion> findAll(Specification<Promocion> filtro, Pageable pageable);

    /** Se usó en alguna venta (sobre el total o en un renglón): ya no se puede borrar. */
    @Query("""
            select (count(v) > 0) from Venta v where v.promocion.id = :id
               or exists (select 1 from DetalleVenta d where d.venta = v and d.promocion.id = :id)
            """)
    boolean seUsoEnVentas(Integer id);
}
