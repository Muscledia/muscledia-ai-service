package com.muscledia.muscledia_ai_service.service;

import com.muscledia.muscledia_ai_service.dto.UserData;
import com.muscledia.muscledia_ai_service.dto.UserDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service responsible for data transformation
 * Single responsibility: convert between DTOs and domain models
 */
@Slf4j
@Service
public class UserDataMapper {

    private static final String DEFAULT_GOAL = "BUILD_MUSCLE";
    private static final String DEFAULT_GENDER = "MALE";
    private static final int DEFAULT_AGE = 25;
    private static final double DEFAULT_HEIGHT = 170.0;
    private static final double DEFAULT_WEIGHT = 70.0;

    /**
     * Convert DTO from external service to domain model
     * Uses record constructor directly
     */
    public UserData toDomain(UserDataDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("UserDataDTO cannot be null");
        }

        if (dto.getUserId() == null) {
            throw new IllegalArgumentException("User ID cannot be null in UserDataDTO");
        }

        log.debug("Converting UserDataDTO to UserData for userId: {}", dto.getUserId());

        // Direct constructor call - no factory method needed
        return new UserData(
                String.valueOf(dto.getUserId()),
                dto.getHeight() != null ? dto.getHeight() : DEFAULT_HEIGHT,
                dto.getWeight() != null ? dto.getWeight() : DEFAULT_WEIGHT,
                dto.getGoalType() != null ? dto.getGoalType() : DEFAULT_GOAL,
                dto.getGender() != null ? dto.getGender() : DEFAULT_GENDER,
                dto.getAge() != null ? dto.getAge() : DEFAULT_AGE
        );
    }
}