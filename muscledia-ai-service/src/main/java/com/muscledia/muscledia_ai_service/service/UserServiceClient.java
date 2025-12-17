package com.muscledia.muscledia_ai_service.service;

import com.muscledia.muscledia_ai_service.dto.UserDataDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * Client service for communicating with user-service
 * Handles HTTP calls to retrieve user data using JWT authentication
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceClient {

    private final WebClient.Builder webClientBuilder;
    
    @Value("${user-service.url}")
    private String userServiceUrl;
    
    private WebClient webClient;

    /**
     * Initialize WebClient with base URL
     * Using @PostConstruct would require @Component, so we'll initialize lazily
     */
    private WebClient getWebClient() {
        if (webClient == null) {
            webClient = webClientBuilder
                    .baseUrl(userServiceUrl)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .build();
        }
        return webClient;
    }

    /**
     * Retrieves user data from user-service using JWT token
     * 
     * @param jwtToken The JWT token (with or without "Bearer " prefix)
     * @return UserDataDTO containing user information
     * @throws RuntimeException if the request fails or user is not found
     */
    public UserDataDTO getUserData(String jwtToken) {
        log.info("Fetching user data from user-service at: {}/api/users/me/data", userServiceUrl);
        
        // Ensure token has "Bearer " prefix
        String bearerToken = jwtToken.startsWith("Bearer ") ? jwtToken : "Bearer " + jwtToken;
        
        try {
            UserDataDTO userData = getWebClient()
                    .get()
                    .uri("/api/users/me/data")
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .bodyToMono(UserDataDTO.class)
                    .block(); // Blocking call since we're in a non-reactive context
            
            if (userData == null) {
                log.error("User data is null from user-service");
                throw new RuntimeException("Failed to retrieve user data: response was null");
            }
            
            log.info("Successfully retrieved user data for userId: {}", userData.getUserId());
            return userData;
            
        } catch (WebClientResponseException e) {
            log.error("Error calling user-service: Status={}, Message={}", e.getStatusCode(), e.getMessage());
            
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new RuntimeException("Unauthorized: Invalid or expired JWT token", e);
            } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new RuntimeException("User not found", e);
            } else {
                throw new RuntimeException("Failed to retrieve user data from user-service: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("Unexpected error calling user-service: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve user data: " + e.getMessage(), e);
        }
    }
}

