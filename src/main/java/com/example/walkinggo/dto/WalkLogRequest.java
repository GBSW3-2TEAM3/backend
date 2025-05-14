package com.example.walkinggo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class WalkLogRequest {

    @NotNull(message = "시작 시간은 필수입니다.")
    @Schema(description = "산책 시작 시간", example = "2025-05-12T10:00:00")
    private LocalDateTime startTime;

    @NotNull(message = "종료 시간은 필수입니다.")
    @Schema(description = "산책 종료 시간", example = "2025-05-12T11:00:00")
    private LocalDateTime endTime;

    @Schema(description = "산책 시간 (초)", example = "3600")
    private Long durationSeconds;

    @Schema(description = "산책 거리 (미터)", example = "3000.0")
    private Double distanceMeters;

    @Schema(description = "소모 칼로리 (선택적, 백엔드에서 계산 가능)", example = "150.5")
    private Double caloriesBurned;

    @Schema(description = "경로 좌표 JSON 문자열", example = "[{\"lat\":37.123, \"lng\":127.123}, {\"lat\":37.124, \"lng\":127.124}]")
    private String routeCoordinatesJson;
}