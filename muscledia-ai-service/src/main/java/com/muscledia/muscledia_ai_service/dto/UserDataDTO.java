package com.muscledia.muscledia_ai_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for receiving user data from external services
 * Used only for data transfer - no business logic
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