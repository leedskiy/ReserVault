package io.leedsk1y.reservault_backend.dto;

import io.leedsk1y.reservault_backend.models.entities.User;
import lombok.Getter;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class UserDetailedResponseDTO {
    private final UUID id;
    private final String name;
    private final String email;
    private final String profileImage;
    private final Instant createdAt;
    private final boolean verified;
    private final String authProvider;
    private final Set<String> roles;

    public UserDetailedResponseDTO(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.profileImage = user.getProfileImage();
        this.createdAt = user.getCreatedAt();
        this.verified = user.isVerified();
        this.authProvider = user.getAuthProvider().name();
        this.roles = user.getRoles().stream()
            .map(role -> role.toString())
            .collect(Collectors.toSet());
    }
}