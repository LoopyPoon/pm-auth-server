package com.pm.pmauthservice.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;

@Configuration
public class RegisteredClientPolicyConfig {

    @Bean
    @DependsOn("seedSpaClient")
    CommandLineRunner enforceRegisteredClientPolicy(RegisteredClientRepository repo) {
        return args -> {
            var rc = repo.findByClientId("pm-spa");
            if (rc == null) {
                System.out.println("[policy] pm-spa not found (unexpected)");
                return;
            }

            System.out.println("[policy] BEFORE: grants=" + rc.getAuthorizationGrantTypes()
                    + ", scopes=" + rc.getScopes()
                    + ", AT_TTL=" + rc.getTokenSettings().getAccessTokenTimeToLive()
                    + ", RT_TTL=" + rc.getTokenSettings().getRefreshTokenTimeToLive()
                    + ", reuseRT=" + rc.getTokenSettings().isReuseRefreshTokens());

            var desiredToken = TokenSettings.builder()
                    .accessTokenTimeToLive(Duration.ofMinutes(15))
                    .refreshTokenTimeToLive(Duration.ofDays(30))
                    .reuseRefreshTokens(false)
                    .build();

            var desiredClient = ClientSettings.builder()
                    .requireProofKey(true)
                    .requireAuthorizationConsent(true)
                    .build();

            var builder = RegisteredClient.from(rc)
                    .tokenSettings(desiredToken)
                    .clientSettings(desiredClient);

            if (rc.getAuthorizationGrantTypes().stream().noneMatch(AuthorizationGrantType.REFRESH_TOKEN::equals)) {
                builder.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN);
            }
            if (!rc.getScopes().contains("offline_access")) {
                builder.scope("offline_access");
            }

            repo.save(builder.build());

            var after = repo.findByClientId("pm-spa");
            System.out.println("[policy] AFTER: grants=" + after.getAuthorizationGrantTypes()
                    + ", scopes=" + after.getScopes()
                    + ", AT_TTL=" + after.getTokenSettings().getAccessTokenTimeToLive()
                    + ", RT_TTL=" + after.getTokenSettings().getRefreshTokenTimeToLive()
                    + ", reuseRT=" + after.getTokenSettings().isReuseRefreshTokens());
        };
    }
}