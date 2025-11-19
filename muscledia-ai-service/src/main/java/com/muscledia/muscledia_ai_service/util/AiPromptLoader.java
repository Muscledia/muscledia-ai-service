package com.muscledia.muscledia_ai_service.util;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;

public class AiPromptLoader {

    public static String loadPrompt(String filename) {
        try {
            ClassPathResource resource = new ClassPathResource("static/ai/" + filename);
            return Files.readString(resource.getFile().toPath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load AI prompt: " + filename, e);
        }
    }

    public static String loadJsonData(String filename) {
        try {
            ClassPathResource resource = new ClassPathResource("data/" + filename);
            return Files.readString(resource.getFile().toPath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load JSON data: " + filename, e);
        }
    }
}
