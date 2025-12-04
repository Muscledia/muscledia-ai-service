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

//import jakarta.validation.Valid;
import javax.validation.Valid;

@RestController
@RequestMapping("/ollama")
@Slf4j
@CrossOrigin("*")
@Tag(name = "Ollama API", description = "Operations related to the Ollama model")
public class OllamaController {

    private final OllamaService ollamaService;

    public OllamaController(final OllamaService ollamaService) {
        this.ollamaService = ollamaService;
    }

    @PostMapping("/postConversationWithModel")
    @Operation(summary = "Ask a question to Ollama model and get an answer")
    public ResponseEntity<Answer> getAnswer(@Valid @RequestBody Question question) {
        try {
            Answer answer = ollamaService.getGeneralAnswer(question);
            return ResponseEntity.ok(answer);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get response from Ollama", e);
        }
    }


    @PostMapping("/getRecommendation")
    @Operation(
            summary = "Get structured workout recommendation based on user data and preferences",
            description = "Retrieves user data from user-service using JWT token and generates personalized workout recommendation"
    )
    @SecurityRequirement(name = "bearer-key")
    public ResponseEntity<WorkoutRecommendation> getStructuredAnswer(
            @Valid @RequestBody PreferencesDto preferences,
            HttpServletRequest request) {
        try {
            // Extract JWT token from Authorization header
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header");
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, 
                        "Authorization header with Bearer token is required"
                );
            }
            
            // Extract token (remove "Bearer " prefix)
            String jwtToken = authHeader.substring(7);
            log.debug("Extracted JWT token for user data retrieval");
            
            WorkoutRecommendation recommendation = ollamaService.getStructuredAnswer(preferences, jwtToken);
            return ResponseEntity.ok(recommendation);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (ResponseStatusException e) {
            throw e; // Re-throw ResponseStatusException as-is
        } catch (Exception e) {
            log.error("Error generating workout recommendation: {}", e.getMessage(), e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Failed to get structured response: " + e.getMessage(), 
                    e
            );
        }
    }
}
