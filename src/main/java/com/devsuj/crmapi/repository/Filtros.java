package com.devsuj.crmapi.repository;

import com.devsuj.crmapi.security.Alcance;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Locale;

/**
 * Piezas reutilizables para armar filtros opcionales con {@link Specification} (DRY): si el valor
 * del filtro es {@code null}, la pieza no restringe nada.
 */
public final class Filtros {

    private Filtros() {
    }

    /** {@code ruta} navega atributos: {@code igual(5, "cliente", "id")} = {@code cliente.id = 5}. */
    public static <T> Specification<T> igual(Object valor, String... ruta) {
        if (valor == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.equal(camino(root, ruta), valor);
    }

    public static <T> Specification<T> desde(LocalDateTime desde, String atributo) {
        if (desde == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(atributo), desde);
    }

    public static <T> Specification<T> antesDe(LocalDateTime hasta, String atributo) {
        if (hasta == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.lessThan(root.get(atributo), hasta);
    }

    /** Búsqueda de texto (contiene, sin distinguir mayúsculas) en cualquiera de los atributos. */
    public static <T> Specification<T> texto(String q, String... atributos) {
        if (q == null || q.isBlank()) {
            return Specification.unrestricted();
        }
        String patron = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.or(Arrays.stream(atributos)
                .map(a -> cb.like(cb.lower(root.get(a)), patron))
                .toArray(jakarta.persistence.criteria.Predicate[]::new));
    }

    /**
     * Registros visibles según el alcance: el dueño ({@code atributoDueno}) está en el alcance
     * o el cliente del registro está asignado a alguien del alcance.
     */
    public static <T> Specification<T> visiblePara(Alcance alcance, String atributoDueno) {
        if (alcance.todos()) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.or(
                camino(root, atributoDueno, "id").in(alcance.idsParaConsulta()),
                camino(root, "cliente", "vendedor", "id").in(alcance.idsParaConsulta()));
    }

    /** Clientes visibles por su vendedor asignado (contratos). */
    public static <T> Specification<T> clienteVisiblePara(Alcance alcance) {
        if (alcance.todos()) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> camino(root, "cliente", "vendedor", "id").in(alcance.idsParaConsulta());
    }

    private static Path<?> camino(Root<?> root, String... ruta) {
        Path<?> camino = root;
        for (String parte : ruta) {
            camino = camino.get(parte);
        }
        return camino;
    }
}
