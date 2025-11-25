package com.muscledia.muscledia_ai_service.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WorkoutRecommendation(
    @JsonProperty("suggestedWorkoutRoutine") String suggestedWorkoutRoutine,
    @JsonProperty("routineId") String routineId,
    @JsonProperty("description") String description,
    @JsonProperty("difficultyLevel") String difficultyLevel,
    @JsonProperty("workoutSplit") String workoutSplit
) {
}