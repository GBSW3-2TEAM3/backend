package com.example.walkinggo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupCreationRequest {

    @NotBlank(message = "그룹 이름은 필수입니다.")
    @Size(min = 2, max = 100, message = "그룹 이름은 2자 이상 100자 이하로 입력해주세요.")
    @Schema(description = "생성할 그룹 이름", example = "우리 동네 걷기 모임")
    private String name;

    @Size(max = 500, message = "그룹 설명은 500자 이하로 입력해주세요.")
    @Schema(description = "그룹 설명 (공개 그룹인 경우 선택, 비공개 시 무시됨)", example = "매주 주말 함께 걸어요!")
    private String description;

    @NotNull(message = "공개 여부를 선택해주세요.")
    @Schema(description = "그룹 공개 여부 (true: 공개, false: 비공개)", example = "true")
    private Boolean isPublic;

    @Schema(description = "비공개 그룹 생성 시 사용할 팀 코드 (숫자만, 비공개 그룹인 경우 필수)", example = "123456")
    @Pattern(regexp = "^[0-9]+$", message = "팀 코드는 숫자만 입력 가능합니다.") // 숫자만
    @Size(min = 4, message = "팀 코드는 4자리 이상 10자리 이하이어야 합니다.")
    private String participationCode;
}