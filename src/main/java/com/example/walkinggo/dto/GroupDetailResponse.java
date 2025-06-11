package com.example.walkinggo.dto;

import com.example.walkinggo.entity.User;
import com.example.walkinggo.entity.UserGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GroupDetailResponse {

    @Schema(description = "그룹 ID")
    private final Long groupId;

    @Schema(description = "그룹 이름")
    private final String groupName;

    @Schema(description = "그룹 설명")
    private final String description;

    @Schema(description = "현재 API를 요청한 사용자가 그룹장인지 여부")
    private final boolean isOwner;

    @Schema(description = "현재 API를 요청한 사용자의 ID")
    private final Long currentUserId;

    @Schema(description = "그룹 멤버 목록 (이동 거리순으로 정렬)")
    private final List<MemberDetailDto> members;

    public static GroupDetailResponse from(UserGroup group, User currentUser, List<MemberDetailDto> members) {
        boolean isOwner = group.getOwner().equals(currentUser);
        return GroupDetailResponse.builder()
                .groupId(group.getId())
                .groupName(group.getName())
                .description(group.getDescription())
                .isOwner(isOwner)
                .currentUserId(currentUser.getId())
                .members(members)
                .build();
    }
}