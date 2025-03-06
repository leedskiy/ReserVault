package io.leedsk1y.reservault_backend.models.entities;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import io.leedsk1y.reservault_backend.models.enums.EAuthProvider;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "users")
public class User implements UserDetails {
    public User() {
        this.roles = new HashSet<>();
    }

    public User(UUID id, String name, String email, String password, String profileImage,
        Instant createdAt, boolean verified, EAuthProvider authProvider, Set<String> roles) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.profileImage = profileImage;
        this.createdAt = createdAt;
        this.verified = verified;
        this.authProvider = authProvider;
        this.roles = roles != null ? roles : new HashSet<>();
    }

    @Id
    private UUID id;

    private String name;

    private String email;

    private String password;

    private String profileImage;

    @CreatedDate
    private Instant createdAt;

    private boolean verified;

    private EAuthProvider authProvider;

    private Set<String> roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toSet());
    }

    @Override
    public String getUsername() {
        return email; // email as a username
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // (temp)
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // (temp)
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // (temp)
    }

    @Override
    public boolean isEnabled() {
        return true; // (temp)
    }
}