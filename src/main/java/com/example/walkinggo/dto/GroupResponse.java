package com.example.walkinggo.dto;

import com.example.walkinggo.entity.UserGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GroupResponse {

    @Schema(description = "그룹 ID")
    private final Long id;
    @Schema(description = "그룹 이름")
    private final String name;
    @Schema(description = "그룹 설명")
    private final String description;
    @Schema(description = "그룹 생성자 아이디")
    private final String ownerUsername;
    @Schema(description = "공개 여부")
    private final Boolean isPublic;
    @Schema(description = "비공개 그룹 참가 코드")
    private final String participationCode;
    @Schema(description = "현재 멤버 수")
    private final int memberCount;
    @Schema(description = "생성 시간")
    private final LocalDateTime createdAt;

    public GroupResponse(UserGroup group) {
        this.id = group.getId();
        this.name = group.getName();
        this.description = group.getDescription();
        this.ownerUsername = group.getOwner().getUsername();
        this.isPublic = group.getIsPublic();
        this.participationCode = group.getParticipationCode();
        this.memberCount = group.getMembers().size();
        this.createdAt = group.getCreatedAt();
    }

    public static GroupResponse fromEntity(UserGroup group) {
        return new GroupResponse(group);
    }
}