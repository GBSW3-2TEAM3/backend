package com.example.walkinggo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserWeightUpdateRequest {

    @NotNull(message = "체중은 필수입니다.")
    @Positive(message = "체중은 양수여야 합니다.")
    @Schema(description = "수정할 사용자 체중 (kg)", example = "68.0")
    private Double weightKg;
}