package io.leedsk1y.reservault_backend.controllers;

import java.util.Map;

import io.leedsk1y.reservault_backend.dto.UpdatePasswordDTO;
import io.leedsk1y.reservault_backend.security.jwt.CookieUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.leedsk1y.reservault_backend.dto.LoginRequestDTO;
import io.leedsk1y.reservault_backend.dto.RegisterRequestDTO;
import io.leedsk1y.reservault_backend.dto.UserDetailedResponseDTO;
import io.leedsk1y.reservault_backend.services.AuthService;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user or manager.
     * @param request The registration data including user info and hotel assignments.
     * @return ResponseEntity with registration result and user details or error message.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO request) {
        logger.info("Attempting to register user with email: {}", request.getEmail());
        try {
            return ResponseEntity.ok(authService.registerUser(request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage(), "status", false));
        }
    }

    /**
     * Authenticates a user and sets the JWT in a cookie on successful login.
     * @param request The login credentials (email and password).
     * @param response HTTP response used to set the JWT cookie.
     * @return ResponseEntity indicating login success or failure.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request, HttpServletResponse response) {
        logger.info("Attempting login for email: {}", request.getEmail());
        try {
            String jwtToken = authService.authenticateUser(request.getEmail(), request.getPassword());
            CookieUtils.setJwtCookie(response, jwtToken);

            return ResponseEntity.ok(Map.of("message", "Login successful", "status", true));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage(), "status", false));
        }
    }

    /**
     * Retrieves the currently authenticated user's details using the JWT cookie.
     * @param request HTTP request containing JWT cookie.
     * @param response HTTP response used to clear cookie if token is invalid.
     * @return ResponseEntity containing user details or an error message.
     */
    @GetMapping("/me")
    public ResponseEntity<?> getAuthenticatedUser(HttpServletRequest request, HttpServletResponse response) {
        logger.info("Fetching authenticated user");
        try {
            UserDetailedResponseDTO user = authService.getAuthenticatedUser(request, response);
            return ResponseEntity.ok(user);
        } catch (ResponseStatusException e) {
            CookieUtils.clearJwtCookie(response);
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", e.getReason(), "status", false));
        }
    }

    /**
     * Logs out the user by blacklisting the JWT and clearing the JWT cookie.
     * @param request HTTP request containing JWT cookie.
     * @param response HTTP response used to clear the cookie.
     * @return ResponseEntity indicating successful logout.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        logger.info("Logging out user");
        authService.logoutUser(request, response);
        return ResponseEntity.ok(Map.of("message", "User logged out successfully", "status", true));
    }

    /**
     * Updates the password for the authenticated user.
     * @param passwordDTO Contains current and new password data.
     * @param request HTTP request containing JWT cookie.
     * @param response HTTP response used for cookie operations if needed.
     * @return ResponseEntity indicating success or failure of password update.
     */
    @PutMapping("/me/password")
    public ResponseEntity<?> updatePassword(@RequestBody UpdatePasswordDTO passwordDTO,
                                            HttpServletRequest request, HttpServletResponse response) {
        logger.info("Attempting to update password for authenticated user");
        try {
            authService.updateUserPassword(passwordDTO, request, response);
            return ResponseEntity.ok(Map.of("message", "Password updated successfully", "status", true));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage(), "status", false));
        }
    }
}