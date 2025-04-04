package io.leedsk1y.reservault_backend.services;

import io.leedsk1y.reservault_backend.dto.UserDetailedResponseDTO;
import io.leedsk1y.reservault_backend.models.entities.User;
import io.leedsk1y.reservault_backend.repositories.UserRepository;
import io.leedsk1y.reservault_backend.security.jwt.CookieUtils;
import io.leedsk1y.reservault_backend.security.jwt.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service
public class UserService {
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

    public UserDetailedResponseDTO getAuthenticatedUser(HttpServletRequest request, HttpServletResponse response) {
        User user = extractUserFromToken(request, response);
        return new UserDetailedResponseDTO(user);
    }

    public void updateUserName(String newName, HttpServletRequest request, HttpServletResponse response) {
        User user = extractUserFromToken(request, response);
        user.setName(newName);
        userRepository.save(user);
    }

    public void deleteAuthenticatedUser(HttpServletRequest request, HttpServletResponse response) {
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