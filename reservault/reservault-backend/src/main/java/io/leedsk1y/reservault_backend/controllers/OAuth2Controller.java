package io.leedsk1y.reservault_backend.controllers;

import io.leedsk1y.reservault_backend.dto.UserDetailedResponseDTO;
import io.leedsk1y.reservault_backend.services.OAuth2Service;
import io.leedsk1y.reservault_backend.security.jwt.CookieUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/oauth2/login")
public class OAuth2Controller {
    private static final Logger logger = LoggerFactory.getLogger(OAuth2Controller.class);

    @Value("${spring.frontend.url}")
    private String frontendUrl;

    private final OAuth2Service oAuth2Service;

    public OAuth2Controller(OAuth2Service oAuth2Service) {
        this.oAuth2Service = oAuth2Service;
    }

    /**
     * Redirects the user to the Google OAuth2 authorization endpoint.
     * @param response HTTP response used to send the redirect.
     * @throws IOException If redirection fails.
     */
    @GetMapping("/google")
    public void redirectToGoogleAuth(HttpServletResponse response) throws IOException {
        logger.info("Redirecting to Google OAuth2 authorization endpoint");
        response.sendRedirect("/oauth2/authorization/google");
    }

    /**
     * Handles successful OAuth2 authentication by generating a JWT token and setting it as a cookie.
     * Then redirects the user to the frontend dashboard.
     * @param response HTTP response used to set cookies and redirect.
     * @param authentication The authenticated user's token.
     * @throws IOException If token generation or redirect fails.
     */
    @GetMapping("/success")
    public void handleOAuth2Success(HttpServletResponse response, Authentication authentication) throws IOException {
        logger.info("Handling successful OAuth2 login for user: {}", authentication.getName());
        try {
            String jwtToken = oAuth2Service.handleOAuth2Authentication((OAuth2AuthenticationToken) authentication);

            CookieUtils.setJwtCookie(response, jwtToken);

            response.sendRedirect(frontendUrl + "/dashboard");
        } catch (RuntimeException e) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Authentication failed: " + e.getMessage());
        }
    }

    /**
     * Handles a failed OAuth2 login attempt.
     * @return ResponseEntity with an unauthorized error message.
     */
    @GetMapping("/failure")
    public ResponseEntity<String> handleOAuth2Failure() {
        logger.warn("OAuth2 login failed");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("OAuth2 login failed");
    }

    /**
     * Retrieves the currently authenticated user from an OAuth2 session.
     * @param authentication The authentication token of the current session.
     * @return ResponseEntity containing detailed user information.
     */
    @GetMapping("/me")
    public ResponseEntity<UserDetailedResponseDTO> getOAuth2AuthenticatedUser(Authentication authentication) {
        logger.info("Fetching authenticated OAuth2 user: {}", authentication.getName());
        return ResponseEntity.ok(oAuth2Service.getAuthenticatedOAuth2User(authentication));
    }
}