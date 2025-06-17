package com.example.walkinggo.controller;

import com.example.walkinggo.dto.ErrorResponse;
import com.example.walkinggo.dto.WalkLogResponse;
import com.example.walkinggo.service.ShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    @Operation(summary = "산책 기록 공유 링크 생성", description = "특정 산책 기록에 대한 공유 가능한 고유 URL을 생성합니다.")
    @ApiResponse(responseCode = "200", description = "공유 링크 생성 또는 기존 링크 조회 성공")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/api/walk-logs/{walkLogId}/share")
    public ResponseEntity<?> createShareLink(
            @PathVariable Long walkLogId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return new ResponseEntity<>(new ErrorResponse("인증 정보가 없습니다."), HttpStatus.UNAUTHORIZED);
        }

        try {
            UUID shareId = shareService.createShareLink(walkLogId, userDetails.getUsername());

            URI shareUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/share/{shareId}")
                    .buildAndExpand(shareId)
                    .toUri();

            return ResponseEntity.ok(Map.of("shareUrl", shareUri.toString()));

        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.FORBIDDEN);
        }
    }

    @Operation(summary = "공유된 산책 경로 조회", description = "공유 링크 ID를 통해 누구나 산책 경로 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/share/{shareId}")
    public ResponseEntity<?> getSharedRoute(@PathVariable UUID shareId) {
        try {
            WalkLogResponse walkLogResponse = shareService.getSharedRoute(shareId);
            return ResponseEntity.ok(walkLogResponse);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }
}