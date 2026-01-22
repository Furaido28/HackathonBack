package com.helha.thelostgrimoire.security;

import com.helha.thelostgrimoire.application.repositories.utils.CurrentUserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * Filter that intercepts every request to validate JWT tokens stored in cookies
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Helper method to find and retrieve the 'jwt' cookie from the request
     */
    private String extractJwtFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        return Arrays.stream(request.getCookies())
                .filter(c -> "jwt".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            /**
             * Attempt to extract and validate the token for each incoming request
             */
            String token = extractJwtFromCookie(request);

            if (token != null && jwtService.isTokenValid(token)) {
                Long userId = jwtService.extractUserId(token);

                /**
                 * Build the authentication token and inject it into Spring Security's context
                 */
                var auth = new UsernamePasswordAuthenticationToken(
                        userId, null, Collections.emptyList()
                );

                SecurityContextHolder.getContext().setAuthentication(auth);

                // Pour tes handlers CQRS
                /**
                 * Populate the custom thread-local context for use within CQRS command/query handlers
                 */
                CurrentUserContext.setUserId(userId);
            }

            /**
             * Continue the filter chain execution
             */
            filterChain.doFilter(request, response);
        } finally {
            /**
             * Critical: Clear the ThreadLocal context to prevent memory leaks and data cross-contamination
             */
            CurrentUserContext.clear();
        }
    }
}