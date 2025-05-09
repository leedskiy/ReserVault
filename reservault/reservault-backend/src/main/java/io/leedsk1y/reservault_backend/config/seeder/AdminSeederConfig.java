package io.leedsk1y.reservault_backend.config.seeder;

import io.leedsk1y.reservault_backend.models.entities.Role;
import io.leedsk1y.reservault_backend.models.entities.User;
import io.leedsk1y.reservault_backend.models.enums.EAuthProvider;
import io.leedsk1y.reservault_backend.models.enums.ERole;
import io.leedsk1y.reservault_backend.repositories.RoleRepository;
import io.leedsk1y.reservault_backend.repositories.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(AdminSeederConfig.class);
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminSeederConfig(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Seeds a default admin user at application startup if one does not already exist.
     * @return ApplicationRunner that performs the admin seeding logic.
     */
    @Bean
    public ApplicationRunner seedAdminUser() {
        return args -> {
            String adminEmail = "admin@example.moc";

            logger.info("Seeding admin user with email: {}", adminEmail);

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
                    passwordEncoder.encode("pass1234"),
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
