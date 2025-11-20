package com.muscledia.muscledia_ai_service.util;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class AiPromptLoader {

    public static String loadPrompt(String filename) {
        try {
            ClassPathResource resource = new ClassPathResource("static/ai/" + filename);
            try (InputStream inputStream = resource.getInputStream()) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load AI prompt: " + filename, e);
        }
    }

    public static String loadJsonData(String filename) {
        try {
            ClassPathResource resource = new ClassPathResource("data/" + filename);
            try (InputStream inputStream = resource.getInputStream()) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load JSON data: " + filename, e);
        }
    }
}
