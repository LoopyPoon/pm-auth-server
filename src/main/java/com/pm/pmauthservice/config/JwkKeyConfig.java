package com.pm.pmauthservice.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.rsa.crypto.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class JwkKeyConfig {

    @Value("${app.keys.jks.location}")
    private Resource keyStore;

    @Value("${app.keys.jks.password}")
    private String keyStorePassword;

    @Value("${app.keys.jks.alias}")
    private String keyAlias;

    @Bean
    public ImmutableJWKSet<SecurityContext> jwkSource() {
        KeyStoreKeyFactory ksFactory = new KeyStoreKeyFactory(keyStore, keyStorePassword.toCharArray());
        KeyPair keyPair = ksFactory.getKeyPair(keyAlias, keyStorePassword.toCharArray());
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(keyPair.getPrivate())
                .keyID(keyAlias) // kid = pm-auth
                .build();

        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    @Bean
    public JwtDecoder jwtDecoder(ImmutableJWKSet<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }
}