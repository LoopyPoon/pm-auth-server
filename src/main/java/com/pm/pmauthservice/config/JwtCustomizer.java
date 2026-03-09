package com.pm.pmauthservice.config;

import com.pm.pmauthservice.entity.Role;
import com.pm.pmauthservice.entity.User;
import com.pm.pmauthservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class JwtCustomizer {

    private final UserRepository userRepository;

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {
            if (!"access_token".equals(context.getTokenType().getValue())) {
                return;
            }

            String principalName = context.getPrincipal().getName();
            log.debug("Customizing token for principal: {}", principalName);

            Optional<User> userOpt = userRepository.findByEmail(principalName);
            if (userOpt.isEmpty()) {
                userOpt = userRepository.findByUsername(principalName);
            }

            userOpt.ifPresent(user -> {
                Set<String> roles = user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet());

                context.getClaims().claim("roles", roles);
                context.getClaims().claim("username", user.getUsername());
                context.getClaims().claim("user_id", user.getId().toString());

                log.debug("Added claims: roles={}, username={}, user_id={}",
                        roles, user.getUsername(), user.getId());
            });
        };
    }
}
