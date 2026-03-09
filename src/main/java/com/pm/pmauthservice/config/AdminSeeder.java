package com.pm.pmauthservice.config;

import com.pm.pmauthservice.entity.Role;
import com.pm.pmauthservice.entity.User;
import com.pm.pmauthservice.repository.RoleRepository;
import com.pm.pmauthservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AdminSeeder {

    @Bean
    CommandLineRunner seedAdmin(UserRepository userRepository,
                                RoleRepository roleRepository,
                                PasswordEncoder encoder) {
        return args -> {
            if (userRepository.existsByEmail("admin@pm.local")) {
                System.out.println("[seed] admin@pm.local already exists");
                return;
            }

            Role userRole = roleRepository.findByName(Role.USER)
                    .orElseThrow(() -> new IllegalStateException("ROLE_USER not found"));
            Role adminRole = roleRepository.findByName(Role.ADMIN)
                    .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not found"));

            User admin = User.builder()
                    .username("admin")
                    .email("admin@pm.local")
                    .passwordHash(encoder.encode("admin12345"))
                    .emailVerified(true)
                    .build();

            admin.addRole(userRole);
            admin.addRole(adminRole);

            userRepository.save(admin);
            System.out.println("[seed] created admin@pm.local (ROLE_USER + ROLE_ADMIN)");
        };
    }
}
