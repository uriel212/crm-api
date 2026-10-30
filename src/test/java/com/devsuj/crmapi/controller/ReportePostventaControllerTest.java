package com.devsuj.crmapi.controller;

import com.devsuj.crmapi.support.PruebaIntegracion;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Reportes de campañas, soporte y fidelización (9.8) y vista 360 (9.9), con valores calculados sobre el seed. */
class ReportePostventaControllerTest extends PruebaIntegracion {

    @Test
    void efectividadDeCampanas() throws Exception {
        mvc.perform(como(GERENTE, get("/api/reportes/campanas/efectividad")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].campanaId").value(3))
                .andExpect(jsonPath("$[0].destinatarios").value(0))
                .andExpect(jsonPath("$[0].costoPorRespuesta").doesNotExist())
                .andExpect(jsonPath("$[2].campanaId").value(2))
                .andExpect(jsonPath("$[2].tasaRespuesta").value(33.33));
        mvc.perform(como(CARLA, get("/api/reportes/campanas/efectividad")))
                .andExpect(status().isForbidden());
    }

    @Test
    void soportePorAlcance() throws Exception {
        mvc.perform(como(ADMIN, get("/api/reportes/soporte")))
                .andExpect(jsonPath("$.abiertos").value(2))
                .andExpect(jsonPath("$.enProceso").value(1))
                .andExpect(jsonPath("$.cerrados").value(3))
                .andExpect(jsonPath("$.sinAsignar").value(1))
                .andExpect(jsonPath("$.horasPromedioCierre").value(60.00))
                .andExpect(jsonPath("$.pendientesPorPrioridad[0].prioridad").value("BAJA"))
                .andExpect(jsonPath("$.pendientesPorPrioridad[0].cantidad").value(1))
                .andExpect(jsonPath("$.pendientesPorPrioridad[2].prioridad").value("ALTA"))
                .andExpect(jsonPath("$.pendientesPorPrioridad[2].cantidad").value(1));
        mvc.perform(como(CARLA, get("/api/reportes/soporte")))
                .andExpect(jsonPath("$.abiertos").value(0))
                .andExpect(jsonPath("$.enProceso").value(1))
                .andExpect(jsonPath("$.cerrados").value(1))
                .andExpect(jsonPath("$.horasPromedioCierre").value(56.00));
    }

    @Test
    void fidelizacionPorAlcance() throws Exception {
        mvc.perform(como(ADMIN, get("/api/reportes/fidelizacion")))
                .andExpect(jsonPath("$.puntosEmitidos").value(7543))
                .andExpect(jsonPath("$.puntosCanjeados").value(1500))
                .andExpect(jsonPath("$.saldoEnCirculacion").value(6043))
                .andExpect(jsonPath("$.clientesConSaldo").value(10))
                .andExpect(jsonPath("$.topClientes", hasSize(5)))
                .andExpect(jsonPath("$.topClientes[0].clienteId").value(1))
                .andExpect(jsonPath("$.topClientes[0].saldo").value(1330))
                .andExpect(jsonPath("$.topClientes[1].clienteId").value(8));
        mvc.perform(como(CARLA, get("/api/reportes/fidelizacion")))
                .andExpect(jsonPath("$.puntosEmitidos").value(3470))
                .andExpect(jsonPath("$.topClientes[*].clienteId").value(org.hamcrest.Matchers.contains(1, 7, 2)));
    }

    @Test
    void vistaTrescientosSesentaDelCliente() throws Exception {
        mvc.perform(como(CARLA, get("/api/clientes/1/resumen")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cliente.nombre").value("Roberto Garza"))
                .andExpect(jsonPath("$.numeroCompras").value(3))
                .andExpect(jsonPath("$.totalComprado").value(233279.00))
                .andExpect(jsonPath("$.ticketPromedio").value(77759.67))
                .andExpect(jsonPath("$.ultimaCompra").value("2026-09-02T10:00:00"))
                .andExpect(jsonPath("$.ultimasVentas", hasSize(3)))
                .andExpect(jsonPath("$.ultimasVentas[0].id").value(21))
                .andExpect(jsonPath("$.oportunidadesAbiertas").value(1))
                .andExpect(jsonPath("$.montoPipeline").value(70000.00))
                .andExpect(jsonPath("$.ultimaInteraccion").value("2026-02-20T09:15:00"))
                .andExpect(jsonPath("$.actividadesProgramadas").value(1))
                .andExpect(jsonPath("$.saldoPuntos").value(1330))
                .andExpect(jsonPath("$.contratosVigentes").value(1))
                .andExpect(jsonPath("$.ticketsAbiertos").value(0))
                .andExpect(jsonPath("$.campanas").value(1));
        mvc.perform(como(CARLA, get("/api/clientes/11/resumen")))
                .andExpect(jsonPath("$.numeroCompras").value(0))
                .andExpect(jsonPath("$.ultimaCompra").doesNotExist())
                .andExpect(jsonPath("$.ticketPromedio").value(0));
        mvc.perform(como(JORGE, get("/api/clientes/1/resumen")))
                .andExpect(status().isForbidden());
        mvc.perform(como(ADMIN, get("/api/clientes/999/resumen")))
                .andExpect(status().isNotFound());
    }
}
