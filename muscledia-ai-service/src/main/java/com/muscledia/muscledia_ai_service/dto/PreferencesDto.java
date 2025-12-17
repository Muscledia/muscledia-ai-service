package com.muscledia.muscledia_ai_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PreferencesDto(
    @Positive int frequency,
    @NotNull String lvlOfTraining
) {
    public static PreferencesDto of(int frequency, String lvlOfTraining) {
        if (frequency <= 0) {
            throw new IllegalArgumentException("Frequency must be positive");
        }
        if (lvlOfTraining == null) {
            throw new IllegalArgumentException("TrainingLevel cannot be null");
        }
        return new PreferencesDto(frequency, lvlOfTraining);
    }
}
