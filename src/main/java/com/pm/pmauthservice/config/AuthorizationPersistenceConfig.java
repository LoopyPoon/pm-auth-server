package com.pm.pmauthservice.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
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
                    .reuseRefreshTokens(false) // ротация RT
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
                    .scope("offline_access")
                    .tokenSettings(tokenSettings)
                    .clientSettings(clientSettings)
                    .build();

            clients.save(spaClient);
            System.out.println("[seedSpaClient] created pm-spa with RT TTL=30d, reuse=false, offline_access");
        };
    }

    // Конфиденциальный клиент Only for local: удобно тестировать в Postman (Basic или Body)
    @Bean
    @Profile("local")
    public CommandLineRunner seedPostmanClient(RegisteredClientRepository clients) {
        return args -> {
            if (clients.findByClientId("pm-postman") != null) return;

            PasswordEncoder enc = PasswordEncoderFactories.createDelegatingPasswordEncoder();
            var tokenSettings = TokenSettings.builder()
                    .accessTokenTimeToLive(Duration.ofMinutes(15))
                    .refreshTokenTimeToLive(Duration.ofDays(30))
                    .reuseRefreshTokens(false)
                    .build();

            var clientSettings = ClientSettings.builder()
                    .requireAuthorizationConsent(true)
                    .requireProofKey(false)
                    .build();

            RegisteredClient postmanClient = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId("pm-postman")
                    .clientSecret(enc.encode("postman-secret"))
                    .clientName("PoorMusic Postman (Confidential, local)")
                    // Разрешаем оба способа аутентификации, чтобы не упираться в метод
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .redirectUri("https://oauth.pstmn.io/v1/callback")
                    .scope(OidcScopes.OPENID)
                    .scope(OidcScopes.PROFILE)
                    .scope("catalog.read")
                    .scope("offline_access")
                    .tokenSettings(tokenSettings)
                    .clientSettings(clientSettings)
                    .build();

            clients.save(postmanClient);
            System.out.println("[seedPostmanClient] created pm-postman with RT TTL=30d, reuse=false, basic+post auth");
        };
    }

    @Bean
    @Profile("local")
    public CommandLineRunner seedMvcClient(RegisteredClientRepository clients) {
        return args -> {
            if (clients.findByClientId("pm-mvc") != null) return;

            PasswordEncoder enc = PasswordEncoderFactories.createDelegatingPasswordEncoder();
            var tokenSettings = TokenSettings.builder()
                    .accessTokenTimeToLive(Duration.ofMinutes(15))
                    .refreshTokenTimeToLive(Duration.ofDays(30))
                    .reuseRefreshTokens(false)
                    .build();

            var clientSettings = ClientSettings.builder()
                    .requireAuthorizationConsent(true)
                    .requireProofKey(false)
                    .build();

            RegisteredClient mvcClient = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId("pm-mvc")
                    .clientSecret(enc.encode("mvc-secret"))
                    .clientName("PoorMusic MVC (Local)")
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .redirectUri("http://localhost:8080/login/oauth2/code/pm-mvc")
                    .postLogoutRedirectUri("http://localhost:8080/")
                    .scope(OidcScopes.OPENID)
                    .scope(OidcScopes.PROFILE)
                    .scope("catalog.read")
                    .scope("offline_access")
                    .tokenSettings(tokenSettings)
                    .clientSettings(clientSettings)
                    .build();

            clients.save(mvcClient);
            System.out.println("[seedMvcClient] created pm-mvc with RT TTL=30d, reuse=false, basic+post auth");
        };
    }
}