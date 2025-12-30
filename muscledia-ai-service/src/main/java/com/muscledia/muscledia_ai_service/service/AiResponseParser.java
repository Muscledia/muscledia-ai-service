package com.muscledia.muscledia_ai_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muscledia.muscledia_ai_service.exception.OllamaException.OllamaException;
import com.muscledia.muscledia_ai_service.model.WorkoutRecommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service responsible for parsing AI responses
 * Single responsibility: clean and parse JSON responses
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiResponseParser {

    private final ObjectMapper objectMapper;

    /**
     * Parse JSON response to WorkoutRecommendation
     * Handles both clean JSON and text with embedded JSON
     */
    public WorkoutRecommendation parseRecommendation(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON response cannot be null or empty");
        }

        try {
            log.debug("Raw AI response length: {} characters", jsonResponse.length());
            log.debug("Raw response preview: {}",
                    jsonResponse.substring(0, Math.min(200, jsonResponse.length())));

            String cleaned = extractAndCleanJson(jsonResponse);

            log.debug("Cleaned JSON length: {} characters", cleaned.length());
            log.debug("Cleaned JSON: {}", cleaned);

            WorkoutRecommendation recommendation = objectMapper.readValue(
                    cleaned,
                    WorkoutRecommendation.class
            );

            validateRecommendation(recommendation);

            log.info("Successfully parsed workout recommendation: {}",
                    recommendation.suggestedWorkoutRoutine());

            return recommendation;

        } catch (Exception e) {
            log.error("Failed to parse AI response. Raw response: {}", jsonResponse);
            log.error("Parse error: {}", e.getMessage(), e);
            throw new OllamaException(
                    "Failed to parse AI response. The AI did not return valid JSON: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Extract and clean JSON from response
     * Handles multiple formats:
     * 1. Pure JSON
     * 2. JSON wrapped in markdown code blocks
     * 3. Text with JSON embedded
     */
    private String extractAndCleanJson(String response) {
        if (response == null) {
            throw new IllegalArgumentException("Response cannot be null");
        }

        String trimmed = response.trim();

        // 1. Check if it's already clean JSON
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return trimmed;
        }

        // 2. Remove markdown code blocks
        String cleaned = removeMarkdownCodeBlocks(trimmed);
        if (cleaned.startsWith("{") && cleaned.endsWith("}")) {
            return cleaned;
        }

        // 3. Extract JSON from text using regex
        String extracted = extractJsonFromText(trimmed);
        if (extracted != null) {
            return extracted;
        }

        // If nothing worked, throw exception
        log.error("Could not extract JSON from response: {}",
                response.substring(0, Math.min(200, response.length())));
        throw new OllamaException("AI response does not contain valid JSON");
    }

    /**
     * Remove markdown code block markers
     */
    private String removeMarkdownCodeBlocks(String text) {
        String result = text;

        // Remove ```json ... ```
        if (result.contains("```json")) {
            result = result.replaceAll("```json\\s*", "");
            result = result.replaceAll("```\\s*$", "");
        }
        // Remove ``` ... ```
        else if (result.contains("```")) {
            result = result.replaceAll("```\\s*", "");
        }

        return result.trim();
    }

    /**
     * Extract JSON object from text using regex
     * Looks for first { and matching } to extract JSON
     */
    private String extractJsonFromText(String text) {
        // Pattern to match JSON objects (handles nested objects)
        Pattern pattern = Pattern.compile(
                "\\{[^{}]*(?:\\{[^{}]*\\}[^{}]*)*\\}",
                Pattern.DOTALL
        );
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            String extracted = matcher.group();
            log.debug("Extracted JSON from text using regex");
            return extracted;
        }

        return null;
    }

    /**
     * Validate parsed recommendation has required fields
     */
    private void validateRecommendation(WorkoutRecommendation recommendation) {
        if (recommendation.suggestedWorkoutRoutine() == null ||
                recommendation.suggestedWorkoutRoutine().trim().isEmpty()) {
            throw new OllamaException("AI response missing suggestedWorkoutRoutine");
        }

        if (recommendation.routineId() == null ||
                recommendation.routineId().trim().isEmpty()) {
            throw new OllamaException("AI response missing routineId");
        }

        if (recommendation.description() == null ||
                recommendation.description().trim().isEmpty()) {
            throw new OllamaException("AI response missing description");
        }

        log.debug("Recommendation validated: {}",
                recommendation.suggestedWorkoutRoutine());
    }
}