package com.example.walkinggo.dto;

import com.example.walkinggo.entity.WalkLog;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class WalkLogResponse {
    @Schema(description = "산책 기록 ID")
    private final Long id;
    @Schema(description = "사용자 아이디")
    private final String username;
    @Schema(description = "산책 시작 시간")
    private final LocalDateTime startTime;
    @Schema(description = "산책 종료 시간")
    private final LocalDateTime endTime;
    @Schema(description = "산책 시간 (초)")
    private final Long durationSeconds;
    @Schema(description = "산책 거리 (미터)")
    private final Double distanceMeters;
    @Schema(description = "소모 칼로리")
    private final Double caloriesBurned;
    @Schema(description = "경로 좌표 JSON")
    private final String routeCoordinatesJson;
    @Schema(description = "기록 생성 시간")
    private final LocalDateTime createdAt;

    public WalkLogResponse(WalkLog walkLog) {
        this.id = walkLog.getId();
        this.username = walkLog.getUser().getUsername();
        this.startTime = walkLog.getStartTime();
        this.endTime = walkLog.getEndTime();
        this.durationSeconds = walkLog.getDurationSeconds();
        this.distanceMeters = walkLog.getDistanceMeters();
        this.caloriesBurned = walkLog.getCaloriesBurned();
        this.routeCoordinatesJson = walkLog.getRouteCoordinatesJson();
        this.createdAt = walkLog.getCreatedAt();
    }

    public static WalkLogResponse fromEntity(WalkLog walkLog) {
        return new WalkLogResponse(walkLog);
    }
}