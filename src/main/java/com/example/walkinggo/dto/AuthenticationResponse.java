package com.example.walkinggo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@AllArgsConstructor
public class AuthenticationResponse {
    @Schema(description = "JWT 토큰 (로그인 성공 시) 또는 사용자 아이디 (회원가입 성공 시)")
    private final String jwt;
    @Schema(description = "로그인 성공 여부")
    private final boolean isLoggedIn;
}