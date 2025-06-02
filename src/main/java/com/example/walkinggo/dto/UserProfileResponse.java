package com.example.walkinggo.dto;

import com.example.walkinggo.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class UserProfileResponse {

    @Schema(description = "사용자 ID")
    private final Long id;

    @Schema(description = "사용자 아이디 (username)")
    private final String username;

    @Schema(description = "사용자 체중 (kg)", example = "70.5")
    private final Double weightKg;

    @Schema(description = "사용자 목표 거리 (km)", example = "5.0")
    private final Double targetDistanceKm;

    public UserProfileResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.weightKg = user.getWeightKg();
        this.targetDistanceKm = user.getTargetDistanceKm();
    }

    public static UserProfileResponse fromEntity(User user) {
        return new UserProfileResponse(user);
    }
}