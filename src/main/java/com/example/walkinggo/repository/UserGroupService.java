package com.example.walkinggo.repository;

import com.example.walkinggo.dto.GroupCreationRequest;
import com.example.walkinggo.dto.GroupResponse;
import com.example.walkinggo.dto.SimpleGroupResponse;
import com.example.walkinggo.entity.User;
import com.example.walkinggo.entity.UserGroup;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserGroupService {

    private final UserGroupRepository userGroupRepository;
    private final UserRepository userRepository;
    private final Logger logger = LoggerFactory.getLogger(UserGroupService.class);
    private static final String CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    @Transactional
    public GroupResponse createGroup(GroupCreationRequest request, String ownerUsername) {
        User owner = userRepository.findByUsername(ownerUsername)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + ownerUsername));

        UserGroup group = UserGroup.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(owner)
                .isPublic(request.getIsPublic())
                .build();

        if (!request.getIsPublic()) {
            group.setParticipationCode(generateUniqueParticipationCode());
        }

        group.addMember(owner);
        UserGroup savedGroup = userGroupRepository.save(group);
        logger.info("그룹 생성 완료: {} (Owner: {})", savedGroup.getName(), ownerUsername);

        return GroupResponse.fromEntity(savedGroup);
    }

    @Transactional
    public void joinPublicGroup(Long groupId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + username));

        UserGroup group = userGroupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("그룹을 찾을 수 없습니다: ID " + groupId));

        if (!group.getIsPublic()) {
            logger.warn("공개 그룹이 아닙니다. 비공개 그룹 참가를 시도했습니다. Group ID: {}", groupId);
            throw new IllegalArgumentException("공개 그룹만 이 방법으로 참가할 수 있습니다.");
        }

        if (group.getMembers().contains(user)) {
            logger.warn("이미 그룹 멤버입니다. User: {}, Group ID: {}", username, groupId);
            throw new IllegalStateException("이미 해당 그룹의 멤버입니다.");
        }

        group.addMember(user);
        userGroupRepository.save(group);
        logger.info("사용자 '{}'가 공개 그룹 '{}'에 참가했습니다.", username, group.getName());
    }

    @Transactional
    public GroupResponse joinPrivateGroup(String participationCode, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + username));

        UserGroup group = userGroupRepository.findByParticipationCode(participationCode)
                .orElseThrow(() -> new EntityNotFoundException("유효하지 않은 참가 코드입니다: " + participationCode));

        if (group.getIsPublic()) {
            logger.warn("비공개 그룹이 아닙니다. 공개 그룹 참가를 시도했습니다. Code: {}", participationCode);
            throw new IllegalArgumentException("비공개 그룹 참가는 코드를 통해서만 가능합니다.");
        }

        if (group.getMembers().contains(user)) {
            logger.warn("이미 그룹 멤버입니다. User: {}, Group Code: {}", username, participationCode);
            throw new IllegalStateException("이미 해당 그룹의 멤버입니다.");
        }

        group.addMember(user);
        userGroupRepository.save(group);
        logger.info("사용자 '{}'가 비공개 그룹 '{}'에 참가했습니다.", username, group.getName());
        return GroupResponse.fromEntity(group);
    }

    public List<SimpleGroupResponse> getPublicGroups() {
        return userGroupRepository.findByIsPublicTrueOrderByNameAsc().stream()
                .map(SimpleGroupResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public GroupResponse getGroupDetails(Long groupId) {
        UserGroup group = userGroupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("그룹을 찾을 수 없습니다: ID " + groupId));
        return GroupResponse.fromEntity(group);
    }

    @Transactional
    public void leaveGroup(Long groupId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + username));
        UserGroup group = userGroupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("그룹을 찾을 수 없습니다: ID " + groupId));

        if (!group.getMembers().contains(user)) {
            throw new IllegalStateException("해당 그룹의 멤버가 아닙니다.");
        }

        if (group.getOwner().equals(user)) {
            throw new IllegalStateException("그룹장은 그룹을 탈퇴할 수 없습니다. 그룹을 삭제하거나 관리자를 위임하세요.");
        }

        group.removeMember(user);
        userGroupRepository.save(group);
        logger.info("사용자 '{}'가 그룹 '{}'에서 탈퇴했습니다.", username, group.getName());
    }

    @Transactional
    public void deleteGroup(Long groupId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + username));
        UserGroup group = userGroupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("그룹을 찾을 수 없습니다: ID " + groupId));

        if (!group.getOwner().equals(user)) {
            logger.warn("그룹 삭제 권한 없음 - 요청자: {}, 그룹 소유자: {}", username, group.getOwner().getUsername());
            throw new AccessDeniedException("그룹 소유자만 그룹을 삭제할 수 있습니다.");
        }
        userGroupRepository.delete(group);
        logger.info("그룹 '{}'(ID: {})가 사용자 '{}'에 의해 삭제되었습니다.", group.getName(), groupId, username);
    }


    private String generateUniqueParticipationCode() {
        String code;
        do {
            code = generateRandomCode();
        } while (userGroupRepository.existsByParticipationCode(code));
        return code;
    }

    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_CHARACTERS.charAt(random.nextInt(CODE_CHARACTERS.length())));
        }
        return sb.toString();
    }
}