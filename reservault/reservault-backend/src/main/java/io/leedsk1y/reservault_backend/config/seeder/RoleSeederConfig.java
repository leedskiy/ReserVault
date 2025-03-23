package io.leedsk1y.reservault_backend.config.seeder;

import io.leedsk1y.reservault_backend.models.enums.ERole;
import io.leedsk1y.reservault_backend.models.entities.Role;
import io.leedsk1y.reservault_backend.repositories.RoleRepository;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class RoleSeederConfig {

    private final RoleRepository roleRepository;

    public RoleSeederConfig(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Bean(name = "seedRoles")
    public ApplicationRunner seedRoles() {
        return args -> {
            Arrays.stream(ERole.values())
                .forEach(roleEnum -> {
                    if (!roleRepository.findByName(roleEnum).isPresent()) {
                        Role role = new Role(roleEnum);
                        roleRepository.save(role);
                    }
                });
        };
    }
}
