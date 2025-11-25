package com.muscledia.muscledia_ai_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UserData(
    @NotNull String userId,
    @Positive double height,
    @Positive double weight,
    @NotNull String goalType,
    @NotNull String gender,
    @Positive int age
) {
    public static UserData of(String userId, double height, double weight, String goalType, String gender, int age) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("UserId cannot be null or blank");
        }
        if (height <= 0 || weight <= 0 || age <= 0) {
            throw new IllegalArgumentException("Height, weight, and age must be positive");
        }
        if (gender == null || gender.isBlank()) {
            throw new IllegalArgumentException("Gender cannot be null or blank");
        }
        if (goalType == null) {
            throw new IllegalArgumentException("GoalType cannot be null");
        }
        return new UserData(userId, height, weight, goalType, gender, age);
    }
}
