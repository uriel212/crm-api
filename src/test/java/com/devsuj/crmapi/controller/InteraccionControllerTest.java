package com.devsuj.crmapi.controller;

import com.devsuj.crmapi.support.PruebaIntegracion;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class InteraccionControllerTest extends PruebaIntegracion {

    @Test
    void agendaDelVendedorActual() throws Exception {
        mvc.perform(como(CARLA, get("/api/interacciones/proximas")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].asunto", containsString("docking")))
                .andExpect(jsonPath("$[0].vencida").value(false));
        mvc.perform(como(JORGE, get("/api/interacciones/proximas").param("dias", "10")))
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void elGerenteVeLaAgendaDeSuEquipoPeroUnVendedorNoLaDeOtro() throws Exception {
        mvc.perform(como(GERENTE, get("/api/interacciones/proximas").param("usuarioId", "4").param("dias", "10")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(como(CARLA, get("/api/interacciones/proximas").param("usuarioId", "4")))
                .andExpect(status().isForbidden());
    }

    @Test
    void registraUnaInteraccionRealizadaConFechaActual() throws Exception {
        mvc.perform(json(CARLA, post("/api/clientes/1/interacciones"), """
                        {"tipo":"LLAMADA","asunto":"Confirmación de pedido"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("REALIZADA"))
                .andExpect(jsonPath("$.fecha").value("2026-10-20T10:00:00"))
                .andExpect(jsonPath("$.usuarioId").value(CARLA));
    }

    @Test
    void validaLasFechasSegunElEstado() throws Exception {
        mvc.perform(json(CARLA, post("/api/clientes/1/interacciones"), """
                        {"tipo":"REUNION","estado":"PROGRAMADA","fecha":"2026-10-01T10:00:00","asunto":"Pasada"}"""))
                .andExpect(status().isBadRequest());
        mvc.perform(json(CARLA, post("/api/clientes/1/interacciones"), """
                        {"tipo":"REUNION","fecha":"2026-11-01T10:00:00","asunto":"Futura realizada"}"""))
                .andExpect(status().isBadRequest());
        mvc.perform(json(CARLA, post("/api/clientes/1/interacciones"), """
                        {"tipo":"REUNION","estado":"PROGRAMADA","fecha":"2026-11-01T10:00:00","asunto":"Visita"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PROGRAMADA"));
    }

    @Test
    void noSeRegistranInteraccionesEnClientesAjenos() throws Exception {
        mvc.perform(json(CARLA, post("/api/clientes/3/interacciones"), """
                        {"tipo":"NOTA","asunto":"Intento"}"""))
                .andExpect(status().isForbidden());
    }

    @Test
    void unaActividadSeCierraUnaSolaVez() throws Exception {
        mvc.perform(json(CARLA, patch("/api/interacciones/24/estado"), """
                        {"estado":"REALIZADA","resultado":"Confirmó 5 docking stations"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("REALIZADA"))
                .andExpect(jsonPath("$.fecha").value("2026-10-20T10:00:00"))
                .andExpect(jsonPath("$.resultado").value("Confirmó 5 docking stations"));
        mvc.perform(json(CARLA, patch("/api/interacciones/24/estado"), """
                        {"estado":"CANCELADA"}"""))
                .andExpect(status().isConflict());
    }

    @Test
    void soloElAutorOSuGerenteModifican() throws Exception {
        String cambio = """
                {"tipo":"LLAMADA","fecha":"2026-10-25T10:00:00","asunto":"Reprogramada"}""";
        mvc.perform(json(JORGE, put("/api/interacciones/24"), cambio)).andExpect(status().isForbidden());
        mvc.perform(json(GERENTE, put("/api/interacciones/24"), cambio))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fecha").value("2026-10-25T10:00:00"));
    }

    @Test
    void historialDelClienteRespetaElAlcance() throws Exception {
        mvc.perform(como(CARLA, get("/api/clientes/1/interacciones")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(4))
                .andExpect(jsonPath("$.contenido[0].fecha").value("2026-10-21T16:00:00"));
        mvc.perform(como(JORGE, get("/api/clientes/1/interacciones")))
                .andExpect(jsonPath("$.totalElementos").value(0));
        mvc.perform(como(JORGE, get("/api/interacciones/1"))).andExpect(status().isForbidden());
    }
}
