package io.leedsk1y.reservault_backend.config.seeder;

import io.leedsk1y.reservault_backend.models.entities.Role;
import io.leedsk1y.reservault_backend.models.entities.User;
import io.leedsk1y.reservault_backend.models.enums.EAuthProvider;
import io.leedsk1y.reservault_backend.models.enums.ERole;
import io.leedsk1y.reservault_backend.repositories.RoleRepository;
import io.leedsk1y.reservault_backend.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Configuration
public class ManagerSeederConfig {
    private static final Logger logger = LoggerFactory.getLogger(ManagerSeederConfig.class);
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public ManagerSeederConfig(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Seeds a default manager user into the database if one does not already exist.
     */
    public void seedManager() {
        String managerEmail = "manager@example.moc";

        logger.info("Seeding manager user with email: {}", managerEmail);

        if (!userRepository.existsByEmail(managerEmail)) {
            Role adminRole = roleRepository.findByName(ERole.ROLE_MANAGER)
                    .orElseGet(() -> {
                        Role newRole = new Role(ERole.ROLE_MANAGER);
                        return roleRepository.save(newRole);
                    });

            Set<String> roles = new HashSet<>();
            roles.add(adminRole.getName().name());

            User managerUser = new User(
                    UUID.randomUUID(),
                    "Manager",
                    managerEmail,
                    passwordEncoder.encode("pass1234"),
                    null,
                    Instant.now(),
                    true,
                    EAuthProvider.DEFAULT,
                    roles
            );

            userRepository.save(managerUser);
        }
    }
}
