package com.devsuj.crmapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** Respuesta 201 Created con el header Location del recurso nuevo (DRY entre controllers). */
final class Respuestas {

    private Respuestas() {
    }

    static <T> ResponseEntity<T> creado(String ruta, Integer id, T cuerpo) {
        var ubicacion = ServletUriComponentsBuilder.fromCurrentContextPath().path(ruta).buildAndExpand(id).toUri();
        return ResponseEntity.created(ubicacion).body(cuerpo);
    }
}
