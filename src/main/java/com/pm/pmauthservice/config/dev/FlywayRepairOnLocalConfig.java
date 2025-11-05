package com.pm.pmauthservice.config.dev;

import org.flywaydb.core.api.exception.FlywayValidateException;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
public class FlywayRepairOnLocalConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            try {
                flyway.migrate();
            } catch (FlywayValidateException ex) {
                System.out.println("[flyway] validation failed: " + ex.getMessage());
                System.out.println("[flyway] running repair()");
                flyway.repair();
                System.out.println("[flyway] re-running migrate()");
                flyway.migrate();
            }
        };
    }
}