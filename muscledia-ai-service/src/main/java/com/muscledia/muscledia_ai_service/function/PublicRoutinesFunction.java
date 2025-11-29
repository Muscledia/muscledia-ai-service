package com.muscledia.muscledia_ai_service.function;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.muscledia.muscledia_ai_service.util.AiPromptLoader;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class PublicRoutinesFunction {

    private final ObjectMapper objectMapper;

    public PublicRoutinesFunction(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Retrieves all available public workout routines as JSON string.
     * This service method is used to provide routines data without embedding
     * the large JSON directly in prompts, reducing token usage.
     * 
     * @return JSON string containing all public workout routines
     */
    public String getPublicRoutinesJson() {
        return AiPromptLoader.loadJsonData("public_routines.json");
    }

    /**
     * Filters public workout routines based on training level and returns filtered JSON.
     * This significantly reduces token usage by only including relevant routines.
     * 
     * @param trainingLevel The user's training level (e.g., "BEGINNER", "INTERMEDIATE", "ADVANCED")
     * @return JSON string containing filtered public workout routines
     */
    public String getFilteredPublicRoutinesJson(String trainingLevel) {
        try {
            String allRoutinesJson = AiPromptLoader.loadJsonData("public_routines.json");
            JsonNode routinesArray = objectMapper.readTree(allRoutinesJson);
            
            if (!routinesArray.isArray()) {
                return allRoutinesJson;
            }

            List<JsonNode> filteredRoutines = new ArrayList<>();
            Iterator<JsonNode> iterator = routinesArray.elements();
            
            while (iterator.hasNext()) {
                JsonNode routine = iterator.next();
                String difficultyLevel = routine.has("difficultyLevel") 
                    ? routine.get("difficultyLevel").asText().toUpperCase() 
                    : "";
                
                // Match routines by training level (case-insensitive)
                if (difficultyLevel.equalsIgnoreCase(trainingLevel.toUpperCase())) {
                    filteredRoutines.add(routine);
                }
            }
            
            // If no routines matched, return all routines as fallback
            if (filteredRoutines.isEmpty()) {
                return allRoutinesJson;
            }
            
            return objectMapper.writeValueAsString(filteredRoutines);
        } catch (Exception e) {
            // On any error, return all routines as fallback
            return getPublicRoutinesJson();
        }
    }
}

