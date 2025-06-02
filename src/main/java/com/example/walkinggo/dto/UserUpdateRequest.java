package com.example.walkinggo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {

    @Schema(description = "수정할 사용자 체중 (kg)", example = "68.0", nullable = true)
    @Positive(message = "체중은 양수여야 합니다.")
    private Double weightKg;

    @Schema(description = "수정할 사용자 목표 거리 (km)", example = "7.5", nullable = true)
    @DecimalMin(value = "0.0", message = "목표 거리는 0 이상이어야 합니다.")
    private Double targetDistanceKm;
}