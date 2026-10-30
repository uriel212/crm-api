package com.devsuj.crmapi.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.mysql.MySQLContainer;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * MySQL 8 real en Docker para las pruebas (el esquema usa ENUM y sintaxis de MySQL; H2 no sirve).
 * Flyway aplica todas las migraciones (esquema + seed) al arrancar. "Hoy" queda fijo en el 2026-10-20 10:00
 * (Guatemala) para que las reglas por fecha den siempre el mismo resultado.
 */
@TestConfiguration(proxyBeanMethods = false)
public class MySqlTestcontainersConfig {

    public static final ZoneId ZONA = ZoneId.of("America/Guatemala");
    public static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 20, 10, 0);

    @Bean
    @ServiceConnection
    MySQLContainer mysql() {
        return new MySQLContainer("mysql:8.0")
                .withDatabaseName("crm_ventas")
                .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_0900_ai_ci");
    }

    @Bean
    @Primary
    Clock relojFijo() {
        return Clock.fixed(AHORA.atZone(ZONA).toInstant(), ZONA);
    }
}
