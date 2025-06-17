package com.example.walkinggo.service;

import com.example.walkinggo.dto.MonthlyActivityResponse;
import com.example.walkinggo.dto.RecommendedRouteResponse;
import com.example.walkinggo.dto.RoutePublishRequest;
import com.example.walkinggo.dto.WalkLogRequest;
import com.example.walkinggo.dto.WalkLogResponse;
import com.example.walkinggo.entity.User;
import com.example.walkinggo.entity.UserGroup;
import com.example.walkinggo.entity.WalkLog;
import com.example.walkinggo.repository.UserRepository;
import com.example.walkinggo.repository.WalkLogRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class WalkLogService {

    private final WalkLogRepository walkLogRepository;
    private final UserRepository userRepository;
    private final Logger logger = LoggerFactory.getLogger(WalkLogService.class);

    private static final double WALKING_MET = 3.5;

    @Transactional
    public WalkLogResponse saveWalkLog(String username, WalkLogRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + username));

        long durationSeconds = request.getDurationSeconds() != null ?
                request.getDurationSeconds() :
                Duration.between(request.getStartTime(), request.getEndTime()).getSeconds();

        double caloriesBurned = request.getCaloriesBurned() != null ?
                request.getCaloriesBurned() :
                calculateCalories(user, durationSeconds);


        WalkLog walkLog = WalkLog.builder()
                .user(user)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .durationSeconds(durationSeconds)
                .distanceMeters(request.getDistanceMeters())
                .steps(request.getSteps())
                .caloriesBurned(caloriesBurned)
                .routeCoordinatesJson(request.getRouteCoordinatesJson())
                .build();

        WalkLog savedLog = walkLogRepository.save(walkLog);
        logger.info("산책 기록 저장 완료: 사용자='{}', 기록 ID={}", username, savedLog.getId());

        updateUserGroupsTotalDistance(user, savedLog.getDistanceMeters());

        return WalkLogResponse.fromEntity(savedLog);
    }

    private void updateUserGroupsTotalDistance(User user, Double distance) {
        if (distance == null || distance <= 0) {
            return;
        }
        Set<UserGroup> userGroups = user.getGroups();
        if (userGroups != null && !userGroups.isEmpty()) {
            for (UserGroup group : userGroups) {
                group.setTotalDistanceMeters(group.getTotalDistanceMeters() + distance);
            }
            logger.info("{} 사용자가 속한 {}개 그룹의 총 이동 거리 업데이트 완료.", user.getUsername(), userGroups.size());
        }
    }

    private double calculateCalories(User user, long durationSeconds) {
        double weightKg = 70.0;

        if (user.getWeightKg() != null ) {
            weightKg = user.getWeightKg();
        }

        double durationHours = durationSeconds / 3600.0;
        return WALKING_MET * weightKg * durationHours;
    }

    public List<WalkLogResponse> getWalkLogsByUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + username));
        return walkLogRepository.findByUserOrderByStartTimeDesc(user).stream()
                .map(WalkLogResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<WalkLogResponse> getWalkLogsByUserAndDate(String username, LocalDate date) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + username));
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        return walkLogRepository.findByUserAndDate(user, startOfDay, endOfDay).stream()
                .map(WalkLogResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public MonthlyActivityResponse getMonthlyActivity(String username, int year, int month) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + username));

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        List<java.sql.Date> activeSqlDates = walkLogRepository.findActiveDatesInMonthByUser(user.getId(), startOfMonth, endOfMonth);

        Set<LocalDate> activeDates = activeSqlDates
                .stream()
                .map(java.sql.Date::toLocalDate)
                .collect(Collectors.toSet());

        return new MonthlyActivityResponse(activeDates);
    }

    @Transactional
    public void publishRoute(Long walkLogId, RoutePublishRequest request, String username) {
        WalkLog walkLog = walkLogRepository.findById(walkLogId)
                .orElseThrow(() -> new EntityNotFoundException("해당 산책 기록을 찾을 수 없습니다."));

        if (!walkLog.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("자신의 산책 기록만 추천 경로로 등록할 수 있습니다.");
        }

        walkLog.setRouteName(request.getRouteName());
        walkLog.setRouteDescription(request.getRouteDescription());
        walkLog.setPublicRoute(true);

        walkLogRepository.save(walkLog);
    }

    @Transactional(readOnly = true)
    public List<RecommendedRouteResponse> getRecommendedRoutes() {
        return walkLogRepository.findByIsPublicRouteTrueOrderByCreatedAtDesc().stream()
                .map(RecommendedRouteResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WalkLogResponse getPublicRouteDetails(Long walkLogId) {
        WalkLog walkLog = walkLogRepository.findById(walkLogId)
                .orElseThrow(() -> new EntityNotFoundException("해당 산책 기록을 찾을 수 없습니다."));

        if (!walkLog.isPublicRoute()) {
            throw new AccessDeniedException("공개된 경로만 조회할 수 있습니다.");
        }

        return WalkLogResponse.fromEntity(walkLog);
    }
}