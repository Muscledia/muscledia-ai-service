package com.muscledia.muscledia_ai_service.service;

import com.muscledia.muscledia_ai_service.dto.PreferencesDto;
import com.muscledia.muscledia_ai_service.model.Answer;
import com.muscledia.muscledia_ai_service.model.Question;
import com.muscledia.muscledia_ai_service.model.WorkoutRecommendation;
import com.muscledia.muscledia_ai_service.exception.OllamaException.OllamaException;

/**
 * Interface for fetching an AI-generated answer based on a given question.
 */
public interface OllamaService {
    /**
     * Generates an answer for the provided question using Ollama AI model.
     *
     * @param question the question object containing the query details
     * @return an Answer object representing the AI-generated response
     * @throws IllegalArgumentException if the question is null or invalid
     * @throws OllamaException custom exception if there is an error in communicating with the AI service
     */
    Answer getGeneralAnswer(Question question);

    /**
     * Generates a structured workout recommendation based on user data and preferences.
     *
     * @param preferences the PreferencesDto request containing frequency preferences and workout experience
     * @return a WorkoutRecommendation object with the recommended routine
     * @throws IllegalArgumentException if the request is null or invalid
     * @throws OllamaException custom exception if there is an error in communicating with the AI service
     */
    WorkoutRecommendation getStructuredAnswer(PreferencesDto preferences);
}