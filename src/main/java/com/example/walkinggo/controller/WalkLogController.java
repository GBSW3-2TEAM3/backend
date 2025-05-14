package com.example.walkinggo.controller;

import com.example.walkinggo.dto.ErrorResponse;
import com.example.walkinggo.dto.MonthlyActivityResponse;
import com.example.walkinggo.dto.WalkLogRequest;
import com.example.walkinggo.dto.WalkLogResponse;
import com.example.walkinggo.service.WalkLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/walk-logs")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class WalkLogController {

    private final WalkLogService walkLogService;
    private final Logger logger = LoggerFactory.getLogger(WalkLogController.class);

    @Operation(summary = "산책 기록 저장", description = "새로운 산책 기록을 저장합니다.")
    @ApiResponse(responseCode = "201", description = "기록 저장 성공", content = @Content(schema = @Schema(implementation = WalkLogResponse.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping
    public ResponseEntity<?> saveWalkLog(@Valid @RequestBody WalkLogRequest request,
                                         @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return new ResponseEntity<>(new ErrorResponse("인증 정보가 없습니다."), HttpStatus.UNAUTHORIZED);
        }
        try {
            WalkLogResponse response = walkLogService.saveWalkLog(userDetails.getUsername(), request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            logger.warn("산책 기록 저장 실패 (사용자 없음): {}", e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("산책 기록 저장 중 오류 발생: 사용자='{}'", userDetails.getUsername(), e);
            return new ResponseEntity<>(new ErrorResponse("산책 기록 저장 중 오류가 발생했습니다."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "내 산책 기록 전체 조회", description = "로그인한 사용자의 모든 산책 기록을 최신순으로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/my")
    public ResponseEntity<?> getMyWalkLogs(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return new ResponseEntity<>(new ErrorResponse("인증 정보가 없습니다."), HttpStatus.UNAUTHORIZED);
        }
        try {
            List<WalkLogResponse> logs = walkLogService.getWalkLogsByUser(userDetails.getUsername());
            return ResponseEntity.ok(logs);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "특정 날짜 산책 기록 조회", description = "로그인한 사용자의 특정 날짜 산책 기록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/date")
    public ResponseEntity<?> getWalkLogsByDate(
            @Parameter(description = "조회할 날짜 (YYYY-MM-DD)", required = true, example = "2025-05-12")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return new ResponseEntity<>(new ErrorResponse("인증 정보가 없습니다."), HttpStatus.UNAUTHORIZED);
        }
        try {
            List<WalkLogResponse> logs = walkLogService.getWalkLogsByUserAndDate(userDetails.getUsername(), date);
            return ResponseEntity.ok(logs);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "월별 산책 활동 조회 (캘린더용)", description = "로그인한 사용자의 특정 월에 산책 활동이 있었던 날짜 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/monthly-activity")
    public ResponseEntity<?> getMonthlyActivity(
            @Parameter(description = "조회할 연도", required = true, example = "2025") @RequestParam int year,
            @Parameter(description = "조회할 월", required = true, example = "5") @RequestParam int month,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return new ResponseEntity<>(new ErrorResponse("인증 정보가 없습니다."), HttpStatus.UNAUTHORIZED);
        }
        try {
            MonthlyActivityResponse response = walkLogService.getMonthlyActivity(userDetails.getUsername(), year, month);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }
}