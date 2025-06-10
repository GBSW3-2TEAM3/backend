package com.example.walkinggo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserTargetDistanceUpdateRequest {

    @NotNull(message = "목표 거리는 필수입니다.")
    @DecimalMin(value = "0.0", message = "목표 거리는 0 이상이어야 합니다.")
    @Schema(description = "수정할 사용자 목표 거리 (km)", example = "7.5")
    private Double targetDistanceKm;
}