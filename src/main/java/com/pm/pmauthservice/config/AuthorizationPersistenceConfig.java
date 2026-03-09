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
                    .redirectUri("http://localhost/callback")
                    .postLogoutRedirectUri("http://localhost:5173/")
                    .postLogoutRedirectUri("http://localhost/")
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
    public CommandLineRunner seedBffClient(RegisteredClientRepository clients) {
        return args -> {
            if (clients.findByClientId("pm-bff") != null) return;

            var enc = org.springframework.security.crypto.factory.PasswordEncoderFactories.createDelegatingPasswordEncoder();
            var token = org.springframework.security.oauth2.server.authorization.settings.TokenSettings.builder()
                    .accessTokenTimeToLive(java.time.Duration.ofMinutes(15))
                    .refreshTokenTimeToLive(java.time.Duration.ofDays(30))
                    .reuseRefreshTokens(false)
                    .build();

            var client = org.springframework.security.oauth2.server.authorization.client.RegisteredClient.withId(java.util.UUID.randomUUID().toString())
                    .clientId("pm-bff")
                    .clientSecret(enc.encode("bff-secret"))
                    .clientName("PoorMusic BFF (Local)")
                    .clientAuthenticationMethod(org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .clientAuthenticationMethod(org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_POST)
                    .authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType.REFRESH_TOKEN)
                    .redirectUri("http://localhost:8080/login/oauth2/code/pm-bff")
                    .scope(org.springframework.security.oauth2.core.oidc.OidcScopes.OPENID)
                    .scope(org.springframework.security.oauth2.core.oidc.OidcScopes.PROFILE)
                    .scope("offline_access")
                    .scope("catalog.read")
                    .tokenSettings(token)
                    .clientSettings(org.springframework.security.oauth2.server.authorization.settings.ClientSettings.builder()
                            .requireAuthorizationConsent(true)
                            .requireProofKey(false)
                            .build())
                    .build();

            clients.save(client);
            System.out.println("[seedBffClient] created pm-bff (redirect http://localhost:8080/login/oauth2/code/pm-bff)");
        };
    }

}