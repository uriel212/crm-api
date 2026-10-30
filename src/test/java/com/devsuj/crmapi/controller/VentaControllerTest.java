package com.devsuj.crmapi.controller;

import com.devsuj.crmapi.support.PruebaIntegracion;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Seed al 2026-10-20: producto 12 (Microsoft 365, Q2,899, stock 100) con la promoción 3 (Q300 por licencia) y
 * la promoción sobre el total "Q500 en compras desde Q10,000". Oportunidad 12 (PROPUESTA) es del cliente 1 de Carla.
 */
class VentaControllerTest extends PruebaIntegracion {

    private static final String CINCO_LICENCIAS = """
            {"clienteId":1,"oportunidadId":12,"detalles":[{"productoId":12,"cantidad":5}]}""";

    @Test
    void registraLaVentaConPromocionesStockOportunidadGanadaYPuntos() throws Exception {
        mvc.perform(json(CARLA, post("/api/ventas"), CINCO_LICENCIAS))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/ventas/")))
                .andExpect(jsonPath("$.usuarioId").value(CARLA))
                .andExpect(jsonPath("$.oportunidadId").value(12))
                .andExpect(jsonPath("$.fecha").value("2026-10-20T10:00:00"))
                .andExpect(jsonPath("$.detalles[0].precioUnitario").value(2899.00))
                .andExpect(jsonPath("$.detalles[0].descuento").value(1500.00))
                .andExpect(jsonPath("$.detalles[0].subtotal").value(12995.00))
                .andExpect(jsonPath("$.detalles[0].promocionId").value(3))
                .andExpect(jsonPath("$.subtotal").value(12995.00))
                .andExpect(jsonPath("$.descuento").value(500.00))
                .andExpect(jsonPath("$.promocionNombre", containsString("Q500")))
                .andExpect(jsonPath("$.total").value(12495.00))
                .andExpect(jsonPath("$.puntosGanados").value(124));

        mvc.perform(como(CARLA, get("/api/productos/12")))
                .andExpect(jsonPath("$.stock").value(95));
        mvc.perform(como(ADMIN, get("/api/productos/12/movimientos")))
                .andExpect(jsonPath("$.contenido[0].tipo").value("VENTA"))
                .andExpect(jsonPath("$.contenido[0].cantidad").value(5))
                .andExpect(jsonPath("$.contenido[0].stockResultante").value(95));
        mvc.perform(como(CARLA, get("/api/oportunidades/12")))
                .andExpect(jsonPath("$.etapa").value("GANADA"));
        mvc.perform(como(CARLA, get("/api/oportunidades/12/historial")))
                .andExpect(jsonPath("$[-1].etapaAnterior").value("PROPUESTA"))
                .andExpect(jsonPath("$[-1].etapa").value("GANADA"))
                .andExpect(jsonPath("$[-1].comentario", startsWith("Venta #")));
    }

    @Test
    void elDetalleIncluyeRenglonesYPuntosYRespetaElAlcance() throws Exception {
        mvc.perform(como(CARLA, get("/api/ventas/4")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteNombre").value("Roberto Garza"))
                .andExpect(jsonPath("$.detalles", hasSize(1)))
                .andExpect(jsonPath("$.total").value(189990.00))
                .andExpect(jsonPath("$.puntosGanados").value(1899));
        mvc.perform(como(JORGE, get("/api/ventas/4")))
                .andExpect(status().isForbidden());
        mvc.perform(como(GERENTE, get("/api/ventas/4")))
                .andExpect(status().isOk());
        mvc.perform(como(CARLA, get("/api/ventas/999")))
                .andExpect(status().isNotFound());
    }

    @Test
    void elListadoSeFiltraPorAlcanceYFechas() throws Exception {
        mvc.perform(como(JORGE, get("/api/ventas")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(7))
                .andExpect(jsonPath("$.contenido[0].fecha").value("2026-09-12T12:30:00"))
                .andExpect(jsonPath("$.contenido[0].detalles").doesNotExist());
        mvc.perform(como(JORGE, get("/api/ventas").param("usuarioId", "3")))
                .andExpect(status().isForbidden());
        mvc.perform(como(CARLA, get("/api/ventas").param("desde", "2026-09-01").param("hasta", "2026-09-30")))
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.contenido[0].id").value(21));
        mvc.perform(como(CARLA, get("/api/ventas").param("desde", "2026-09-30").param("hasta", "2026-09-01")))
                .andExpect(status().isBadRequest());
        mvc.perform(como(ADMIN, get("/api/clientes/2/ventas")))
                .andExpect(jsonPath("$.totalElementos").value(3));
        mvc.perform(como(ADMIN, get("/api/clientes/999/ventas")))
                .andExpect(status().isNotFound());
    }

    @Test
    void elCalculoNoGuardaNada() throws Exception {
        mvc.perform(json(CARLA, post("/api/ventas/calculo"), CINCO_LICENCIAS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(12495.00))
                .andExpect(jsonPath("$.promocionId").isNumber());
        mvc.perform(como(CARLA, get("/api/productos/12")))
                .andExpect(jsonPath("$.stock").value(100));
        mvc.perform(como(CARLA, get("/api/oportunidades/12")))
                .andExpect(jsonPath("$.etapa").value("PROPUESTA"));
    }

    @Test
    void unProspectoPasaAActivoConSuPrimeraCompra() throws Exception {
        mvc.perform(json(CARLA, post("/api/ventas"), """
                        {"clienteId":11,"detalles":[{"productoId":13,"cantidad":1}]}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descuento").value(0))
                .andExpect(jsonPath("$.promocionId").doesNotExist())
                .andExpect(jsonPath("$.total").value(899.00))
                .andExpect(jsonPath("$.puntosGanados").value(8));
        mvc.perform(como(CARLA, get("/api/clientes/11")))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
    }

    @Test
    void stockInsuficienteProductoInactivoORepetido() throws Exception {
        mvc.perform(json(CARLA, post("/api/ventas"), """
                        {"clienteId":1,"detalles":[{"productoId":3,"cantidad":3}]}"""))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", containsString("Stock insuficiente")));
        mvc.perform(json(CARLA, post("/api/ventas"), """
                        {"clienteId":1,"detalles":[{"productoId":14,"cantidad":1}]}"""))
                .andExpect(status().isConflict());
        mvc.perform(json(CARLA, post("/api/ventas"), """
                        {"clienteId":1,"detalles":[{"productoId":7,"cantidad":1},{"productoId":7,"cantidad":2}]}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("repetido")));
        mvc.perform(json(CARLA, post("/api/ventas"), """
                        {"clienteId":1,"detalles":[{"productoId":999,"cantidad":1}]}"""))
                .andExpect(status().isNotFound());
    }

    @Test
    void clienteInactivoOAjenoYOportunidadInvalida() throws Exception {
        mvc.perform(json(CARLA, post("/api/ventas"), """
                        {"clienteId":15,"detalles":[{"productoId":7,"cantidad":1}]}"""))
                .andExpect(status().isConflict());
        mvc.perform(json(CARLA, post("/api/ventas"), """
                        {"clienteId":3,"detalles":[{"productoId":7,"cantidad":1}]}"""))
                .andExpect(status().isForbidden());
        mvc.perform(json(CARLA, post("/api/ventas"), """
                        {"clienteId":1,"oportunidadId":9,"detalles":[{"productoId":7,"cantidad":1}]}"""))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", containsString("otro cliente")));
        mvc.perform(json(CARLA, post("/api/ventas"), """
                        {"clienteId":1,"oportunidadId":1,"detalles":[{"productoId":7,"cantidad":1}]}"""))
                .andExpect(status().isConflict());
    }

    @Test
    void validaElCuerpo() throws Exception {
        mvc.perform(json(CARLA, post("/api/ventas"), "{\"detalles\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.clienteId").exists())
                .andExpect(jsonPath("$.errores.detalles").exists());
        mvc.perform(json(CARLA, post("/api/ventas"), """
                        {"clienteId":1,"detalles":[{"productoId":7,"cantidad":0}]}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores['detalles[0].cantidad']").exists());
        mvc.perform(post("/api/ventas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unGerentePuedeRegistrarANombreDeSuVendedor() throws Exception {
        mvc.perform(json(GERENTE, post("/api/ventas"), """
                        {"clienteId":3,"usuarioId":4,"detalles":[{"productoId":7,"cantidad":1}]}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuarioId").value(JORGE));
    }
}
