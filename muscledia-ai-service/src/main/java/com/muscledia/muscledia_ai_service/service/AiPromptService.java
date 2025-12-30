package com.muscledia.muscledia_ai_service.service;

import com.muscledia.muscledia_ai_service.dto.UserData;
import com.muscledia.muscledia_ai_service.dto.PreferencesDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Domain service for AI prompts
 * OPTIMIZED: Concise prompts with strict JSON-only instructions
 */
@Slf4j
@Service
public class AiPromptService {

    /**
     * OPTIMIZED: Build concise user context
     */
    public String buildUserContext(UserData userData, PreferencesDto preferences) {
        validateUserData(userData);
        validatePreferences(preferences);

        return String.format("""
            User: %d yrs, %s, %.0f cm, %.0f kg
            Goal: %s
            Training: %s, %d days/week
            """,
                userData.age(),
                userData.gender(),
                userData.height(),
                userData.weight(),
                userData.goalType(),
                preferences.lvlOfTraining(),
                preferences.frequency()
        );
    }

    /**
     * OPTIMIZED: Build strict JSON-only prompt
     * Forces AI to output only JSON, no preamble
     */
    public String buildRecommendationPrompt(String userContext, String routinesJson) {
        if (userContext == null || userContext.trim().isEmpty()) {
            throw new IllegalArgumentException("User context cannot be null or empty");
        }
        if (routinesJson == null || routinesJson.trim().isEmpty()) {
            throw new IllegalArgumentException("Routines JSON cannot be null or empty");
        }

        // CRITICAL: Very strict instructions for JSON-only output
        return String.format("""
            YOU MUST RESPOND WITH ONLY A JSON OBJECT. NO TEXT BEFORE OR AFTER.
            
            %s
            
            Available routines:
            %s
            
            Select the BEST routine and respond with ONLY this JSON (no explanations):
            {
              "suggestedWorkoutRoutine": "exact routine title",
              "routineId": "exact routine id",
              "description": "why this routine suits the user (max 50 words)",
              "difficultyLevel": "exact difficulty from routine",
              "workoutSplit": "exact workout split from routine"
            }
            
            CRITICAL: Output ONLY the JSON object above. No preamble, no explanations, no markdown.
            """, userContext, routinesJson);
    }

    private void validateUserData(UserData userData) {
        if (userData == null) {
            throw new IllegalArgumentException("User data cannot be null");
        }
        if (userData.height() <= 0 || userData.weight() <= 0) {
            throw new IllegalArgumentException("Invalid user physical data");
        }
        if (userData.age() < 18) {
            throw new IllegalArgumentException("User must be at least 18 years old");
        }
    }

    private void validatePreferences(PreferencesDto preferences) {
        if (preferences == null) {
            throw new IllegalArgumentException("Preferences cannot be null");
        }
        if (preferences.frequency() <= 0 || preferences.frequency() > 7) {
            throw new IllegalArgumentException("Training frequency must be between 1 and 7 days");
        }
        if (preferences.lvlOfTraining() == null || preferences.lvlOfTraining().trim().isEmpty()) {
            throw new IllegalArgumentException("Training level cannot be null or empty");
        }
    }
}