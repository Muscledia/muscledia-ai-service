package com.muscledia.muscledia_ai_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Service responsible for loading resources from classpath
 * Single responsibility: resource loading only
 */
@Slf4j
@Service
public class ResourceLoaderService {

    private static final String PROMPT_PATH = "static/ai/";
    private static final String DATA_PATH = "data/";

    /**
     * Load AI prompt from classpath
     */
    public String loadPrompt(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }

        String path = PROMPT_PATH + filename;
        log.debug("Loading prompt from: {}", path);

        return loadResource(path);
    }

    /**
     * Load JSON data from classpath
     */
    public String loadJsonData(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }

        String path = DATA_PATH + filename;
        log.debug("Loading JSON data from: {}", path);

        return loadResource(path);
    }

    /**
     * Internal method to load resource content
     */
    private String loadResource(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);

            if (!resource.exists()) {
                throw new IllegalArgumentException("Resource not found: " + path);
            }

            try (InputStream inputStream = resource.getInputStream()) {
                String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                log.debug("Successfully loaded resource: {} ({} characters)", path, content.length());
                return content;
            }
        } catch (IOException e) {
            log.error("Failed to load resource: {}", path, e);
            throw new RuntimeException("Failed to load resource: " + path, e);
        }
    }
}