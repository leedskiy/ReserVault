package io.leedsk1y.reservault_backend.services;

import io.leedsk1y.reservault_backend.dto.RegisterRequestDTO;
import io.leedsk1y.reservault_backend.dto.UserDetailedResponseDTO;
import io.leedsk1y.reservault_backend.models.entities.Hotel;
import io.leedsk1y.reservault_backend.models.entities.HotelManager;
import io.leedsk1y.reservault_backend.models.entities.Role;
import io.leedsk1y.reservault_backend.models.entities.User;
import io.leedsk1y.reservault_backend.models.enums.EAuthProvider;
import io.leedsk1y.reservault_backend.models.enums.ERole;
import io.leedsk1y.reservault_backend.repositories.HotelManagerRepository;
import io.leedsk1y.reservault_backend.repositories.HotelRepository;
import io.leedsk1y.reservault_backend.repositories.RoleRepository;
import io.leedsk1y.reservault_backend.repositories.UserRepository;
import io.leedsk1y.reservault_backend.security.jwt.JwtUtils;
import io.leedsk1y.reservault_backend.security.jwt.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final HotelRepository hotelRepository;
    private final HotelManagerRepository hotelManagerRepository ;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, HotelRepository hotelRepository,
                       HotelManagerRepository hotelManagerRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.hotelRepository = hotelRepository;
        this.hotelManagerRepository = hotelManagerRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    public UserDetailedResponseDTO registerUser(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new RuntimeException("Email is already in use");
        }

        boolean isManager = request.getIsManager();
        Set<Role> roles = new HashSet<>();

        if (isManager) {
            validateManagerDetails(request);
            Role managerRole = roleRepository.findByName(ERole.ROLE_MANAGER)
                    .orElseThrow(() -> new RuntimeException("Error: Manager role not found"));
            roles.add(managerRole);
        } else {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role not found"));
            roles.add(userRole);
        }

        User user = new User(
                UUID.randomUUID(),
                request.getName(),
                request.getEmail().toLowerCase(),
                passwordEncoder.encode(request.getPassword()),
                null,
                Instant.now(),
                !isManager, // not verified if manager
                EAuthProvider.DEFAULT,
                roles.stream().map(Role::getName).map(Enum::name).collect(Collectors.toSet())
        );

        user = userRepository.save(user);

        if (isManager) {
            assignHotelsToManager(request.getHotelIdentifiers(), user.getId());
        }

        return new UserDetailedResponseDTO(user);
    }

    private void validateManagerDetails(RegisterRequestDTO request) {
        // 1. check if manager added hotel identifiers
        if (request.getHotelIdentifiers() == null || request.getHotelIdentifiers().isEmpty()) {
            throw new RuntimeException("Managers must be assigned to at least one hotel during registration.");
        }

        // 2. check if hotel identifiers are valid
        Set<String> existingHotelIdentifiers = hotelRepository.findAll()
                .stream()
                .map(Hotel::getIdentifier)
                .collect(Collectors.toSet());

        List<String> invalidIdentifiers = request.getHotelIdentifiers().stream()
                .filter(id -> !existingHotelIdentifiers.contains(id))
                .collect(Collectors.toList());

        if (!invalidIdentifiers.isEmpty()) {
            throw new RuntimeException("Invalid hotel identifiers provided: " + invalidIdentifiers);
        }
    }

    private void assignHotelsToManager(List<String> hotelIdentifiers, UUID managerId) {
        for (String hotelIdentifier : hotelIdentifiers) {
            HotelManager hotelManager = new HotelManager(hotelIdentifier, managerId);
            hotelManagerRepository.save(hotelManager);
        }
    }

    public String authenticateUser(String email, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email.toLowerCase(), password)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userRepository.findByEmail(email.toLowerCase())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            if (user.getRoles().contains("ROLE_MANAGER") && !user.isVerified()) {
                throw new RuntimeException("Manager is not verified");
            }

            return jwtUtils.generateTokenFromUsername(user.getUsername());
        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid email or password");
        }
    }

    public UserDetailedResponseDTO getAuthenticatedUser(HttpServletRequest request, HttpServletResponse response) {
        String token = jwtUtils.getJwtFromCookies(request);

        if (token == null || !jwtUtils.validateJwtToken(token, response)) {
            CookieUtils.clearJwtCookie(response);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }

        String email = jwtUtils.getUserNameFromJwtToken(token);

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        return new UserDetailedResponseDTO(userOptional.get());
    }

    public void logoutUser(HttpServletRequest request, HttpServletResponse response) {
        String token = jwtUtils.getJwtFromCookies(request);

        if (token != null) {
            jwtUtils.blacklistToken(token);
        }

        CookieUtils.clearJwtCookie(response);
    }
}