package com.muscledia.muscledia_ai_service.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record WorkoutRoutineRequest(
    @NotNull @Valid UserData userData,
    @NotNull @Valid PreferencesDto preferences
) {
}
