package com.muscledia.muscledia_ai_service.config;

import com.muscledia.muscledia_ai_service.security.JwtAuthenticationEntryPoint;
import com.muscledia.muscledia_ai_service.security.JwtAuthenticationWebFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationWebFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final SecurityProperties securityProperties;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint))

                .authorizeHttpRequests(auth -> auth
                        // FIX: Allow Actuator Health Checks (Critical for Docker)
                        .requestMatchers("/actuator/**").permitAll()

                        // Public endpoints
                        .requestMatchers(getPublicEndpoints()).permitAll()

                        // Swagger
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/webjars/**")
                        .permitAll()

                        // TEMP public admin endpoints
                        .requestMatchers(HttpMethod.POST, "/api/admin/data/hevy/populate").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/admin/data/hevy/fetch-all").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/admin/data/hevy/sync-folder-workout-plan-ids").permitAll()

                        // Admin
                        .requestMatchers(getAdminEndpoints()).hasRole("ADMIN")

                        // Specific public GET endpoints
                        .requestMatchers(HttpMethod.GET, "/api/v1/exercises/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/muscle-groups/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/routine-folders/public").permitAll()

                        // Everything else requires auth
                        .anyRequest().authenticated()
                );

        // Add JWT Filter BEFORE UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter,
                org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private String[] getPublicEndpoints() {
        List<String> publicEndpoints = securityProperties.getPublicEndpoints();
        return publicEndpoints != null ? publicEndpoints.toArray(new String[0]) : new String[0];
    }

    private String[] getAdminEndpoints() {
        List<String> adminEndpoints = securityProperties.getAdminEndpoints();
        return adminEndpoints != null ? adminEndpoints.toArray(new String[0]) : new String[0];
    }
}