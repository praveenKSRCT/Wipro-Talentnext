package com.training.security.config;

import com.training.security.entity.ERole;
import com.training.security.entity.Role;
import com.training.security.entity.User;
import com.training.security.repository.RoleRepository;
import com.training.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Runs on application startup:
 *  1. Ensures ROLE_USER and ROLE_ADMIN exist in the roles table.
 *  2. Seeds a default admin user (username: admin / password: Admin@123)
 *     ONLY if no admin user exists yet - safe to run repeatedly.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(ERole.ROLE_USER)));

        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(ERole.ROLE_ADMIN)));

        if (!userRepository.existsByUsername("admin")) {
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            roles.add(userRole);

            User admin = User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .enabled(true)
                    .roles(roles)
                    .build();

            userRepository.save(admin);
            log.info("Seeded default admin user -> username: admin | password: Admin@123 (CHANGE AFTER FIRST LOGIN)");
        }
    }
}
