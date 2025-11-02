package com.muscledia.muscledia_ai_service.model;

/**
 * Represents a question used in the AI responder service.
 * <p>
 * This class encapsulates the question text intended for use
 * in the AI responder service. It is implemented as a Java record,
 * providing immutability and concise representation.
 */
public record Question( String question) {
    /**
     * Factory method to create a new {@link Question} instance with validation.
     *
     * @param question the text of the question
     * @return a validated {@link Question} instance
     * @throws IllegalArgumentException if the question is null or blank
     */
    public static Question of(String question) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Question cannot be null or blank.");
        }
        return new Question(question);
    }
}