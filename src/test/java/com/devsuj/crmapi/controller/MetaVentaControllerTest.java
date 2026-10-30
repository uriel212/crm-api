package com.devsuj.crmapi.controller;

import com.devsuj.crmapi.support.PruebaIntegracion;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Seed: metas de jul–oct 2026 para Carla (ids 1–4), Jorge (5–8) y Sofía (9–12). */
class MetaVentaControllerTest extends PruebaIntegracion {

    @Test
    void elAvanceSeCalculaConLasVentasRealesDelMes() throws Exception {
        mvc.perform(como(CARLA, get("/api/metas").param("anio", "2026").param("mes", "9")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.contenido[0].montoObjetivo").value(65000.00))
                .andExpect(jsonPath("$.contenido[0].montoVendido").value(25794.00))
                .andExpect(jsonPath("$.contenido[0].numeroVentas").value(1))
                .andExpect(jsonPath("$.contenido[0].porcentajeAvance").value(39.68))
                .andExpect(jsonPath("$.contenido[0].faltante").value(39206.00))
                .andExpect(jsonPath("$.contenido[0].cumplida").value(false));
        mvc.perform(como(SOFIA, get("/api/metas/12")))
                .andExpect(jsonPath("$.montoVendido").value(30975.00))
                .andExpect(jsonPath("$.porcentajeAvance").value(68.83));
    }

    @Test
    void unaVentaNuevaSumaAlAvanceDelMes() throws Exception {
        mvc.perform(como(CARLA, get("/api/metas/4")))
                .andExpect(jsonPath("$.montoVendido").value(0));
        mvc.perform(json(CARLA, post("/api/ventas"), """
                        {"clienteId":1,"detalles":[{"productoId":12,"cantidad":5}]}"""))
                .andExpect(status().isCreated());
        mvc.perform(como(CARLA, get("/api/metas/4")))
                .andExpect(jsonPath("$.montoVendido").value(12495.00))
                .andExpect(jsonPath("$.numeroVentas").value(1));
    }

    @Test
    void cadaQuienVeLasMetasDeSuAlcance() throws Exception {
        mvc.perform(como(CARLA, get("/api/metas")))
                .andExpect(jsonPath("$.totalElementos").value(4))
                .andExpect(jsonPath("$.contenido[0].mes").value(10));
        mvc.perform(como(GERENTE, get("/api/metas")))
                .andExpect(jsonPath("$.totalElementos").value(12));
        mvc.perform(como(JORGE, get("/api/metas/3")))
                .andExpect(status().isForbidden());
        mvc.perform(como(JORGE, get("/api/metas").param("usuarioId", "3")))
                .andExpect(status().isForbidden());
        mvc.perform(como(ADMIN, get("/api/metas/999")))
                .andExpect(status().isNotFound());
    }

    @Test
    void elGerenteCreaEditaYEliminaMetasDeSuEquipo() throws Exception {
        String nueva = """
                {"usuarioId":3,"anio":2026,"mes":11,"montoObjetivo":70000}""";
        String location = mvc.perform(json(GERENTE, post("/api/metas"), nueva))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuarioNombre").value("Carla Mendoza"))
                .andExpect(jsonPath("$.montoVendido").value(0))
                .andReturn().getResponse().getHeader("Location");
        mvc.perform(json(GERENTE, post("/api/metas"), nueva))
                .andExpect(status().isConflict());

        String ruta = location.substring(location.indexOf("/api/metas"));
        mvc.perform(json(GERENTE, put(ruta), """
                        {"usuarioId":3,"anio":2026,"mes":11,"montoObjetivo":80000}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.montoObjetivo").value(80000));
        mvc.perform(json(GERENTE, put(ruta), """
                        {"usuarioId":3,"anio":2026,"mes":10,"montoObjetivo":80000}"""))
                .andExpect(status().isConflict());
        mvc.perform(como(GERENTE, delete(ruta)))
                .andExpect(status().isNoContent());
        mvc.perform(como(GERENTE, get(ruta)))
                .andExpect(status().isNotFound());
    }

    @Test
    void soloVendedoresActivosYSoloGerenteOAdmin() throws Exception {
        mvc.perform(json(CARLA, post("/api/metas"), """
                        {"usuarioId":3,"anio":2026,"mes":11,"montoObjetivo":1}"""))
                .andExpect(status().isForbidden());
        mvc.perform(json(ADMIN, post("/api/metas"), """
                        {"usuarioId":2,"anio":2026,"mes":11,"montoObjetivo":1}"""))
                .andExpect(status().isConflict());
        mvc.perform(json(ADMIN, post("/api/metas"), """
                        {"usuarioId":6,"anio":2026,"mes":11,"montoObjetivo":1}"""))
                .andExpect(status().isConflict());
        mvc.perform(json(ADMIN, post("/api/metas"), """
                        {"usuarioId":3,"anio":2026,"mes":13,"montoObjetivo":0}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.mes").exists())
                .andExpect(jsonPath("$.errores.montoObjetivo").exists());
    }
}
