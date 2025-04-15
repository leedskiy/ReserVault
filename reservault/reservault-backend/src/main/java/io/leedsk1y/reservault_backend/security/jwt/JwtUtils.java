package io.leedsk1y.reservault_backend.security.jwt;

import java.security.Key;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.SecretKey;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;

    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    /**
     * Retrieves the JWT token from cookies in the incoming request.
     * @param request HTTP request potentially containing cookies.
     * @return The JWT token string if present, otherwise null.
     */
    public String getJwtFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Generates a signed JWT token for the specified username.
     * @param username The username to include in the JWT subject.
     * @return A signed JWT token string.
     */
    public String generateTokenFromUsername(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key())
                .compact();
    }

    /**
     * Extracts the username from a valid JWT token.
     * @param token The JWT token to parse.
     * @return The username stored as the subject in the token.
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Adds a JWT token to the in-memory blacklist, making it invalid for future use.
     * @param token The JWT token to blacklist.
     */
    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
    }

    /**
     * Checks if the given JWT token is blacklisted.
     * @param token The JWT token to verify.
     * @return True if the token is blacklisted, false otherwise.
     */
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    /**
     * Validates a JWT token for signature, expiration, and blacklist status.
     * @param authToken The JWT token to validate.
     * @param response HTTP response used for cookie cleanup if validation fails.
     * @return True if the token is valid, false otherwise.
     */
    public boolean validateJwtToken(String authToken, HttpServletResponse response) {
        if (isTokenBlacklisted(authToken)) {
            logger.error("JWT token is blacklisted");
            CookieUtils.clearJwtCookie(response);
            return false;
        }

        try {
            Jwts.parser().verifyWith((SecretKey) key()).build().parseSignedClaims(authToken);
            return true;
        }
        catch(MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            CookieUtils.clearJwtCookie(response);
        }
        catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
            CookieUtils.clearJwtCookie(response);
        }
        catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
            CookieUtils.clearJwtCookie(response);
        }
        catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
            CookieUtils.clearJwtCookie(response);
        }

        return false;
    }
}