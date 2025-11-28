package com.muscledia.muscledia_ai_service.security;

import com.muscledia.muscledia_ai_service.dto.UserPrincipal;
import com.muscledia.muscledia_ai_service.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationWebFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            JwtAuthenticationToken authentication = validateAuthentication(token);
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            filterChain.doFilter(request, response);
        } catch (RuntimeException ex) {
            log.error("JWT authentication error: {}", ex.getMessage());
            handleAuthenticationError(response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private JwtAuthenticationToken validateAuthentication(String token) {
        if (!jwtService.validateToken(token)) {
            throw new AuthenticationServiceException("Invalid JWT token");
        }

        Long userId = jwtService.extractUserId(token);
        String username = jwtService.extractUsername(token);
        List<String> roles = jwtService.extractRoles(token);

        log.debug("Authenticated user: {} with role: {}", username, roles);

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .toList();

        UserPrincipal principal = new UserPrincipal(userId, username, authorities);
        return new JwtAuthenticationToken(principal, token, authorities);
    }

    private void handleAuthenticationError(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String body = "{\"error\": \"Unauthorized\", \"message\": \"Invalid or missing authentication token\"}";
        response.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
    }
}