package com.devsuj.crmapi.service;

import com.devsuj.crmapi.dto.catalogo.CategoriaRequest;
import com.devsuj.crmapi.dto.catalogo.CategoriaResponse;
import com.devsuj.crmapi.exception.RecursoNoEncontradoException;
import com.devsuj.crmapi.exception.ReglaNegocioException;
import com.devsuj.crmapi.mapper.CatalogoMapper;
import com.devsuj.crmapi.model.Categoria;
import com.devsuj.crmapi.repository.CategoriaRepository;
import com.devsuj.crmapi.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Nombre único (la colación de MySQL ignora mayúsculas y acentos). Solo se borra si no tiene productos. */
@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository repository;
    private final ProductoRepository productoRepository;
    private final CatalogoMapper mapper;

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return repository.findAllByOrderByNombreAsc().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse obtener(Integer id) {
        return mapper.toResponse(buscar(id));
    }

    @Transactional
    public CategoriaResponse crear(CategoriaRequest request) {
        if (repository.existsByNombre(request.nombre())) {
            throw nombreDuplicado(request.nombre());
        }
        Categoria categoria = new Categoria();
        mapper.copiar(request, categoria);
        return mapper.toResponse(repository.save(categoria));
    }

    @Transactional
    public CategoriaResponse actualizar(Integer id, CategoriaRequest request) {
        Categoria categoria = buscar(id);
        if (repository.existsByNombreAndIdNot(request.nombre(), id)) {
            throw nombreDuplicado(request.nombre());
        }
        mapper.copiar(request, categoria);
        return mapper.toResponse(categoria);
    }

    @Transactional
    public void eliminar(Integer id) {
        Categoria categoria = buscar(id);
        if (productoRepository.existsByCategoriaId(id)) {
            throw new ReglaNegocioException("La categoría " + categoria.getNombre()
                    + " tiene productos: muévelos a otra categoría antes de borrarla.");
        }
        repository.delete(categoria);
    }

    @Transactional(readOnly = true)
    public Categoria buscar(Integer id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("una categoría", id));
    }

    private static ReglaNegocioException nombreDuplicado(String nombre) {
        return new ReglaNegocioException("Ya existe una categoría llamada " + nombre + ".");
    }
}
