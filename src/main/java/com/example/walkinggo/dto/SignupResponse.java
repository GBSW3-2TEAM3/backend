package com.example.walkinggo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignupResponse {

    @Schema(description = "등록된 사용자 아이디")
    private final String username;

    @Schema(description = "회원가입 성공 여부")
    private final boolean signUp;
}