package com.muscledia.muscledia_ai_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Service responsible for filtering workout routines
 * Single responsibility: business logic for filtering
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoutineFilterService {

    private final ObjectMapper objectMapper;
    private final RoutineDataService routineDataService;

    /**
     * Get all routines as JSON string
     */
    public String getAllRoutinesJson() {
        return routineDataService.loadAllRoutines();
    }

    /**
     * Filter routines by training level
     * Business logic: match routines to user's training level
     */
    public String getFilteredRoutinesJson(String trainingLevel) {
        if (trainingLevel == null || trainingLevel.trim().isEmpty()) {
            log.warn("Training level is null or empty, returning all routines");
            return getAllRoutinesJson();
        }

        try {
            String allRoutinesJson = routineDataService.loadAllRoutines();
            JsonNode routinesArray = objectMapper.readTree(allRoutinesJson);

            if (!routinesArray.isArray()) {
                log.warn("Routines data is not an array, returning as is");
                return allRoutinesJson;
            }

            List<JsonNode> filteredRoutines = filterByDifficultyLevel(
                    routinesArray,
                    trainingLevel
            );

            if (filteredRoutines.isEmpty()) {
                log.info("No routines found for level: {}, returning all routines", trainingLevel);
                return allRoutinesJson;
            }

            String result = objectMapper.writeValueAsString(filteredRoutines);
            log.info("Filtered {} routines for level: {}", filteredRoutines.size(), trainingLevel);

            return result;

        } catch (Exception e) {
            log.error("Error filtering routines for level: {}", trainingLevel, e);
            return getAllRoutinesJson();
        }
    }

    /**
     * Business logic: filter routines by difficulty level
     */
    private List<JsonNode> filterByDifficultyLevel(JsonNode routinesArray, String targetLevel) {
        List<JsonNode> filtered = new ArrayList<>();
        Iterator<JsonNode> iterator = routinesArray.elements();

        String normalizedTarget = targetLevel.trim().toUpperCase();

        while (iterator.hasNext()) {
            JsonNode routine = iterator.next();

            if (!routine.has("difficultyLevel")) {
                continue;
            }

            String difficultyLevel = routine.get("difficultyLevel")
                    .asText("")
                    .trim()
                    .toUpperCase();

            if (difficultyLevel.equals(normalizedTarget)) {
                filtered.add(routine);
            }
        }

        return filtered;
    }
}