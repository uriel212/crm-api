package com.devsuj.crmapi.controller;

import com.devsuj.crmapi.support.PruebaIntegracion;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Seed: 14 productos; 3 (stock 2/3) y 8 (4/5) con stock bajo; 14 inactivo; 6, 7, 9 y 13 canjeables. */
class ProductoControllerTest extends PruebaIntegracion {

    @Test
    void catalogoConFiltros() throws Exception {
        mvc.perform(como(CARLA, get("/api/productos").param("size", "50")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(14));
        mvc.perform(como(CARLA, get("/api/productos").param("activo", "true").param("canjeables", "true")))
                .andExpect(jsonPath("$.totalElementos").value(4))
                .andExpect(jsonPath("$.contenido[*].canjeable", everyItem(is(true))));
        mvc.perform(como(CARLA, get("/api/productos").param("categoriaId", "1").param("q", "lenovo")))
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.contenido[0].id").value(1));
        mvc.perform(como(CARLA, get("/api/productos/3")))
                .andExpect(jsonPath("$.stockBajo").value(true))
                .andExpect(jsonPath("$.categoriaNombre").value("Laptops"));
    }

    @Test
    void stockBajoExcluyeInactivosYSoloLoVenGerenteYAdmin() throws Exception {
        mvc.perform(como(GERENTE, get("/api/productos/stock-bajo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(3, 8)));
        mvc.perform(como(CARLA, get("/api/productos/stock-bajo")))
                .andExpect(status().isForbidden());
    }

    @Test
    void crearConStockInicialLoDejaEnElKardex() throws Exception {
        String location = mvc.perform(json(GERENTE, post("/api/productos"), """
                        {"categoriaId":3,"nombre":"Teclado mecánico","precio":1299.00,"stockMinimo":5,"stockInicial":20}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.stock").value(20))
                .andExpect(jsonPath("$.canjeable").value(false))
                .andReturn().getResponse().getHeader("Location");
        String ruta = location.substring(location.indexOf("/api/productos"));
        mvc.perform(como(GERENTE, get(ruta + "/movimientos")))
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.contenido[0].tipo").value("ENTRADA"))
                .andExpect(jsonPath("$.contenido[0].stockResultante").value(20));
        mvc.perform(json(GERENTE, post("/api/productos"), """
                        {"categoriaId":999,"nombre":"X","precio":1,"stockMinimo":0}"""))
                .andExpect(status().isNotFound());
        mvc.perform(json(GERENTE, post("/api/productos"), """
                        {"categoriaId":3,"nombre":"","precio":-1,"stockMinimo":0}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").exists())
                .andExpect(jsonPath("$.errores.precio").exists());
        mvc.perform(json(CARLA, post("/api/productos"), """
                        {"categoriaId":3,"nombre":"X","precio":1,"stockMinimo":0}"""))
                .andExpect(status().isForbidden());
    }

    @Test
    void ajustesManualesDeInventario() throws Exception {
        mvc.perform(json(GERENTE, post("/api/productos/3/movimientos"), """
                        {"tipo":"ENTRADA","cantidad":10,"motivo":"Factura F-1"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("ENTRADA"))
                .andExpect(jsonPath("$.stockResultante").value(12));
        mvc.perform(json(GERENTE, post("/api/productos/3/movimientos"), """
                        {"tipo":"SALIDA","cantidad":2,"motivo":"Merma"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.stockResultante").value(10));
        mvc.perform(json(GERENTE, post("/api/productos/3/movimientos"), """
                        {"tipo":"SALIDA","cantidad":11,"motivo":"Conteo"}"""))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", containsString("Stock insuficiente")));
        mvc.perform(json(GERENTE, post("/api/productos/3/movimientos"), """
                        {"tipo":"VENTA","cantidad":1,"motivo":"x"}"""))
                .andExpect(status().isBadRequest());
        mvc.perform(como(GERENTE, get("/api/productos/3/movimientos")))
                .andExpect(jsonPath("$.contenido[0].tipo").value("SALIDA"))
                .andExpect(jsonPath("$.contenido[0].stockResultante").value(10));
        mvc.perform(como(CARLA, get("/api/productos/3")))
                .andExpect(jsonPath("$.stock").value(10))
                .andExpect(jsonPath("$.stockBajo").value(false));
    }

    @Test
    void editarYDesactivar() throws Exception {
        mvc.perform(json(GERENTE, put("/api/productos/7"), """
                        {"categoriaId":3,"nombre":"Mouse MX Master 3S","precio":1999.00,"precioPuntos":2000,"stockMinimo":10}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precio").value(1999.00))
                .andExpect(jsonPath("$.stock").value(40));
        mvc.perform(json(GERENTE, patch("/api/productos/7/estado"), "{\"activo\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
        mvc.perform(json(CARLA, post("/api/ventas"), """
                        {"clienteId":1,"detalles":[{"productoId":7,"cantidad":1}]}"""))
                .andExpect(status().isConflict());
    }
}
