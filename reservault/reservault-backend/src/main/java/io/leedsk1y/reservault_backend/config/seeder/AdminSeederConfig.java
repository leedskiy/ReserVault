package io.leedsk1y.reservault_backend.config.seeder;

import io.leedsk1y.reservault_backend.models.entities.Role;
import io.leedsk1y.reservault_backend.models.entities.User;
import io.leedsk1y.reservault_backend.models.enums.EAuthProvider;
import io.leedsk1y.reservault_backend.models.enums.ERole;
import io.leedsk1y.reservault_backend.repositories.RoleRepository;
import io.leedsk1y.reservault_backend.repositories.UserRepository;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Configuration
@DependsOn("seedRoles")
public class AdminSeederConfig {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminSeederConfig(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public ApplicationRunner seedAdminUser() {
        return args -> {
            String adminEmail = "admin@example.moc";

            if (!userRepository.existsByEmail(adminEmail)) {
                Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                    .orElseGet(() -> {
                        Role newRole = new Role(ERole.ROLE_ADMIN);
                        return roleRepository.save(newRole);
                    });

                Set<String> roles = new HashSet<>();
                roles.add(adminRole.getName().name());

                User adminUser = new User(
                    UUID.randomUUID(),
                    "Admin",
                    adminEmail,
                    passwordEncoder.encode("pass"),
                    null,
                    Instant.now(),
                    true,
                    EAuthProvider.DEFAULT,
                    roles
                );

                userRepository.save(adminUser);
            }
        };
    }
}
