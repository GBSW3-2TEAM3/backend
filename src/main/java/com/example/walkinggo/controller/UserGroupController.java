package com.example.walkinggo.controller;

import com.example.walkinggo.dto.*;
import com.example.walkinggo.service.UserGroupService;
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

    @Operation(
            summary = "그룹 생성",
            description = """
                          새로운 걷기 그룹을 생성합니다.
                          - **공개 그룹 (`isPublic: true`)**: `name`, `description`(선택), `isPublic` 필드를 사용합니다.
                          - **비공개 그룹 (`isPublic: false`)**: `name`, `isPublic`, `participationCode` 필드를 사용합니다.
                          """
    )
    @ApiResponse(responseCode = "201", description = "그룹 생성 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = GroupResponse.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (예: 필수값 누락, 참여코드 중복/유효성 위반 등)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "이미 다른 그룹에 가입된 사용자가 그룹 생성 시도",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping
    public ResponseEntity<?> createGroup(
            @Valid @RequestBody GroupCreationRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return new ResponseEntity<>(new ErrorResponse("인증 정보가 없습니다."), HttpStatus.UNAUTHORIZED);
        }
        String username = userDetails.getUsername();
        logger.info("그룹 생성 요청: 사용자='{}', 그룹명='{}', 공개여부={}", username, request.getName(), request.getIsPublic());

        try {
            GroupResponse groupResponse = userGroupService.createGroup(request, username);
            return new ResponseEntity<>(groupResponse, HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            logger.warn("그룹 생성 실패 (사용자 없음): 사용자='{}', 메시지={}", username, e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            logger.warn("그룹 생성 실패 (잘못된 요청): 사용자='{}', 메시지={}", username, e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalStateException e) {
            logger.warn("그룹 생성 실패 (상태 오류): 사용자='{}', 메시지={}", username, e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.CONFLICT);
        }
        catch (Exception e) {
            logger.error("그룹 생성 중 오류 발생: 사용자='{}', 예외 유형={}, 메시지={}", username, e.getClass().getName(), e.getMessage(), e);
            return new ResponseEntity<>(new ErrorResponse("그룹 생성 중 오류가 발생했습니다."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "비공개 그룹 가입 (참여 코드 사용)", description = "참여 코드를 이용하여 비공개 그룹에 가입합니다.")
    @ApiResponse(responseCode = "200", description = "가입 성공", content = @Content(schema = @Schema(implementation = GroupResponse.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 해당 코드가 공개 그룹 코드, 요청 형식 오류)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "유효하지 않은 참가 코드", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "이미 가입된 그룹 또는 다른 그룹에 가입되어 있음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/join")
    public ResponseEntity<?> joinPrivateGroup(
            @Valid @RequestBody GroupJoinRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
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
        }
        catch (IllegalArgumentException e) {
            logger.warn("비공개 그룹 가입 실패 (잘못된 요청): User={}, Code={}, 메시지={}", username, request.getParticipationCode(), e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalStateException e) {
            logger.warn("비공개 그룹 가입 실패 (상태 오류): User={}, Code={}, 메시지={}", username, request.getParticipationCode(), e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.CONFLICT);
        } catch (Exception e) {
            logger.error("비공개 그룹 가입 처리 중 오류 발생: User={}, Code={}", username, request.getParticipationCode(), e);
            return new ResponseEntity<>(new ErrorResponse("그룹 가입 처리 중 오류 발생"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "공개 그룹 목록 조회")
    @ApiResponse(responseCode = "200", description = "목록 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SimpleGroupResponse.class)))
    @GetMapping("/public")
    public ResponseEntity<List<SimpleGroupResponse>> getPublicGroups() {
        List<SimpleGroupResponse> groups = userGroupService.getPublicGroups();
        return ResponseEntity.ok(groups);
    }

    @Operation(summary = "그룹 상세 정보 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GroupResponse.class)))
    @ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
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

    @Operation(summary = "그룹 상세 정보 조회 (멤버별 이동거리 포함)", description = "그룹의 기본 정보와 함께, 소속된 멤버 목록과 각 멤버의 총 이동거리를 반환합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{groupId}/details")
    public ResponseEntity<?> getGroupDetailsWithMemberDistances(
            @PathVariable Long groupId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return new ResponseEntity<>(new ErrorResponse("인증 정보가 없습니다."), HttpStatus.UNAUTHORIZED);
        }

        try {
            GroupDetailResponse groupDetails = userGroupService.getGroupDetailsWithMemberDistances(groupId, userDetails.getUsername());
            return ResponseEntity.ok(groupDetails);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "공개 그룹 가입")
    @ApiResponse(responseCode = "200", description = "가입 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 해당 그룹이 비공개, 이미 가입된 그룹)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "이미 다른 그룹에 가입되어 있음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/{groupId}/join-public")
    public ResponseEntity<?> joinPublicGroup(@PathVariable Long groupId, @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return new ResponseEntity<>(new ErrorResponse("인증 정보가 없습니다."), HttpStatus.UNAUTHORIZED);
        }
        String username = userDetails.getUsername();
        logger.info("공개 그룹 가입 요청: User={}, GroupID={}", username, groupId);
        try {
            userGroupService.joinPublicGroup(groupId, username);
            return ResponseEntity.ok().body("공개 그룹 가입 성공");
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse("그룹 가입 처리 중 오류 발생"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "그룹 멤버 목록 조회", description = "특정 그룹에 속한 모든 멤버의 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "멤버 목록 조회 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MemberResponse.class)))
    @ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{groupId}/members")
    public ResponseEntity<?> getGroupMembers(@PathVariable Long groupId) {
        try {
            List<MemberResponse> members = userGroupService.getGroupMembers(groupId);
            return ResponseEntity.ok(members);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "그룹 탈퇴")
    @ApiResponse(responseCode = "204", description = "탈퇴 성공")
    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<?> leaveGroup(@PathVariable Long groupId, @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return new ResponseEntity<>(new ErrorResponse("인증 정보가 없습니다."), HttpStatus.UNAUTHORIZED);
        }
        String username = userDetails.getUsername();
        try {
            userGroupService.leaveGroup(groupId, username);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse("그룹 탈퇴 처리 중 오류 발생"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "그룹 삭제 (소유자만 가능)")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @DeleteMapping("/{groupId}")
    public ResponseEntity<?> deleteGroup(@PathVariable Long groupId, @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return new ResponseEntity<>(new ErrorResponse("인증 정보가 없습니다."), HttpStatus.UNAUTHORIZED);
        }
        String username = userDetails.getUsername();
        try {
            userGroupService.deleteGroup(groupId, username);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse("그룹 삭제 처리 중 오류 발생"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "팀 랭킹 조회 (총 이동 거리 순)", description = "공개 그룹들의 멤버 총 이동 거리를 기준으로 랭킹을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "팀 랭킹 조회 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RankedGroupResponse.class)))
    @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/ranked-by-distance")
    public ResponseEntity<?> getRankedGroupsByDistance() {
        try {
            List<RankedGroupResponse> rankedGroups = userGroupService.getRankedPublicGroupsByDistance();
            return ResponseEntity.ok(rankedGroups);
        } catch (Exception e) {
            logger.error("팀 랭킹(거리순) 조회 중 오류 발생", e);
            return new ResponseEntity<>(new ErrorResponse("팀 랭킹 조회 중 오류가 발생했습니다."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}