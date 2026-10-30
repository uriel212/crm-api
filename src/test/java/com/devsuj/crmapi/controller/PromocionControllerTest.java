package com.devsuj.crmapi.controller;

import com.devsuj.crmapi.support.PruebaIntegracion;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Seed al 2026-10-20: vigentes la 3 (Q300 por licencia, producto 12) y la 4 (Q500 desde Q10,000, alcance VENTA).
 * La 1 está inactiva y la 2 es de la campaña 3 (PLANEADA) y de noviembre.
 */
class PromocionControllerTest extends PruebaIntegracion {

    private static final String DIEZ_POR_CIENTO_MOUSE = """
            {"nombre":"10% en mouse","tipo":"PORCENTAJE","alcance":"PRODUCTO","valor":10,
             "fechaInicio":"2026-10-01","fechaFin":"2026-10-31","productoIds":[7]}""";

    @Test
    void vigentesYFiltros() throws Exception {
        mvc.perform(como(CARLA, get("/api/promociones/vigentes")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].nombre").value("Q300 menos por licencia de Microsoft 365"))
                .andExpect(jsonPath("$[0].productos[0].id").value(12))
                .andExpect(jsonPath("$[1].alcance").value("VENTA"))
                .andExpect(jsonPath("$[1].montoMinimo").value(10000.00));
        mvc.perform(como(CARLA, get("/api/promociones")))
                .andExpect(jsonPath("$.totalElementos").value(4));
        mvc.perform(como(CARLA, get("/api/promociones").param("productoId", "4")))
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.contenido[0].id").value(2))
                .andExpect(jsonPath("$.contenido[0].campanaNombre").value("Buen Fin 2026"))
                .andExpect(jsonPath("$.contenido[0].vigente").value(false));
        mvc.perform(como(CARLA, get("/api/promociones/1")))
                .andExpect(jsonPath("$.activa").value(false))
                .andExpect(jsonPath("$.productos", hasSize(4)));
    }

    @Test
    void unaPromocionNuevaSeAplicaEnLasVentas() throws Exception {
        mvc.perform(json(GERENTE, post("/api/promociones"), DIEZ_POR_CIENTO_MOUSE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vigente").value(true))
                .andExpect(jsonPath("$.montoMinimo").doesNotExist());
        mvc.perform(json(CARLA, post("/api/ventas/calculo"), "{\"detalles\":[{\"productoId\":7,\"cantidad\":1}]}"))
                .andExpect(jsonPath("$.detalles[0].descuento").value(189.90))
                .andExpect(jsonPath("$.total").value(1709.10));
    }

    @Test
    void conCampanaNoActivaNoEstaVigenteYDesactivadaNoSeAplica() throws Exception {
        mvc.perform(json(GERENTE, post("/api/promociones"), DIEZ_POR_CIENTO_MOUSE.replace("{\"nombre\"", "{\"campanaId\":3,\"nombre\"")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.campanaId").value(3))
                .andExpect(jsonPath("$.vigente").value(false));
        mvc.perform(json(GERENTE, patch("/api/promociones/4/estado"), "{\"activa\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vigente").value(false));
        mvc.perform(json(CARLA, post("/api/ventas/calculo"), "{\"detalles\":[{\"productoId\":12,\"cantidad\":5}]}"))
                .andExpect(jsonPath("$.descuento").value(0))
                .andExpect(jsonPath("$.total").value(12995.00));
    }

    @Test
    void validaLaCoherenciaConElAlcance() throws Exception {
        mvc.perform(json(GERENTE, post("/api/promociones"), DIEZ_POR_CIENTO_MOUSE.replace("[7]", "[]")))
                .andExpect(status().isBadRequest());
        mvc.perform(json(GERENTE, post("/api/promociones"), DIEZ_POR_CIENTO_MOUSE.replace("\"PRODUCTO\"", "\"VENTA\"")))
                .andExpect(status().isBadRequest());
        mvc.perform(json(GERENTE, post("/api/promociones"), DIEZ_POR_CIENTO_MOUSE.replace("\"valor\":10", "\"valor\":150")))
                .andExpect(status().isBadRequest());
        mvc.perform(json(GERENTE, post("/api/promociones"), DIEZ_POR_CIENTO_MOUSE.replace("2026-10-31", "2026-09-01")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.fechaFin").exists());
        mvc.perform(json(GERENTE, post("/api/promociones"), DIEZ_POR_CIENTO_MOUSE.replace("[7]", "[999]")))
                .andExpect(status().isNotFound());
        mvc.perform(json(CARLA, post("/api/promociones"), DIEZ_POR_CIENTO_MOUSE))
                .andExpect(status().isForbidden());
    }

    @Test
    void soloSeBorraSiNuncaSeUso() throws Exception {
        mvc.perform(como(ADMIN, delete("/api/promociones/3")))
                .andExpect(status().isConflict());
        String location = mvc.perform(json(ADMIN, post("/api/promociones"), DIEZ_POR_CIENTO_MOUSE))
                .andReturn().getResponse().getHeader("Location");
        String ruta = location.substring(location.indexOf("/api/promociones"));
        mvc.perform(como(GERENTE, delete(ruta)))
                .andExpect(status().isForbidden());
        mvc.perform(como(ADMIN, delete(ruta)))
                .andExpect(status().isNoContent());
        mvc.perform(como(ADMIN, get(ruta)))
                .andExpect(status().isNotFound());
    }
}
