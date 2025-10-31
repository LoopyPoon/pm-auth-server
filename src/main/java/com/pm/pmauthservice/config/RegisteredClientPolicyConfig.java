package com.pm.pmauthservice.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;

@Configuration
public class RegisteredClientPolicyConfig {

    @Bean
    CommandLineRunner enforceRegisteredClientPolicy(RegisteredClientRepository repo) {
        return args -> {
            var rc = repo.findByClientId("pm-spa");
            if (rc == null) return;

            boolean needSave = false;
            var builder = RegisteredClient.from(rc);

            // Политика токенов: короткий access, длинный refresh, ротация refresh токенов
            var desiredToken = TokenSettings.builder()
                    .accessTokenTimeToLive(Duration.ofMinutes(15))
                    .refreshTokenTimeToLive(Duration.ofDays(30))
                    .reuseRefreshTokens(false)
                    .build();
            if (!rc.getTokenSettings().equals(desiredToken)) {
                builder.tokenSettings(desiredToken);
                needSave = true;
            }

            // Клиентские настройки — PKCE и consent оставляем включёнными
            var desiredClient = ClientSettings.builder()
                    .requireProofKey(true)
                    .requireAuthorizationConsent(true)
                    .build();
            if (!rc.getClientSettings().equals(desiredClient)) {
                builder.clientSettings(desiredClient);
                needSave = true;
            }

            // Гарантируем наличие refresh_token гранта
            if (rc.getAuthorizationGrantTypes().stream().noneMatch(AuthorizationGrantType.REFRESH_TOKEN::equals)) {
                builder.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN);
                needSave = true;
            }

            // Гарантируем наличие offline_access (для выдачи refresh_token в Postman/SPA)
            if (!rc.getScopes().contains("offline_access")) {
                builder.scope("offline_access");
                needSave = true;
            }

            if (needSave) {
                repo.save(builder.build()); // Сохранение пройдёт через репозиторий и сериализует JSON корректно
            }
        };
    }
}