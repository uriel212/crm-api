package com.devsuj.crmapi.controller;

import com.devsuj.crmapi.support.PruebaIntegracion;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Valores calculados a mano sobre el seed, con "hoy" = 2026-10-20. */
class ReporteControllerTest extends PruebaIntegracion {

    @Test
    void tableroDelAdminEnElMesEnCurso() throws Exception {
        mvc.perform(como(ADMIN, get("/api/reportes/dashboard")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.desde").value("2026-10-01"))
                .andExpect(jsonPath("$.hasta").value("2026-10-20"))
                .andExpect(jsonPath("$.totalVendido").value(30975.00))
                .andExpect(jsonPath("$.numeroVentas").value(1))
                .andExpect(jsonPath("$.ticketPromedio").value(30975.00))
                .andExpect(jsonPath("$.metaMes").value(165000.00))
                .andExpect(jsonPath("$.vendidoMes").value(30975.00))
                .andExpect(jsonPath("$.avanceMetaMes").value(18.77))
                .andExpect(jsonPath("$.clientesActivos").value(9))
                .andExpect(jsonPath("$.clientesProspecto").value(4))
                .andExpect(jsonPath("$.clientesNuevos").value(1))
                .andExpect(jsonPath("$.oportunidadesAbiertas").value(9))
                .andExpect(jsonPath("$.montoPipeline").value(686095.00))
                .andExpect(jsonPath("$.tasaConversion").value(0))
                .andExpect(jsonPath("$.ticketsAbiertos").value(3))
                .andExpect(jsonPath("$.productosStockBajo").value(2));
    }

    @Test
    void elTableroDelVendedorSoloCuentaLoSuyo() throws Exception {
        mvc.perform(como(CARLA, get("/api/reportes/dashboard")))
                .andExpect(jsonPath("$.totalVendido").value(0))
                .andExpect(jsonPath("$.metaMes").value(65000.00))
                .andExpect(jsonPath("$.clientesActivos").value(3))
                .andExpect(jsonPath("$.clientesProspecto").value(1))
                .andExpect(jsonPath("$.oportunidadesAbiertas").value(3))
                .andExpect(jsonPath("$.montoPipeline").value(142196.00))
                .andExpect(jsonPath("$.ticketsAbiertos").value(1));
        mvc.perform(como(CARLA, get("/api/reportes/dashboard").param("desde", "2026-09-01").param("hasta", "2026-09-30")))
                .andExpect(jsonPath("$.totalVendido").value(25794.00))
                .andExpect(jsonPath("$.metaMes").value(65000.00));
        mvc.perform(como(CARLA, get("/api/reportes/dashboard").param("desde", "2026-10-20").param("hasta", "2026-10-01")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ventasPorMesDevuelveLosDoceMeses() throws Exception {
        mvc.perform(como(ADMIN, get("/api/reportes/ventas/por-mes").param("anio", "2026")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(12)))
                .andExpect(jsonPath("$[8].mes").value(9))
                .andExpect(jsonPath("$[8].numeroVentas").value(3))
                .andExpect(jsonPath("$[8].total").value(118776.00))
                .andExpect(jsonPath("$[9].total").value(30975.00))
                .andExpect(jsonPath("$[10].numeroVentas").value(0))
                .andExpect(jsonPath("$[10].total").value(0));
        mvc.perform(como(ADMIN, get("/api/reportes/ventas/por-mes").param("anio", "1990")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.anio").exists());
    }

    @Test
    void ventasPorVendedorCategoriaYProductos() throws Exception {
        mvc.perform(como(ADMIN, get("/api/reportes/ventas/por-vendedor")))
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].usuarioNombre").value("Carla Mendoza"))
                .andExpect(jsonPath("$[0].numeroVentas").value(8))
                .andExpect(jsonPath("$[0].total").value(347699.60))
                .andExpect(jsonPath("$[1].total").value(259065.00));
        mvc.perform(como(JORGE, get("/api/reportes/ventas/por-vendedor")))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].usuarioId").value(JORGE));
        mvc.perform(como(ADMIN, get("/api/reportes/ventas/por-categoria")))
                .andExpect(jsonPath("$[0].categoriaNombre").value("Laptops"))
                .andExpect(jsonPath("$[0].unidades").value(18))
                .andExpect(jsonPath("$[0].total").value(341482.00));
        mvc.perform(como(ADMIN, get("/api/reportes/productos/mas-vendidos").param("limite", "2")))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].productoId").value(13))
                .andExpect(jsonPath("$[0].unidades").value(60))
                .andExpect(jsonPath("$[1].productoId").value(12))
                .andExpect(jsonPath("$[1].unidades").value(40));
        mvc.perform(como(ADMIN, get("/api/reportes/productos/mas-vendidos").param("limite", "0")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void pipelineYConversion() throws Exception {
        mvc.perform(como(ADMIN, get("/api/reportes/oportunidades/pipeline")))
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[0].etapa").value("PROSPECTO"))
                .andExpect(jsonPath("$[0].cantidad").value(3))
                .andExpect(jsonPath("$[0].montoEstimado").value(154900.00))
                .andExpect(jsonPath("$[3].etapa").value("NEGOCIACION"))
                .andExpect(jsonPath("$[3].cantidad").value(3))
                .andExpect(jsonPath("$[4].etapa").value("GANADA"))
                .andExpect(jsonPath("$[4].cantidad").value(5));
        mvc.perform(como(ADMIN, get("/api/reportes/oportunidades/conversion")))
                .andExpect(jsonPath("$.desde").value("2026-01-01"))
                .andExpect(jsonPath("$.ganadas").value(5))
                .andExpect(jsonPath("$.perdidas").value(2))
                .andExpect(jsonPath("$.tasaConversion").value(71.43))
                .andExpect(jsonPath("$.abiertas").value(9));
    }

    @Test
    void rendimientoDeVendedoresContraSuMeta() throws Exception {
        mvc.perform(como(GERENTE, get("/api/reportes/rendimiento-vendedores").param("anio", "2026").param("mes", "9")))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].usuarioNombre").value("Sofía Herrera"))
                .andExpect(jsonPath("$[0].totalVendido").value(49497.00))
                .andExpect(jsonPath("$[0].metaObjetivo").value(45000.00))
                .andExpect(jsonPath("$[0].avanceMeta").value(109.99))
                .andExpect(jsonPath("$[2].usuarioNombre").value("Carla Mendoza"))
                .andExpect(jsonPath("$[2].avanceMeta").value(39.68));
        mvc.perform(como(CARLA, get("/api/reportes/rendimiento-vendedores").param("anio", "2026").param("mes", "6")))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].oportunidadesPerdidas").value(1))
                .andExpect(jsonPath("$[0].metaObjetivo").doesNotExist())
                .andExpect(jsonPath("$[0].tasaConversion").value(0));
        mvc.perform(como(CARLA, get("/api/reportes/rendimiento-vendedores").param("mes", "13")))
                .andExpect(status().isBadRequest());
    }
}
