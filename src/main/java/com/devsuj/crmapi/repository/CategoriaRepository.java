package com.devsuj.crmapi.repository;

import com.devsuj.crmapi.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    boolean existsByNombre(String nombre);

    boolean existsByNombreAndIdNot(String nombre, Integer id);

    List<Categoria> findAllByOrderByNombreAsc();
}
