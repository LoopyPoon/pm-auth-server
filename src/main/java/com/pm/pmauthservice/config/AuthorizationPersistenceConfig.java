package com.pm.pmauthservice.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;
import java.util.UUID;

@Configuration
public class AuthorizationPersistenceConfig {

    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcRegisteredClientRepository(jdbcTemplate);
    }

    @Bean
    public OAuth2AuthorizationService authorizationService(JdbcTemplate jdbcTemplate,
                                                           RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
    }

    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService(JdbcTemplate jdbcTemplate,
                                                                         RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
    }

    // Сидинг public SPA-клиента (PKCE) если его нет в БД
    @Bean
    public CommandLineRunner seedSpaClient(RegisteredClientRepository clients) {
        return args -> {
            if (clients.findByClientId("pm-spa") != null) return;

            var tokenSettings = TokenSettings.builder()
                    .accessTokenTimeToLive(Duration.ofMinutes(15))
                    .refreshTokenTimeToLive(Duration.ofDays(30))
                    .reuseRefreshTokens(false) // включаем ротацию refresh токенов
                    .build();

            var clientSettings = ClientSettings.builder()
                    .requireAuthorizationConsent(true)
                    .requireProofKey(true)
                    .build();

            RegisteredClient spaClient = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId("pm-spa")
                    .clientName("PoorMusic SPA (Local)")
                    .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .redirectUri("https://oauth.pstmn.io/v1/callback")
                    .redirectUri("http://localhost:5173/callback")
                    .postLogoutRedirectUri("http://localhost:5173/")
                    .scope(OidcScopes.OPENID)
                    .scope(OidcScopes.PROFILE)
                    .scope("catalog.read")
                    .scope("catalog.write")
                    .scope("offline_access") // чтобы получать refresh_token
                    .tokenSettings(tokenSettings)
                    .clientSettings(clientSettings)
                    .build();

            clients.save(spaClient);
        };
    }
}