package com.muscledia.muscledia_ai_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO matching the response from user-service /api/users/me/data endpoint
 * This matches the UserDataDTO structure from user-service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDataDTO {
    private Long userId;
    private Double height;
    private Double weight;
    private String goalType;
    private String gender;
    private Integer age;
}

