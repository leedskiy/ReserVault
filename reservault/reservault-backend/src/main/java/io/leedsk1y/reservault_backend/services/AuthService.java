package io.leedsk1y.reservault_backend.services;

import io.leedsk1y.reservault_backend.dto.LoginResponseDTO;
import io.leedsk1y.reservault_backend.dto.RegisterRequestDTO;
import io.leedsk1y.reservault_backend.dto.UserDetailedResponseDTO;
import io.leedsk1y.reservault_backend.models.entities.Role;
import io.leedsk1y.reservault_backend.models.entities.User;
import io.leedsk1y.reservault_backend.models.enums.EAuthProvider;
import io.leedsk1y.reservault_backend.models.enums.ERole;
import io.leedsk1y.reservault_backend.repositories.RoleRepository;
import io.leedsk1y.reservault_backend.repositories.UserRepository;
import io.leedsk1y.reservault_backend.security.jwt.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
        PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
        JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    public UserDetailedResponseDTO registerUser(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new RuntimeException("Email is already in use");
        }

        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
            .orElseThrow(() -> new RuntimeException("Error: Role not found"));
        roles.add(userRole);

        User user = new User(
            UUID.randomUUID(),
            request.getName(),
            request.getEmail().toLowerCase(),
            passwordEncoder.encode(request.getPassword()),
            null,
            Instant.now(),
            true,
            EAuthProvider.DEFAULT,
            roles.stream().map(Role::getName).map(Enum::name).collect(Collectors.toSet())
        );

        return new UserDetailedResponseDTO(userRepository.save(user));
    }

    public LoginResponseDTO authenticateUser(String email, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email.toLowerCase(), password));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new RuntimeException("User not found"));

            String jwtToken = jwtUtils.generateTokenFromUsername(userDetails);

            return new LoginResponseDTO(
                jwtToken,
                user.getId(),
                user.getEmail(),
                user.getProfileImage(),
                user.isVerified(),
                user.getAuthProvider(),
                user.getAuthorities()
            );
        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid email or password");
        }
    }

    public void logout(String token) {
        if (token != null) {
            jwtUtils.blacklistToken(token);
        }
        SecurityContextHolder.clearContext();
    }

    public UserDetailedResponseDTO getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return new UserDetailedResponseDTO(user);
    }
}