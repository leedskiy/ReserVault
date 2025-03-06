package io.leedsk1y.reservault_backend.repositories;

import io.leedsk1y.reservault_backend.models.enums.ERole;
import io.leedsk1y.reservault_backend.models.entities.Role;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends MongoRepository<Role, UUID> {
    Optional<Role> findByName(ERole name);
}