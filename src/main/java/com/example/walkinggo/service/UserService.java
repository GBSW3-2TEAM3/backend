package com.example.walkinggo.service;

import com.example.walkinggo.dto.SimpleGroupResponse;
import com.example.walkinggo.dto.UserProfileResponse;
import com.example.walkinggo.dto.UserUpdateRequest;
import com.example.walkinggo.entity.User;
import com.example.walkinggo.entity.UserGroup;
import com.example.walkinggo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Transactional
    public User registerUser(User user) {
        try {
            if (userRepository.findByUsername(user.getUsername()).isPresent()) {
                logger.warn("이미 존재하는 아이디입니다: {}", user.getUsername());
                throw new IllegalStateException("이미 존재하는 아이디입니다.");
            }
            if (!user.getPassword().equals(user.getPasswordConfirm())) {
                logger.warn("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
                throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            if (user.getWeightKg() == null) {
            }
            if (user.getTargetDistanceKm() == null) {
            }
            User savedUser = userRepository.save(user);
            logger.info("사용자 등록 성공: {}", savedUser.getUsername());
            return savedUser;
        } catch (IllegalStateException | IllegalArgumentException e) {
            logger.error("사용자 등록 실패: {}", e.getMessage());
            throw e;
        }
    }

    public Optional<User> findByUsername(String username) {
        try {
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isPresent()) {
                logger.info("사용자 조회 성공: {}", username);
            } else {
                logger.info("사용자 조회 실패: {}", username);
            }
            return user;
        } catch (Exception e) {
            logger.error("사용자 조회 중 오류 발생: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("프로필 조회 시 사용자를 찾을 수 없음: {}", username);
                    return new EntityNotFoundException("사용자를 찾을 수 없습니다: " + username);
                });
        return UserProfileResponse.fromEntity(user);
    }

    @Transactional
    public UserProfileResponse updateUserProfile(String username, UserUpdateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + username));

        boolean updated = false;
        if (request.getWeightKg() != null) {
            user.setWeightKg(request.getWeightKg());
            updated = true;
        }
        if (request.getTargetDistanceKm() != null) {
            user.setTargetDistanceKm(request.getTargetDistanceKm());
            updated = true;
        }

        if (updated) {
            User updatedUser = userRepository.save(user);
            logger.info("사용자 정보 업데이트 성공: {}", username);
            return UserProfileResponse.fromEntity(updatedUser);
        } else {
            logger.info("사용자 정보 업데이트 요청이 있었으나 변경 사항 없음: {}", username);
            return UserProfileResponse.fromEntity(user);
        }
    }

    @Transactional
    public UserProfileResponse updateUserWeight(String username, Double weightKg) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + username));
        user.setWeightKg(weightKg);
        User updatedUser = userRepository.save(user);
        logger.info("사용자 체중 업데이트 성공: {}", username);
        return UserProfileResponse.fromEntity(updatedUser);
    }

    @Transactional
    public UserProfileResponse updateUserTargetDistance(String username, Double targetDistanceKm) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + username));
        user.setTargetDistanceKm(targetDistanceKm);
        User updatedUser = userRepository.save(user);
        logger.info("사용자 목표 거리 업데이트 성공: {}", username);
        return UserProfileResponse.fromEntity(updatedUser);
    }

    @Transactional(readOnly = true)
    public List<SimpleGroupResponse> getUserJoinedGroups(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + username));

        Set<UserGroup> joinedGroups = user.getGroups();
        if (joinedGroups == null || joinedGroups.isEmpty()) {
            logger.info("{} 사용자가 가입한 그룹이 없습니다.", username);
            return List.of();
        }
        logger.info("{} 사용자가 가입한 그룹 {}개 조회.", username, joinedGroups.size());
        return joinedGroups.stream()
                .map(SimpleGroupResponse::fromEntity)
                .collect(Collectors.toList());
    }
}