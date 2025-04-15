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

    /**
     * Retrieves the currently authenticated user's information.
     * @param request HTTP request containing the JWT cookie.
     * @param response HTTP response used for potential cookie handling.
     * @return ResponseEntity containing UserDetailedResponseDTO of the user.
     */
    @GetMapping("/me")
    public ResponseEntity<UserDetailedResponseDTO> getUser(HttpServletRequest request, HttpServletResponse response) {
        logger.info("Fetching authenticated user details");
        return ResponseEntity.ok(userService.getAuthenticatedUser(request, response));
    }

    /**
     * Updates the name of the authenticated user.
     * @param name The new name to set.
     * @param request HTTP request with authentication token.
     * @param response HTTP response used for handling cookies or errors.
     * @return ResponseEntity with a success message and status.
     */
    @PutMapping("/me/name")
    public ResponseEntity<Map<String, Object>> updateName(@RequestParam String name,
                                                          HttpServletRequest request,
                                                          HttpServletResponse response) {
        logger.info("Updating user name to: {}", name);
        userService.updateUserName(name, request, response);
        return ResponseEntity.ok(Map.of("message", "Name updated successfully", "status", true));
    }

    /**
     * Deletes the authenticated user unless the user has an admin role.
     * @param request HTTP request containing JWT for authentication.
     * @param response HTTP response to handle cookie cleanup.
     * @return ResponseEntity indicating the success or failure of the deletion.
     */
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