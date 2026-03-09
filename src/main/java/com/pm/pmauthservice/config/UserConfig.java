package com.pm.pmauthservice.config;

import com.pm.pmauthservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class UserConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return loginInput -> {
            var appUser = userRepository.findByEmail(loginInput)
                    .or(() -> userRepository.findByUsername(loginInput))
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "Пользователь не найден: " + loginInput
                    ));

            var authorities = appUser.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.getName()))
                    .toList();

            return User.builder()
                    .username(appUser.getEmail())
                    .password(appUser.getPasswordHash())
                    .disabled(!appUser.isEmailVerified())
                    .authorities(authorities)
                    .build();
        };
    }

}
