package com.muscledia.muscledia_ai_service.dto;

/**
 * Domain model for user data
 * Immutable and validated via compact constructor
 */
public record UserData(
        String userId,
        double height,
        double weight,
        String goalType,
        String gender,
        int age
) {
    /**
     * Compact constructor with validation
     * Executed automatically when creating instances
     */
    public UserData {
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
    }
}