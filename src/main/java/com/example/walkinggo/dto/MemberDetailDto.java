package com.example.walkinggo.dto;

import com.example.walkinggo.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class MemberDetailDto {

    @Schema(description = "사용자 DB ID")
    private final Long userId;

    @Schema(description = "사용자 아이디 (username)")
    private final String username;

    @Schema(description = "해당 멤버의 총 이동 거리 (km)")
    private final Double totalDistanceKm;

    @Schema(description = "이 멤버가 그룹장인지 여부")
    private final boolean isOwner;

    public MemberDetailDto(User user, Double totalDistanceMeters, boolean isOwner) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.totalDistanceKm = (totalDistanceMeters != null) ? Math.round(totalDistanceMeters / 10.0) / 100.0 : 0.0;
        this.isOwner = isOwner;
    }
}