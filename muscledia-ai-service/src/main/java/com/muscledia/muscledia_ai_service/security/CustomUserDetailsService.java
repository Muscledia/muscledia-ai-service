package com.muscledia.muscledia_ai_service.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Reactive UserDetailsService implementation for JWT-based authentication.
 * Since user details are extracted from JWT tokens, this service is mainly
 * used as a placeholder and should not be called in normal JWT authentication
 * flow.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        throw new UsernameNotFoundException(
                "UserDetails should be extracted from JWT token, not from database lookup");
    }
}