package com.example.walkinggo.dto;

import com.example.walkinggo.entity.WalkLog;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class RecommendedRouteResponse {
    @Schema(description = "원본 산책 기록 ID")
    private final Long id;
    @Schema(description = "경로 이름")
    private final String routeName;
    @Schema(description = "경로 설명")
    private final String routeDescription;
    @Schema(description = "거리 (km)")
    private final Double distanceKm;
    @Schema(description = "예상 소요 시간 (초)")
    private final Long durationSeconds;
    @Schema(description = "경로를 등록한 사용자 이름")
    private final String ownerUsername;

    public RecommendedRouteResponse(WalkLog walkLog) {
        this.id = walkLog.getId();
        this.routeName = walkLog.getRouteName();
        this.routeDescription = walkLog.getRouteDescription();
        this.distanceKm = (walkLog.getDistanceMeters() != null) ? Math.round(walkLog.getDistanceMeters() / 10.0) / 100.0 : 0.0;
        this.durationSeconds = walkLog.getDurationSeconds();
        this.ownerUsername = walkLog.getUser().getUsername();
    }
}