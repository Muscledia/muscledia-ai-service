package com.muscledia.muscledia_ai_service.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Domain model for workout recommendations
 * Immutable with validation
 */
public record WorkoutRecommendation(
        @JsonProperty("suggestedWorkoutRoutine") String suggestedWorkoutRoutine,
        @JsonProperty("routineId") String routineId,
        @JsonProperty("description") String description,
        @JsonProperty("difficultyLevel") String difficultyLevel,
        @JsonProperty("workoutSplit") String workoutSplit
) {
    /**
     * Compact constructor for validation
     */
    public WorkoutRecommendation {
        if (suggestedWorkoutRoutine == null || suggestedWorkoutRoutine.isBlank()) {
            throw new IllegalArgumentException("Suggested workout routine cannot be null or blank");
        }
        if (routineId == null || routineId.isBlank()) {
            throw new IllegalArgumentException("Routine ID cannot be null or blank");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be null or blank");
        }
    }
}