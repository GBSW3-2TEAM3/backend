package com.example.walkinggo.dto;

import com.example.walkinggo.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class MemberResponse {

    @Schema(description = "사용자 DB ID")
    private final Long id;

    @Schema(description = "사용자 아이디 (username)")
    private final String username;

    public MemberResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
    }
}