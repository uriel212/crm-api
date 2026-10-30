package com.devsuj.crmapi.security;

import java.util.List;
import java.util.Set;

/**
 * Usuarios cuyos registros puede ver quien hace la petición: ADMIN = todos; GERENTE = él y su equipo;
 * VENDEDOR = solo él.
 */
public record Alcance(boolean todos, Set<Integer> usuarioIds) {

    private static final List<Integer> SIN_USUARIOS = List.of(-1);

    public static Alcance total() {
        return new Alcance(true, Set.of());
    }

    public boolean incluye(Integer usuarioId) {
        return todos || (usuarioId != null && usuarioIds.contains(usuarioId));
    }

    /** Lista para un {@code IN (:ids)} de JPQL (nunca vacía: un IN vacío es inválido en SQL). */
    public List<Integer> idsParaConsulta() {
        return usuarioIds.isEmpty() ? SIN_USUARIOS : List.copyOf(usuarioIds);
    }
}
