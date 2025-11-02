package com.muscledia.muscledia_ai_service.service;


import com.muscledia.muscledia_ai_service.model.Answer;
import com.muscledia.muscledia_ai_service.model.Question;
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
    Answer getAnswer(Question question);
}