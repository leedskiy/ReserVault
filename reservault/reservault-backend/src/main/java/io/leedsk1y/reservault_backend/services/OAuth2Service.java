package io.leedsk1y.reservault_backend.services;

import io.leedsk1y.reservault_backend.dto.UserDetailedResponseDTO;
import io.leedsk1y.reservault_backend.models.enums.EAuthProvider;
import io.leedsk1y.reservault_backend.models.enums.ERole;
import io.leedsk1y.reservault_backend.models.entities.Role;
import io.leedsk1y.reservault_backend.models.entities.User;
import io.leedsk1y.reservault_backend.repositories.RoleRepository;
import io.leedsk1y.reservault_backend.repositories.UserRepository;
import io.leedsk1y.reservault_backend.security.jwt.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OAuth2Service {
    private static final Logger logger = LoggerFactory.getLogger(OAuth2Service.class);
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtils jwtUtils;

    public OAuth2Service(UserRepository userRepository, RoleRepository roleRepository, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtUtils = jwtUtils;
    }

    public String handleOAuth2Authentication(OAuth2AuthenticationToken auth2AuthenticationToken) {
        logger.info("Handling OAuth2 authentication for user");

        OAuth2User oAuth2User = auth2AuthenticationToken.getPrincipal();
        String email = Optional.ofNullable((String) oAuth2User.getAttribute("email"))
                .orElseThrow(() -> new RuntimeException("OAuth2 authentication failed: Email not found"));

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createNewUser(oAuth2User));

        String profileImageUrl = oAuth2User.getAttribute("picture");
        if (profileImageUrl != null && !profileImageUrl.equals(user.getProfileImage())) {
            user.setProfileImage(profileImageUrl);
            userRepository.save(user);
        }

        return jwtUtils.generateTokenFromUsername(user.getUsername());
    }

    private User createNewUser(OAuth2User oAuth2User) {
        logger.info("Creating new user via OAuth2");
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role not found"));
        roles.add(userRole);

        User user = new User(
                UUID.randomUUID(),
                oAuth2User.getAttribute("name"),
                oAuth2User.getAttribute("email"),
                null,
                oAuth2User.getAttribute("picture"),
                Instant.now(),
                true,
                EAuthProvider.GOOGLE,
                roles.stream().map(Role::getName).map(Enum::name).collect(Collectors.toSet())
        );

        return userRepository.save(user);
    }

    public UserDetailedResponseDTO getAuthenticatedOAuth2User(Authentication authentication) {
        logger.info("Fetching authenticated OAuth2 user from token");
        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            throw new RuntimeException("Unauthorized: OAuth2 token is missing");
        }

        String email = ((OAuth2AuthenticationToken) authentication).getPrincipal().getAttribute("email");
        return userRepository.findByEmail(email)
                .map(UserDetailedResponseDTO::new)
                .orElseThrow(() -> new RuntimeException("OAuth2 user not found"));
    }
}