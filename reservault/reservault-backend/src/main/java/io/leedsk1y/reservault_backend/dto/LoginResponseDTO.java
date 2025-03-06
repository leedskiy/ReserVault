package io.leedsk1y.reservault_backend.dto;

import io.leedsk1y.reservault_backend.models.enums.EAuthProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponseDTO {
    private String token;
    private UUID userId;
    private String email;
    private String profileImage;
    private boolean verified;
    private EAuthProvider authProvider;
    private Collection<? extends GrantedAuthority> roles;
}
