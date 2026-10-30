package com.devsuj.crmapi.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class EtapaOportunidadTest {

    @Test
    void entreAbiertasSeAvanzaYRetrocede() {
        assertThat(EtapaOportunidad.PROSPECTO.puedeCambiarA(EtapaOportunidad.NEGOCIACION)).isTrue();
        assertThat(EtapaOportunidad.NEGOCIACION.puedeCambiarA(EtapaOportunidad.CALIFICADA)).isTrue();
        assertThat(EtapaOportunidad.PROPUESTA.puedeCambiarA(EtapaOportunidad.PERDIDA)).isTrue();
    }

    @Test
    void noSeCambiaALaMismaEtapaNiAGanadaAMano() {
        assertThat(EtapaOportunidad.PROPUESTA.puedeCambiarA(EtapaOportunidad.PROPUESTA)).isFalse();
        assertThat(EtapaOportunidad.NEGOCIACION.puedeCambiarA(EtapaOportunidad.GANADA)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(EtapaOportunidad.class)
    void lasCerradasSonFinales(EtapaOportunidad destino) {
        assertThat(EtapaOportunidad.GANADA.puedeCambiarA(destino)).isFalse();
        assertThat(EtapaOportunidad.PERDIDA.puedeCambiarA(destino)).isFalse();
    }
}
