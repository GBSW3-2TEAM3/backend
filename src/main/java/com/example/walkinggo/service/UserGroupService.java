package com.example.walkinggo.service;

import com.example.walkinggo.dto.GroupCreationRequest;
import com.example.walkinggo.dto.GroupResponse;
import com.example.walkinggo.dto.SimpleGroupResponse;
import com.example.walkinggo.entity.User;
import com.example.walkinggo.entity.UserGroup;
import com.example.walkinggo.repository.UserGroupRepository;
import com.example.walkinggo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserGroupService {

    private final UserGroupRepository userGroupRepository;
    private final UserRepository userRepository;
    private final Logger logger = LoggerFactory.getLogger(UserGroupService.class);

    @Transactional
    public GroupResponse createGroup(GroupCreationRequest request, String ownerUsername) {
        logger.info(">>> [Service] createGroup 진입: User={}, GroupName={}", ownerUsername, request.getName());
        try {
            User owner = userRepository.findByUsername(ownerUsername)
                    .orElseThrow(() -> {
                        logger.warn("<<< [Service] UserRepository.findByUsername 결과: 사용자 없음 - {}", ownerUsername);
                        return new EntityNotFoundException("사용자를 찾을 수 없습니다: " + ownerUsername);
                    });

            UserGroup.UserGroupBuilder groupBuilder = UserGroup.builder()
                    .name(request.getName())
                    .owner(owner)
                    .isPublic(request.getIsPublic());

            if (request.getIsPublic()) {
                groupBuilder.description(request.getDescription());
                groupBuilder.participationCode(null);
            } else {
                if (request.getParticipationCode() == null || request.getParticipationCode().trim().isEmpty()) { // String에 맞게 수정
                    logger.warn("비공개 그룹 생성 시 참여 코드가 필요합니다. User: {}", ownerUsername);
                    throw new IllegalArgumentException("비공개 그룹을 생성하려면 참여 코드를 입력해야 합니다.");
                }
                // DTO에서 @Pattern으로 숫자만 입력되도록 검증했으므로, 여기서는 중복만 체크
                if (userGroupRepository.existsByParticipationCode(request.getParticipationCode())) {
                    logger.warn("이미 사용 중인 참여 코드입니다: {}", request.getParticipationCode());
                    throw new IllegalStateException("이미 사용 중인 참여 코드입니다. 다른 코드를 입력해주세요.");
                }
                groupBuilder.participationCode(request.getParticipationCode());
                groupBuilder.description(null);
            }

            UserGroup group = groupBuilder.build();
            group.addMember(owner);

            UserGroup savedGroup = userGroupRepository.save(group);
            logger.info("<<< [Service] UserGroupRepository.save 호출 완료 (저장된 Group ID: {})", savedGroup.getId());

            return GroupResponse.fromEntity(savedGroup);

        } catch (EntityNotFoundException enfe) {
            logger.warn("!!! [Service] createGroup 실행 중 사용자 못찾음 (EntityNotFoundException): {}", enfe.getMessage());
            throw enfe;
        } catch (IllegalArgumentException | IllegalStateException ex) {
            logger.warn("!!! [Service] createGroup 실행 중 오류: {}", ex.getMessage());
            throw ex;
        } catch (Exception e) {
            logger.error("!!! [Service] createGroup 실행 중 예외 발생: User={}, 예외 유형={}, 메시지={}",
                    ownerUsername, e.getClass().getName(), e.getMessage(), e);
            throw e;
        }
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
    public GroupResponse joinPrivateGroup(String participationCode, String username) { // String으로 변경
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + username));
        UserGroup group = userGroupRepository.findByParticipationCode(participationCode) // String으로 변경
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
        UserGroup savedGroup = userGroupRepository.save(group);
        logger.info("사용자 '{}'가 비공개 그룹 '{}'에 참가했습니다.", username, group.getName());
        return GroupResponse.fromEntity(savedGroup);
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
}