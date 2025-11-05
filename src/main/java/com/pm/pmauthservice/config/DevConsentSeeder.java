package com.pm.pmauthservice.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@Profile("local")
public class DevConsentSeeder {
    @Bean
    CommandLineRunner seedConsent(JdbcTemplate jdbc) {
        return args -> {
            var sql = """
                with rc as (select id from oauth2_registered_client where client_id='pm-spa')
                insert into oauth2_authorization_consent(registered_client_id, principal_name, authorities)
                select id, 'demo@pm.local', 'SCOPE_profile,SCOPE_offline_access,SCOPE_catalog.read'
                from rc
                on conflict (registered_client_id, principal_name) do nothing
                """;
            jdbc.update(sql);
            System.out.println("[consent] seeded consent for demo@pm.local (profile, offline_access, catalog.read)");
        };
    }
}