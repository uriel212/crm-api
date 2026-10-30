package com.devsuj.crmapi.repository;

import com.devsuj.crmapi.model.Rol;
import com.devsuj.crmapi.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer>, JpaSpecificationExecutor<Usuario> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Integer id);

    @Query("select u.id from Usuario u where u.gerente.id = :gerenteId")
    List<Integer> findIdsDelEquipo(Integer gerenteId);

    long countByGerenteIdAndActivoTrue(Integer gerenteId);

    @Override
    @EntityGraph(attributePaths = "gerente")
    Page<Usuario> findAll(Specification<Usuario> filtro, Pageable pageable);

    List<Usuario> findByRolOrderByNombre(Rol rol);
}
