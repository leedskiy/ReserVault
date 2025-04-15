package io.leedsk1y.reservault_backend.services;

import io.leedsk1y.reservault_backend.dto.RegisterRequestDTO;
import io.leedsk1y.reservault_backend.dto.UpdatePasswordDTO;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
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

    /**
     * Registers a new user or manager with the provided registration details.
     * @param request The registration request containing user info and hotel associations (if manager).
     * @return A detailed response DTO of the registered user.
     */
    public UserDetailedResponseDTO registerUser(RegisterRequestDTO request) {
        logger.info("Registering new user with email: {}, isManager: {}", request.getEmail(), request.getIsManager());
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

    /**
     * Validates hotel identifiers provided during manager registration.
     * @param request The registration request from a manager.
     * @throws RuntimeException If hotel identifiers are missing or invalid.
     */
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

    /**
     * Assigns the specified hotels to a manager during registration.
     * @param hotelIdentifiers List of hotel identifiers.
     * @param managerId UUID of the newly registered manager.
     */
    private void assignHotelsToManager(List<String> hotelIdentifiers, UUID managerId) {
        for (String hotelIdentifier : hotelIdentifiers) {
            HotelManager hotelManager = new HotelManager(hotelIdentifier, managerId);
            hotelManagerRepository.save(hotelManager);
        }
    }

    /**
     * Authenticates a user with email and password, generating a JWT on success.
     * @param email The user's email.
     * @param password The user's password.
     * @return A JWT token for authenticated access.
     * @throws RuntimeException If authentication fails or user is unverified.
     */
    public String authenticateUser(String email, String password) {
        logger.info("Authenticating user with email: {}", email);
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

    /**
     * Retrieves the currently authenticated user based on the JWT in the request cookies.
     * @param request The HTTP request containing JWT.
     * @param response The HTTP response to clear cookies if needed.
     * @return A detailed response DTO of the authenticated user.
     * @throws ResponseStatusException If the token is invalid or user is not found.
     */
    public UserDetailedResponseDTO getAuthenticatedUser(HttpServletRequest request, HttpServletResponse response) {
        logger.info("Retrieving authenticated user from request");
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

    /**
     * Logs out the user by blacklisting the JWT and clearing the cookie.
     * @param request The HTTP request containing the JWT.
     * @param response The HTTP response where the JWT cookie will be cleared.
     */
    public void logoutUser(HttpServletRequest request, HttpServletResponse response) {
        logger.info("Logging out user from request");
        String token = jwtUtils.getJwtFromCookies(request);

        if (token != null) {
            jwtUtils.blacklistToken(token);
        }

        CookieUtils.clearJwtCookie(response);
    }

    /**
     * Updates the password of the currently authenticated user.
     * @param passwordDTO DTO containing the current and new passwords.
     * @param request The HTTP request containing JWT.
     * @param response The HTTP response to clear cookies if needed.
     * @throws RuntimeException If user is OAuth2-based, password is incorrect, or token is invalid.
     */
    public void updateUserPassword(UpdatePasswordDTO passwordDTO, HttpServletRequest request, HttpServletResponse response) {
        logger.info("Attempting to update password for authenticated user");
        String token = jwtUtils.getJwtFromCookies(request);

        if (token == null || !jwtUtils.validateJwtToken(token, response)) {
            CookieUtils.clearJwtCookie(response);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }

        String email = jwtUtils.getUserNameFromJwtToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getPassword() == null) {
            throw new RuntimeException("OAuth2 users cannot change password via this method.");
        }

        if (!passwordEncoder.matches(passwordDTO.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        userRepository.save(user);
    }
}