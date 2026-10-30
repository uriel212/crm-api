package com.devsuj.crmapi.repository;

import com.devsuj.crmapi.model.Cliente;
import com.devsuj.crmapi.model.EstadoCliente;
import com.devsuj.crmapi.model.Usuario;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer>, JpaSpecificationExecutor<Cliente> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Integer id);

    long countByVendedorId(Integer vendedorId);

    @Override
    @EntityGraph(attributePaths = "vendedor")
    Page<Cliente> findAll(Specification<Cliente> filtro, Pageable pageable);

    @EntityGraph(attributePaths = "vendedor")
    List<Cliente> findByIdIn(Collection<Integer> ids);

    /** Bloquea la fila del cliente hasta el fin de la transacción (evita dos canjes simultáneos). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cliente c where c.id = :id")
    Optional<Cliente> bloquearPorId(Integer id);

    @Modifying(flushAutomatically = true)
    @Query("update Cliente c set c.vendedor = :nuevo, c.actualizadoEn = :ahora where c.vendedor.id = :anteriorId")
    int reasignarTodos(Integer anteriorId, Usuario nuevo, LocalDateTime ahora);

    List<Cliente> findByEstado(EstadoCliente estado);

    List<Cliente> findByEstadoNot(EstadoCliente estado);

    /** Clientes que ya compraron y cuya última compra fue antes de {@code limite} (segmento SIN_COMPRA_90_DIAS). */
    @Query("""
            select c from Cliente c
            where c.estado <> com.devsuj.crmapi.model.EstadoCliente.PROSPECTO
              and (select max(v.fecha) from Venta v where v.cliente = c) < :limite
            """)
    List<Cliente> findSinCompraDesde(LocalDateTime limite);
}
