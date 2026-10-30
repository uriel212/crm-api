package com.devsuj.crmapi.controller;

import com.devsuj.crmapi.support.PruebaIntegracion;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Seed: 5 categorías (Accesorios, Impresoras, Laptops, Monitores, Software), todas con productos. */
class CategoriaControllerTest extends PruebaIntegracion {

    @Test
    void todosVenLasCategoriasOrdenadasPorNombre() throws Exception {
        mvc.perform(como(CARLA, get("/api/categorias")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].nombre").value("Accesorios"))
                .andExpect(jsonPath("$[4].nombre").value("Software"));
        mvc.perform(como(CARLA, get("/api/categorias/999")))
                .andExpect(status().isNotFound());
    }

    @Test
    void elGerenteCreaYEditaYElNombreEsUnicoSinImportarMayusculasNiAcentos() throws Exception {
        String location = mvc.perform(json(GERENTE, post("/api/categorias"), """
                        {"nombre":"Redes","descripcion":"Routers y switches"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Redes"))
                .andReturn().getResponse().getHeader("Location");
        mvc.perform(json(GERENTE, post("/api/categorias"), "{\"nombre\":\"LAPTOPS\"}"))
                .andExpect(status().isConflict());
        String ruta = location.substring(location.indexOf("/api/categorias"));
        mvc.perform(json(GERENTE, put(ruta), "{\"nombre\":\"Redes y conectividad\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Redes y conectividad"));
        mvc.perform(json(GERENTE, put(ruta), "{\"nombre\":\"Monitores\"}"))
                .andExpect(status().isConflict());
        mvc.perform(json(GERENTE, post("/api/categorias"), "{\"nombre\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").exists());
    }

    @Test
    void soloElAdminBorraYSoloSiNoTieneProductos() throws Exception {
        String location = mvc.perform(json(ADMIN, post("/api/categorias"), "{\"nombre\":\"Temporal\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        String ruta = location.substring(location.indexOf("/api/categorias"));
        mvc.perform(como(GERENTE, delete(ruta)))
                .andExpect(status().isForbidden());
        mvc.perform(como(ADMIN, delete("/api/categorias/1")))
                .andExpect(status().isConflict());
        mvc.perform(como(ADMIN, delete(ruta)))
                .andExpect(status().isNoContent());
        mvc.perform(json(CARLA, post("/api/categorias"), "{\"nombre\":\"X\"}"))
                .andExpect(status().isForbidden());
    }
}
