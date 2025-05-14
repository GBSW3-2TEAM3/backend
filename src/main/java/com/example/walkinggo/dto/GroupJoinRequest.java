package com.example.walkinggo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupJoinRequest {

    @NotBlank(message = "참가 코드를 입력해주세요.")
    @Schema(description = "비공개 그룹 참가 코드", example = "123456")
    private String participationCode;
}