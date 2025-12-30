package com.muscledia.muscledia_ai_service.exception.OllamaException;


/**
 * Custom exception for Ollama service errors
 */
public class OllamaException extends RuntimeException {

    public OllamaException(String message) {
        super(message);
    }

    public OllamaException(String message, Throwable cause) {
        super(message, cause);
    }
}
