package com.devsuj.crmapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Una sola zona horaria para todo el negocio (Guatemala). Los services piden la hora a este
 * {@link Clock} en lugar de llamar a {@code LocalDateTime.now()} directo: así las pruebas pueden
 * fijar "hoy" y los cálculos por fecha (vigencias, 90 días sin compra, vencimientos) son repetibles.
 */
@Configuration
public class ZonaHorariaConfig {

    public static final String ZONA_NEGOCIO = "America/Guatemala";

    @Bean
    public Clock clock(@Value("${app.zona-horaria:" + ZONA_NEGOCIO + "}") String zona) {
        return Clock.system(ZoneId.of(zona));
    }
}
