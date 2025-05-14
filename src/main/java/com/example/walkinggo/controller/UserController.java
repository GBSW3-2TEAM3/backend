package com.example.walkinggo.controller;

import com.example.walkinggo.dto.ErrorResponse;
import com.example.walkinggo.dto.UserProfileResponse;
import com.example.walkinggo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Operation(summary = "내 프로필 정보 조회", description = "현재 로그인된 사용자의 프로필 정보를 반환합니다.")
    @ApiResponse(responseCode = "200", description = "프로필 정보 조회 성공",
            content = @Content(schema = @Schema(implementation = UserProfileResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            logger.warn("내 프로필 조회 시 인증된 사용자 정보를 찾을 수 없습니다.");
            return new ResponseEntity<>(new ErrorResponse("인증 정보가 없습니다. 다시 로그인해주세요."), HttpStatus.UNAUTHORIZED);
        }

        String username = userDetails.getUsername();
        logger.info("내 프로필 정보 조회 요청: 사용자='{}'", username);

        try {
            UserProfileResponse userProfile = userService.getUserProfile(username);
            return ResponseEntity.ok(userProfile);
        } catch (EntityNotFoundException e) {
            logger.warn("내 프로필 조회 실패 - 서비스에서 사용자를 찾을 수 없음: {}", username);
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("내 프로필 정보 조회 중 서버 오류 발생: 사용자='{}'", username, e);
            return new ResponseEntity<>(new ErrorResponse("프로필 정보 조회 중 오류가 발생했습니다."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}