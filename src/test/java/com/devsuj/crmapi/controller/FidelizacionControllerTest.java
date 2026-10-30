package com.devsuj.crmapi.controller;

import com.devsuj.crmapi.support.PruebaIntegracion;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Seed: el cliente 1 ganó 1899 + 174 + 257 = 2330 puntos (ventas 4, 13 y 21) y canjeó 1000 → saldo 1330.
 * Canjeables: 6 (2200 pts), 7 (1900), 9 (3500), 13 (900, stock 200).
 */
class FidelizacionControllerTest extends PruebaIntegracion {

    @Test
    void saldoYMovimientosDelCliente() throws Exception {
        mvc.perform(como(JORGE, get("/api/clientes/1/puntos")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteNombre").value("Roberto Garza"))
                .andExpect(jsonPath("$.puntosGanados").value(2330))
                .andExpect(jsonPath("$.puntosCanjeados").value(1000))
                .andExpect(jsonPath("$.saldo").value(1330));
        mvc.perform(como(CARLA, get("/api/clientes/1/puntos/movimientos")))
                .andExpect(jsonPath("$.totalElementos").value(4))
                .andExpect(jsonPath("$.contenido[0].tipo").value("CANJEADO"))
                .andExpect(jsonPath("$.contenido[1].ventaId").value(21))
                .andExpect(jsonPath("$.contenido[1].puntos").value(257));
        mvc.perform(como(CARLA, get("/api/clientes/999/puntos")))
                .andExpect(status().isNotFound());
    }

    @Test
    void canjeDescuentaPuntosYStock() throws Exception {
        mvc.perform(json(CARLA, post("/api/clientes/1/puntos/canjes"), "{\"productoId\":13,\"cantidad\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.movimiento.tipo").value("CANJEADO"))
                .andExpect(jsonPath("$.movimiento.puntos").value(900))
                .andExpect(jsonPath("$.movimiento.productoId").value(13))
                .andExpect(jsonPath("$.movimiento.cantidad").value(1))
                .andExpect(jsonPath("$.movimiento.usuarioId").value(CARLA))
                .andExpect(jsonPath("$.saldo").value(430));
        mvc.perform(como(CARLA, get("/api/clientes/1/puntos")))
                .andExpect(jsonPath("$.saldo").value(430));
        mvc.perform(como(CARLA, get("/api/productos/13")))
                .andExpect(jsonPath("$.stock").value(199));
        mvc.perform(como(ADMIN, get("/api/productos/13/movimientos")))
                .andExpect(jsonPath("$.contenido[0].tipo").value("CANJE"));
    }

    @Test
    void reglasDelCanje() throws Exception {
        mvc.perform(json(CARLA, post("/api/clientes/1/puntos/canjes"), "{\"productoId\":7,\"cantidad\":1}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", containsString("Puntos insuficientes")))
                .andExpect(jsonPath("$.saldo").value(1330))
                .andExpect(jsonPath("$.puntosNecesarios").value(1900));
        mvc.perform(json(CARLA, post("/api/clientes/1/puntos/canjes"), "{\"productoId\":1,\"cantidad\":1}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", containsString("no se puede canjear")));
        mvc.perform(json(CARLA, post("/api/clientes/15/puntos/canjes"), "{\"productoId\":13,\"cantidad\":1}"))
                .andExpect(status().isConflict());
        mvc.perform(json(JORGE, post("/api/clientes/1/puntos/canjes"), "{\"productoId\":13,\"cantidad\":1}"))
                .andExpect(status().isForbidden());
        mvc.perform(json(CARLA, post("/api/clientes/1/puntos/canjes"), "{\"productoId\":13,\"cantidad\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.cantidad").exists());
    }

    @Test
    void unaVentaNuevaSumaPuntosAlSaldo() throws Exception {
        mvc.perform(json(CARLA, post("/api/ventas"), """
                        {"clienteId":1,"detalles":[{"productoId":12,"cantidad":5}]}"""))
                .andExpect(status().isCreated());
        mvc.perform(como(CARLA, get("/api/clientes/1/puntos")))
                .andExpect(jsonPath("$.puntosGanados").value(2454))
                .andExpect(jsonPath("$.saldo").value(1454));
    }
}
