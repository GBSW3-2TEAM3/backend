package com.example.walkinggo.dto;

import com.example.walkinggo.entity.UserGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class RankedGroupResponse {
    @Schema(description = "그룹 ID")
    private final Long id;

    @Schema(description = "그룹 이름")
    private final String name;

    @Schema(description = "그룹 설명")
    private final String description;

    @Schema(description = "현재 멤버 수")
    private final int memberCount;

    @Schema(description = "공개 여부")
    private final boolean isPublic;

    @Schema(description = "팀 총 이동 거리 (km)")
    private final Double totalDistanceKm;

    @Schema(description = "팀 순위")
    private final int rank;

    public RankedGroupResponse(UserGroup group, Double totalDistanceMeters, int rank) {
        this.id = group.getId();
        this.name = group.getName();
        this.description = group.getDescription();
        this.memberCount = group.getMembers() != null ? group.getMembers().size() : 0;
        this.isPublic = group.getIsPublic();
        this.totalDistanceKm = (totalDistanceMeters != null) ? Math.round(totalDistanceMeters / 10.0) / 100.0 : 0.0;
        this.rank = rank;
    }
}