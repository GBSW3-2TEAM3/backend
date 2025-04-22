package com.example.walkinggo.controller;

import com.example.walkinggo.dto.ErrorResponse;
import com.example.walkinggo.dto.GroupCreationRequest;
import com.example.walkinggo.dto.GroupJoinRequest;
import com.example.walkinggo.dto.GroupResponse;
import com.example.walkinggo.dto.SimpleGroupResponse;
import com.example.walkinggo.repository.UserGroupService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserGroupController {

    private final UserGroupService userGroupService;
    private final Logger logger = LoggerFactory.getLogger(UserGroupController.class);

    @Operation(summary = "그룹 생성", description = "새로운 걷기 그룹을 생성합니다.")
    @ApiResponse(responseCode = "201", description = "그룹 생성 성공", content = @Content(schema = @Schema(implementation = GroupResponse.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping
    public ResponseEntity<?> createGroup(@Valid @RequestBody GroupCreationRequest request, @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return new ResponseEntity<>(new ErrorResponse("인증 정보가 없습니다."), HttpStatus.UNAUTHORIZED);
        }
        String username = userDetails.getUsername();
        logger.info("그룹 생성 요청: 사용자='{}', 그룹명='{}'", username, request.getName());

        try {
            GroupResponse groupResponse = userGroupService.createGroup(request, username);
            logger.info("그룹 생성 성공: 그룹 ID={}, 그룹명='{}'", groupResponse.getId(), groupResponse.getName());
            return new ResponseEntity<>(groupResponse, HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            logger.warn("그룹 생성 실패 (사용자 없음): 사용자='{}', 메시지={}", username, e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("그룹 생성 중 예상치 못한 오류 발생: 사용자='{}'", username, e);
            return new ResponseEntity<>(new ErrorResponse("그룹 생성 중 오류가 발생했습니다."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "공개 그룹 목록 조회", description = "공개된 모든 걷기 그룹 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "목록 조회 성공")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/public")
    public ResponseEntity<List<SimpleGroupResponse>> getPublicGroups() {
        List<SimpleGroupResponse> groups = userGroupService.getPublicGroups();
        return ResponseEntity.ok(groups);
    }

    @Operation(summary = "그룹 상세 정보 조회", description = "특정 그룹의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = GroupResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{groupId}")
    public ResponseEntity<?> getGroupDetails(@PathVariable Long groupId) {
        try {
            GroupResponse group = userGroupService.getGroupDetails(groupId);
            return ResponseEntity.ok(group);
        } catch (EntityNotFoundException e) {
            logger.warn("그룹 상세 정보 조회 실패: Group ID={}, 메시지={}", groupId, e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("그룹 상세 정보 조회 중 오류 발생: Group ID={}", groupId, e);
            return new ResponseEntity<>(new ErrorResponse("그룹 정보 조회 중 오류 발생"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "공개 그룹 가입", description = "ID를 이용하여 공개 그룹에 가입합니다.")
    @ApiResponse(responseCode = "200", description = "가입 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (공개 그룹 아님 등)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "그룹 또는 사용자를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "이미 가입된 그룹", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/{groupId}/join")
    public ResponseEntity<?> joinPublicGroup(@PathVariable Long groupId, @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return new ResponseEntity<>(new ErrorResponse("인증 정보가 없습니다."), HttpStatus.UNAUTHORIZED);
        }
        String username = userDetails.getUsername();
        logger.info("공개 그룹 가입 요청: User={}, GroupID={}", username, groupId);
        try {
            userGroupService.joinPublicGroup(groupId, username);
            return ResponseEntity.ok().body("그룹 가입 성공");
        } catch (EntityNotFoundException e) {
            logger.warn("공개 그룹 가입 실패 (찾을 수 없음): User={}, GroupID={}, 메시지={}", username, groupId, e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            logger.warn("공개 그룹 가입 실패 (잘못된 요청): User={}, GroupID={}, 메시지={}", username, groupId, e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalStateException e) {
            logger.warn("공개 그룹 가입 실패 (이미 가입됨): User={}, GroupID={}, 메시지={}", username, groupId, e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.CONFLICT);
        } catch (Exception e) {
            logger.error("공개 그룹 가입 처리 중 오류 발생: User={}, GroupID={}", username, groupId, e);
            return new ResponseEntity<>(new ErrorResponse("그룹 가입 처리 중 오류 발생"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "비공개 그룹 가입", description = "참여 코드를 이용하여 비공개 그룹에 가입합니다.")
    @ApiResponse(responseCode = "200", description = "가입 성공", content = @Content(schema = @Schema(implementation = GroupResponse.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (비공개 그룹 아님 등)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "잘못된 참여 코드 또는 사용자를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "이미 가입된 그룹", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/join")
    public ResponseEntity<?> joinPrivateGroup(@Valid @RequestBody GroupJoinRequest request, @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return new ResponseEntity<>(new ErrorResponse("인증 정보가 없습니다."), HttpStatus.UNAUTHORIZED);
        }
        String username = userDetails.getUsername();
        logger.info("비공개 그룹 가입 요청: User={}, Code={}", username, request.getParticipationCode());
        try {
            GroupResponse groupResponse = userGroupService.joinPrivateGroup(request.getParticipationCode(), username);
            return ResponseEntity.ok(groupResponse);
        } catch (EntityNotFoundException e) {
            logger.warn("비공개 그룹 가입 실패 (찾을 수 없음): User={}, Code={}, 메시지={}", username, request.getParticipationCode(), e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            logger.warn("비공개 그룹 가입 실패 (잘못된 요청): User={}, Code={}, 메시지={}", username, request.getParticipationCode(), e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalStateException e) {
            logger.warn("비공개 그룹 가입 실패 (이미 가입됨): User={}, Code={}, 메시지={}", username, request.getParticipationCode(), e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.CONFLICT);
        } catch (Exception e) {
            logger.error("비공개 그룹 가입 처리 중 오류 발생: User={}, Code={}", username, request.getParticipationCode(), e);
            return new ResponseEntity<>(new ErrorResponse("그룹 가입 처리 중 오류 발생"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "그룹 탈퇴", description = "현재 로그인된 사용자가 특정 그룹에서 탈퇴합니다.")
    @ApiResponse(responseCode = "204", description = "탈퇴 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (그룹장이 탈퇴 시도 등)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "그룹 또는 사용자를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<?> leaveGroup(@PathVariable Long groupId, @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return new ResponseEntity<>(new ErrorResponse("인증 정보가 없습니다."), HttpStatus.UNAUTHORIZED);
        }
        String username = userDetails.getUsername();
        logger.info("그룹 탈퇴 요청: User={}, GroupID={}", username, groupId);
        try {
            userGroupService.leaveGroup(groupId, username);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            logger.warn("그룹 탈퇴 실패 (찾을 수 없음): User={}, GroupID={}, 메시지={}", username, groupId, e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            logger.warn("그룹 탈퇴 실패 (잘못된 요청): User={}, GroupID={}, 메시지={}", username, groupId, e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("그룹 탈퇴 처리 중 오류 발생: User={}, GroupID={}", username, groupId, e);
            return new ResponseEntity<>(new ErrorResponse("그룹 탈퇴 처리 중 오류 발생"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "그룹 삭제", description = "그룹 소유자가 그룹을 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "삭제 권한 없음 (그룹 소유주 아님)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "그룹 또는 사용자를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/{groupId}")
    public ResponseEntity<?> deleteGroup(@PathVariable Long groupId, @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return new ResponseEntity<>(new ErrorResponse("인증 정보가 없습니다."), HttpStatus.UNAUTHORIZED);
        }
        String username = userDetails.getUsername();
        logger.info("그룹 삭제 요청: User={}, GroupID={}", username, groupId);
        try {
            userGroupService.deleteGroup(groupId, username);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            logger.warn("그룹 삭제 실패 (찾을 수 없음): User={}, GroupID={}, 메시지={}", username, groupId, e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (AccessDeniedException e) {
            logger.warn("그룹 삭제 실패 (권한 없음): User={}, GroupID={}, 메시지={}", username, groupId, e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("그룹 삭제 처리 중 오류 발생: User={}, GroupID={}", username, groupId, e);
            return new ResponseEntity<>(new ErrorResponse("그룹 삭제 처리 중 오류 발생"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}