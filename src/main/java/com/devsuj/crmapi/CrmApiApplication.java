package com.devsuj.crmapi;

import com.devsuj.crmapi.config.ZonaHorariaConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class CrmApiApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(ZonaHorariaConfig.ZONA_NEGOCIO));
        SpringApplication.run(CrmApiApplication.class, args);
    }
}
