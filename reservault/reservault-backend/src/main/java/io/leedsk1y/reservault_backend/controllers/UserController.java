package io.leedsk1y.reservault_backend.controllers;

import io.leedsk1y.reservault_backend.dto.UserDetailedResponseDTO;
import io.leedsk1y.reservault_backend.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDetailedResponseDTO> getUser(HttpServletRequest request, HttpServletResponse response) {
        logger.info("Fetching authenticated user details");
        return ResponseEntity.ok(userService.getAuthenticatedUser(request, response));
    }

    @PutMapping("/me/name")
    public ResponseEntity<Map<String, Object>> updateName(@RequestParam String name,
                                                          HttpServletRequest request,
                                                          HttpServletResponse response) {
        logger.info("Updating user name to: {}", name);
        userService.updateUserName(name, request, response);
        return ResponseEntity.ok(Map.of("message", "Name updated successfully", "status", true));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Map<String, Object>> deleteUser(HttpServletRequest request, HttpServletResponse response) {
        logger.info("Attempting to delete authenticated user");
        UserDetailedResponseDTO user = userService.getAuthenticatedUser(request, response);
        if (user.getRoles().contains("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Admin accounts cannot be deleted", "status", false));
        }

        userService.deleteAuthenticatedUser(request, response);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully", "status", true));
    }
}