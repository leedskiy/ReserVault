package io.leedsk1y.reservault_backend.services;

import io.leedsk1y.reservault_backend.dto.UserDetailedResponseDTO;
import io.leedsk1y.reservault_backend.models.entities.User;
import io.leedsk1y.reservault_backend.repositories.UserRepository;
import io.leedsk1y.reservault_backend.security.jwt.CookieUtils;
import io.leedsk1y.reservault_backend.security.jwt.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final UserDeletionService userDeletionService;

    public UserService(UserRepository userRepository,
                       JwtUtils jwtUtils,
                       UserDeletionService userDeletionService) {
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
        this.userDeletionService = userDeletionService;
    }

    /**
     * Retrieves the authenticated user from the JWT token and returns detailed user info.
     * @param request HTTP request containing the JWT cookie.
     * @param response HTTP response for handling token invalidation.
     * @return UserDetailedResponseDTO containing user details.
     * @throws ResponseStatusException If the token is invalid or user not found.
     */
    public UserDetailedResponseDTO getAuthenticatedUser(HttpServletRequest request, HttpServletResponse response) {
        logger.info("Fetching authenticated user details from token");
        User user = extractUserFromToken(request, response);
        return new UserDetailedResponseDTO(user);
    }

    /**
     * Updates the name of the authenticated user.
     * @param newName The new name to set for the user.
     * @param request HTTP request containing the JWT cookie.
     * @param response HTTP response for handling token validation or cleanup.
     * @throws ResponseStatusException If the token is invalid or user not found.
     */
    public void updateUserName(String newName, HttpServletRequest request, HttpServletResponse response) {
        logger.info("Updating authenticated user's name");
        User user = extractUserFromToken(request, response);
        user.setName(newName);
        userRepository.save(user);
    }

    /**
     * Deletes the authenticated user's account.
     * Handles user or manager-specific deletions, but prevents admin deletion.
     * @param request HTTP request containing the JWT cookie.
     * @param response HTTP response used to clear the JWT cookie.
     * @throws ResponseStatusException If token is invalid, user is not found, or user is an admin.
     */
    public void deleteAuthenticatedUser(HttpServletRequest request, HttpServletResponse response) {
        logger.info("Deleting authenticated user");
        User user = extractUserFromToken(request, response);

        if (user.getRoles().contains("ROLE_ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin accounts cannot be deleted");
        }

        if (user.getRoles().contains("ROLE_MANAGER")) {
            userDeletionService.deleteManager(user.getId());
        } else {
            userDeletionService.deleteUser(user.getId());
        }

        CookieUtils.clearJwtCookie(response);
    }

    /**
     * Extracts the user from the JWT token found in the request cookies.
     * @param request HTTP request with JWT cookie.
     * @param response HTTP response for clearing the cookie if invalid.
     * @return The authenticated User entity.
     * @throws ResponseStatusException If the token is missing, invalid, or the user doesn't exist.
     */
    private User extractUserFromToken(HttpServletRequest request, HttpServletResponse response) {
        String token = jwtUtils.getJwtFromCookies(request);

        if (token == null || !jwtUtils.validateJwtToken(token, response)) {
            CookieUtils.clearJwtCookie(response);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }

        String email = jwtUtils.getUserNameFromJwtToken(token);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}