package com.example.walkinggo.controller;

import com.example.walkinggo.dto.GroupCreationRequest;
import com.example.walkinggo.dto.GroupJoinRequest;
import com.example.walkinggo.dto.GroupResponse;
import com.example.walkinggo.dto.SimpleGroupResponse;
import com.example.walkinggo.service.UserGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Tag(name = "User Groups", description = "사용자 그룹 관련 API")
public class UserGroupController {

    private final UserGroupService userGroupService;

    @PostMapping
    @Operation(summary = "그룹 생성", description = "새로운 사용자 그룹을 생성합니다.")
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody GroupCreationRequest request) {
        String username = getCurrentUsername();
        GroupResponse createdGroup = userGroupService.createGroup(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGroup);
    }

    @GetMapping("/public")
    @Operation(summary = "공개 그룹 목록 조회", description = "참가 가능한 공개 그룹 목록을 조회합니다.")
    public ResponseEntity<List<SimpleGroupResponse>> getPublicGroups() {
        List<SimpleGroupResponse> publicGroups = userGroupService.getPublicGroups();
        return ResponseEntity.ok(publicGroups);
    }

    @PostMapping("/{groupId}/join")
    @Operation(summary = "공개 그룹 참가", description = "ID를 이용하여 공개 그룹에 참가합니다.")
    public ResponseEntity<Void> joinPublicGroup(@PathVariable Long groupId) {
        String username = getCurrentUsername();
        userGroupService.joinPublicGroup(groupId, username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/join-private")
    @Operation(summary = "비공개 그룹 참가", description = "참가 코드를 이용하여 비공개 그룹에 참가합니다.")
    public ResponseEntity<GroupResponse> joinPrivateGroup(@Valid @RequestBody GroupJoinRequest request) {
        String username = getCurrentUsername();
        GroupResponse joinedGroup = userGroupService.joinPrivateGroup(request.getParticipationCode(), username);
        return ResponseEntity.ok(joinedGroup);
    }

    @GetMapping("/{groupId}")
    @Operation(summary = "그룹 상세 정보 조회", description = "특정 그룹의 상세 정보를 조회합니다.")
    public ResponseEntity<GroupResponse> getGroupDetails(@PathVariable Long groupId) {
        GroupResponse groupDetails = userGroupService.getGroupDetails(groupId);
        return ResponseEntity.ok(groupDetails);
    }

    @DeleteMapping("/{groupId}/leave")
    @Operation(summary = "그룹 탈퇴", description = "현재 참가 중인 그룹에서 탈퇴합니다.")
    public ResponseEntity<Void> leaveGroup(@PathVariable Long groupId) {
        String username = getCurrentUsername();
        userGroupService.leaveGroup(groupId, username);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{groupId}")
    @Operation(summary = "그룹 삭제", description = "그룹 소유자가 그룹을 삭제합니다.")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long groupId) {
        String username = getCurrentUsername();
        userGroupService.deleteGroup(groupId, username);
        return ResponseEntity.noContent().build();
    }


    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("인증되지 않은 사용자입니다.");
        }
        return authentication.getName();
    }
}