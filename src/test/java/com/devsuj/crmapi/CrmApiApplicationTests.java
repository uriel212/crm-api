package com.devsuj.crmapi;

import com.devsuj.crmapi.support.PruebaIntegracion;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Arranca la app completa: Flyway aplica V1..Vn y Hibernate valida que las entidades coincidan con el esquema. */
class CrmApiApplicationTests extends PruebaIntegracion {

    @Test
    void contextoArrancaYSwaggerEsPublico() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }
}
