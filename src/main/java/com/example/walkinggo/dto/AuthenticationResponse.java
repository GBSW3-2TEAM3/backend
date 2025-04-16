package com.example.walkinggo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthenticationResponse {

    @Schema(description = "JWT 토큰")
    private final String jwt;

    @Schema(description = "로그인 성공 여부")
    private final boolean isLoggedIn;

    @Schema(description = "로그인한 사용자 아이디")
    private final String username;

    @Schema(description = "응답 메시지")
    private final String message;
}