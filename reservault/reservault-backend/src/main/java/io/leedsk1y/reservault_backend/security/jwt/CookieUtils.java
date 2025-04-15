package io.leedsk1y.reservault_backend.security.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CookieUtils {
    private static final Logger logger = LoggerFactory.getLogger(CookieUtils.class);

    /**
     * Sets a secure, HTTP-only JWT cookie in the response.
     * @param response HTTP servlet response where the cookie will be added.
     * @param jwtToken The JWT token to include in the cookie.
     */
    public static void setJwtCookie(HttpServletResponse response, String jwtToken) {
        Cookie cookie = new Cookie("jwt", jwtToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(3 * 24 * 60 * 60); // 3 days
        cookie.setAttribute("SameSite", "Strict");

        response.addCookie(cookie);
        logger.debug("JWT cookie set successfully.");
    }

    /**
     * Clears the JWT cookie from the client by setting its max age to zero.
     * @param response HTTP servlet response used to add the expired cookie.
     */
    public static void clearJwtCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Strict");

        response.addCookie(cookie);
        logger.debug("JWT cookie cleared.");
    }
}
