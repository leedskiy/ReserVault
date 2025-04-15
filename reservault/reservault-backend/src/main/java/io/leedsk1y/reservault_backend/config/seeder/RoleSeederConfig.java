package io.leedsk1y.reservault_backend.config.seeder;

import io.leedsk1y.reservault_backend.models.enums.ERole;
import io.leedsk1y.reservault_backend.models.entities.Role;
import io.leedsk1y.reservault_backend.repositories.RoleRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class RoleSeederConfig {
    private static final Logger logger = LoggerFactory.getLogger(RoleSeederConfig.class);
    private final RoleRepository roleRepository;

    public RoleSeederConfig(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Seeds all enum-defined roles ({@link ERole}) into the database if they are not already present.
     * @return ApplicationRunner that seeds missing roles
     */
    @Bean(name = "seedRoles")
    public ApplicationRunner seedRoles() {
        return args -> {
            Arrays.stream(ERole.values())
                .forEach(roleEnum -> {
                    if (!roleRepository.findByName(roleEnum).isPresent()) {
                        logger.info("Seeding role: {}", roleEnum.name());
                        Role role = new Role(roleEnum);
                        roleRepository.save(role);
                    }
                });
        };
    }
}
