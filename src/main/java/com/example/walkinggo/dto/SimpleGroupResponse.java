package com.example.walkinggo.dto;

import com.example.walkinggo.entity.UserGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class SimpleGroupResponse {
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

    public SimpleGroupResponse(UserGroup group) {
        this.id = group.getId();
        this.name = group.getName();
        this.description = group.getDescription();
        this.memberCount = group.getMembers().size();
        this.isPublic = group.getIsPublic();
    }

    public static SimpleGroupResponse fromEntity(UserGroup group) {
        return new SimpleGroupResponse(group);
    }
}