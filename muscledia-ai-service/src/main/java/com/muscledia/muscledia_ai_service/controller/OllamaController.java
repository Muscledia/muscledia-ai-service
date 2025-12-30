package com.muscledia.muscledia_ai_service.controller;

import com.muscledia.muscledia_ai_service.dto.PreferencesDto;
import com.muscledia.muscledia_ai_service.model.Answer;
import com.muscledia.muscledia_ai_service.model.Question;
import com.muscledia.muscledia_ai_service.model.WorkoutRecommendation;
import com.muscledia.muscledia_ai_service.service.OllamaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

/**
 * Controller for AI operations
 * Responsibility: Handle HTTP requests only, delegate to service layer
 */
@RestController
@RequestMapping("/ollama")
@Slf4j
@CrossOrigin("*")
@Tag(name = "Ollama API", description = "AI-powered workout recommendations")
public class OllamaController {

    private final OllamaService ollamaService;

    public OllamaController(OllamaService ollamaService) {
        this.ollamaService = ollamaService;
    }

    /**
     * General conversational endpoint
     */
    @PostMapping("/postConversationWithModel")
    @Operation(summary = "Ask a question to Ollama model")
    public ResponseEntity<Answer> getAnswer(@Valid @RequestBody Question question) {
        try {
            log.debug("Processing general question");
            Answer answer = ollamaService.getGeneralAnswer(question);
            return ResponseEntity.ok(answer);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid question input: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);

        } catch (Exception e) {
            log.error("Error processing question: {}", e.getMessage(), e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to get response from AI service",
                    e
            );
        }
    }

    /**
     * OPTIMIZED: Get workout recommendation
     *
     * Performance optimizations applied:
     * 1. Minimal data sent to AI (top 10 routines with 6 fields only)
     * 2. Optimized prompt structure
     * 3. Lower AI temperature for faster responses
     * 4. Token limits on AI output
     *
     * Expected response time: 30-120 seconds (down from 5 minutes)
     */
    @PostMapping("/getRecommendation")
    @Operation(
            summary = "Get personalized workout recommendation (OPTIMIZED)",
            description = "Fast AI recommendation using optimized data and model parameters. " +
                    "Expected response time: 30-120 seconds."
    )
    @SecurityRequirement(name = "bearer-key")
    public ResponseEntity<WorkoutRecommendation> getStructuredAnswer(
            @Valid @RequestBody PreferencesDto preferences,
            HttpServletRequest request) {

        long startTime = System.currentTimeMillis();

        try {
            String jwtToken = extractJwtToken(request);

            log.info("Starting optimized recommendation generation");
            WorkoutRecommendation recommendation = ollamaService.getStructuredAnswer(
                    preferences,
                    jwtToken
            );

            long duration = System.currentTimeMillis() - startTime;
            log.info("Recommendation generated in {} seconds", duration / 1000.0);

            return ResponseEntity.ok(recommendation);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid input: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);

        } catch (ResponseStatusException e) {
            throw e;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Error after {} seconds: {}", duration / 1000.0, e.getMessage(), e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to generate recommendation: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Extract JWT token from request header
     * Presentation layer logic - validate authorization header format
     */
    private String extractJwtToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header");
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authorization header with Bearer token is required"
            );
        }

        return authHeader.substring(7);
    }
}