package com.devsuj.crmapi.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class UnPuntoPorCadaCienQuetzalesTest {

    private final ReglaPuntos regla = new UnPuntoPorCadaCienQuetzales();

    @Test
    void cuentaSoloLosCienCompletos() {
        assertThat(regla.puntosPor(new BigDecimal("12495.00"))).isEqualTo(124);
        assertThat(regla.puntosPor(new BigDecimal("99.99"))).isZero();
        assertThat(regla.puntosPor(new BigDecimal("100.00"))).isEqualTo(1);
    }
}
