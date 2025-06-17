package com.example.walkinggo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoutePublishRequest {

    @NotBlank(message = "경로 이름은 필수입니다.")
    @Size(max = 100)
    @Schema(description = "등록할 경로의 이름", example = "대공원 한바퀴 코스")
    private String routeName;

    @Size(max = 500)
    @Schema(description = "등록할 경로의 설명", example = "초보자도 쉽게 걸을 수 있는 평지 코스입니다.")
    private String routeDescription;
}