package com.muscledia.muscledia_ai_service.service;

import com.muscledia.muscledia_ai_service.dto.UserPrincipal;
import com.muscledia.muscledia_ai_service.exception.UnauthorizedException;
import com.muscledia.muscledia_ai_service.security.JwtAuthenticationToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    public Long getCurrentUserId() {
        UserPrincipal principal = getCurrentUser();
        log.debug("Current user ID: {}", principal.getUserId());
        return principal.getUserId();
    }

    public UserPrincipal getCurrentUser() {
        JwtAuthenticationToken auth = getAuthentication();
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        log.debug("Current user: {}", principal);
        return principal;
    }

    public String getCurrentUsername() {
        return getCurrentUser().getUsername();
    }

    public boolean hasRole(String role) {
        return getCurrentUser().hasRole(role);
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public boolean canAccessResource(Long resourceUserId) {
        UserPrincipal user = getCurrentUser();
        return user.isAdmin() || user.getUserId().equals(resourceUserId);
    }

    public boolean isCurrentUser(Long userId) {
        return getCurrentUserId().equals(userId);
    }

    private JwtAuthenticationToken getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthenticationToken)) {
            throw new UnauthorizedException("No authenticated user found");
        }
        return jwtAuthenticationToken;
    }
}