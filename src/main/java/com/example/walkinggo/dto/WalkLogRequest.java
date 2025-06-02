package com.example.walkinggo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class WalkLogRequest {

    @NotNull(message = "시작 시간 필수")
    @Schema(description = "산책 시작 시간", example = "2025-05-20T10:00:00")
    private LocalDateTime startTime;

    @NotNull(message = "종료 시간 필수")
    @Schema(description = "산책 종료 시간", example = "2025-05-20T11:30:00")
    private LocalDateTime endTime;

    @Schema(description = "산책 시간 (초)", example = "5400")
    private Long durationSeconds;

    @Schema(description = "산책 거리 (미터)", example = "4500.5")
    private Double distanceMeters;

    @NotNull(message = "걸음 수는 필수입니다.")
    @Min(value = 0, message = "걸음 수는 0 이상이어야 합니다.")
    @Schema(description = "총 걸음 수", example = "6000")
    private Integer steps;

    @NotNull(message = "소모 칼로리는 필수입니다.")
    @Schema(description = "소모 칼로리 (프론트엔드에서 계산된 값)", example = "350.7")
    private Double caloriesBurned;

    @Schema(description = "경로 좌표 JSON 문자열")
    private String routeCoordinatesJson;
}