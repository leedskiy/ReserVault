package io.leedsk1y.reservault_backend.controllers;

import java.util.Map;

import io.leedsk1y.reservault_backend.dto.UpdatePasswordDTO;
import io.leedsk1y.reservault_backend.security.jwt.CookieUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.security.core.AuthenticationException;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO request) {
        try {
            return ResponseEntity.ok(authService.registerUser(request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage(), "status", false));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request, HttpServletResponse response) {
        try {
            String jwtToken = authService.authenticateUser(request.getEmail(), request.getPassword());
            CookieUtils.setJwtCookie(response, jwtToken);

            return ResponseEntity.ok(Map.of("message", "Login successful", "status", true));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage(), "status", false));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getAuthenticatedUser(HttpServletRequest request, HttpServletResponse response) {
        try {
            UserDetailedResponseDTO user = authService.getAuthenticatedUser(request, response);
            return ResponseEntity.ok(user);
        } catch (ResponseStatusException e) {
            CookieUtils.clearJwtCookie(response);
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", e.getReason(), "status", false));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logoutUser(request, response);
        return ResponseEntity.ok(Map.of("message", "User logged out successfully", "status", true));
    }

    @PutMapping("/me/password")
    public ResponseEntity<?> updatePassword(@RequestBody UpdatePasswordDTO passwordDTO,
                                            HttpServletRequest request, HttpServletResponse response) {
        try {
            authService.updateUserPassword(passwordDTO, request, response);
            return ResponseEntity.ok(Map.of("message", "Password updated successfully", "status", true));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage(), "status", false));
        }
    }
}