package com.example.walkinggo.service;

import com.example.walkinggo.dto.GroupCreationRequest;
import com.example.walkinggo.dto.GroupResponse;
import com.example.walkinggo.dto.RankedGroupResponse;
import com.example.walkinggo.dto.SimpleGroupResponse;
import com.example.walkinggo.entity.User;
import com.example.walkinggo.entity.UserGroup;
import com.example.walkinggo.repository.UserGroupRepository;
import com.example.walkinggo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserGroupService {

    private final UserGroupRepository userGroupRepository;
    private final UserRepository userRepository;
    private final Logger logger = LoggerFactory.getLogger(UserGroupService.class);

    @Transactional
    public GroupResponse createGroup(GroupCreationRequest request, String ownerUsername) {
        User owner = userRepository.findByUsername(ownerUsername)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + ownerUsername));

        if (!owner.getGroups().isEmpty()) {
            throw new IllegalStateException("이미 다른 그룹에 가입되어 있습니다. 한 명의 사용자는 하나의 그룹에만 가입할 수 있습니다.");
        }

        UserGroup.UserGroupBuilder groupBuilder = UserGroup.builder()
                .name(request.getName())
                .owner(owner)
                .isPublic(request.getIsPublic());

        if (request.getIsPublic()) {
            groupBuilder.description(request.getDescription());
            groupBuilder.participationCode(null);
        } else {
            if (request.getParticipationCode() == null || request.getParticipationCode().trim().isEmpty()) {
                throw new IllegalArgumentException("비공개 그룹을 생성하려면 참여 코드를 입력해야 합니다.");
            }
            if (userGroupRepository.existsByParticipationCode(request.getParticipationCode())) {
                throw new IllegalStateException("이미 사용 중인 참여 코드입니다. 다른 코드를 입력해주세요.");
            }
            groupBuilder.participationCode(request.getParticipationCode());
            groupBuilder.description(null);
        }

        UserGroup group = groupBuilder.build();
        group.addMember(owner);
        UserGroup savedGroup = userGroupRepository.save(group);
        return GroupResponse.fromEntity(savedGroup);
    }

    @Transactional
    public GroupResponse joinPrivateGroup(String participationCode, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + username));

        if (!user.getGroups().isEmpty()) {
            throw new IllegalStateException("이미 다른 그룹에 가입되어 있습니다. 한 명의 사용자는 하나의 그룹에만 가입할 수 있습니다.");
        }

        UserGroup group = userGroupRepository.findByParticipationCode(participationCode)
                .orElseThrow(() -> new EntityNotFoundException("유효하지 않은 참가 코드입니다: " + participationCode));

        if (group.getIsPublic()) {
            throw new IllegalArgumentException("비공개 그룹 참가는 코드를 통해서만 가능합니다.");
        }

        group.addMember(user);
        UserGroup savedGroup = userGroupRepository.save(group);
        return GroupResponse.fromEntity(savedGroup);
    }

    @Transactional
    public void joinPublicGroup(Long groupId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + username));

        if (!user.getGroups().isEmpty()) {
            throw new IllegalStateException("이미 다른 그룹에 가입되어 있습니다. 한 명의 사용자는 하나의 그룹에만 가입할 수 있습니다.");
        }

        UserGroup group = userGroupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("그룹을 찾을 수 없습니다: ID " + groupId));
        if (!group.getIsPublic()) {
            throw new IllegalArgumentException("공개 그룹만 이 방법으로 참가할 수 있습니다.");
        }

        group.addMember(user);
        userGroupRepository.save(group);
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
            if (group.getMembers().size() > 1) {
                throw new IllegalStateException("그룹장은 그룹을 탈퇴할 수 없습니다. 그룹을 삭제하거나 다른 멤버에게 그룹장을 위임해야 합니다.");
            }
            logger.info("마지막 멤버인 그룹장 {}가 탈퇴하여 그룹(ID:{})을 삭제합니다.", username, groupId);
            deleteGroup(groupId, username);
        } else {
            group.removeMember(user);
            userGroupRepository.save(group);
        }
    }

    @Transactional
    public void deleteGroup(Long groupId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + username));
        UserGroup group = userGroupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("그룹을 찾을 수 없습니다: ID " + groupId));
        if (!group.getOwner().equals(user)) {
            throw new AccessDeniedException("그룹 소유자만 그룹을 삭제할 수 있습니다.");
        }

        Set<User> members = group.getMembers();
        for(User member : members) {
            member.getGroups().remove(group);
        }

        userGroupRepository.delete(group);
    }

    @Transactional(readOnly = true)
    public List<RankedGroupResponse> getRankedPublicGroupsByDistance() {
        List<UserGroup> groups = userGroupRepository.findByIsPublicTrueOrderByTotalDistanceMetersDescNameAsc();
        List<RankedGroupResponse> rankedGroups = new ArrayList<>();
        int rank = 1;
        for (UserGroup group : groups) {
            rankedGroups.add(new RankedGroupResponse(group, group.getTotalDistanceMeters(), rank++));
        }
        logger.info("공개 그룹 랭킹 (거리순) 조회 완료. 총 {}개 그룹.", rankedGroups.size());
        return rankedGroups;
    }
}