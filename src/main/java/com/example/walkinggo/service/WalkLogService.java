package com.example.walkinggo.service;

import com.example.walkinggo.dto.MonthlyActivityResponse;
import com.example.walkinggo.dto.WalkLogRequest;
import com.example.walkinggo.dto.WalkLogResponse;
import com.example.walkinggo.entity.User;
import com.example.walkinggo.entity.WalkLog;
import com.example.walkinggo.repository.UserRepository;
import com.example.walkinggo.repository.WalkLogRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
                calculateCalories(user, durationSeconds, request.getDistanceMeters());


        WalkLog walkLog = WalkLog.builder()
                .user(user)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .durationSeconds(durationSeconds)
                .distanceMeters(request.getDistanceMeters())
                .caloriesBurned(caloriesBurned)
                .routeCoordinatesJson(request.getRouteCoordinatesJson())
                .build();

        WalkLog savedLog = walkLogRepository.save(walkLog);
        logger.info("산책 기록 저장 완료: 사용자='{}', 기록 ID={}", username, savedLog.getId());
        return WalkLogResponse.fromEntity(savedLog);
    }

    private double calculateCalories(User user, long durationSeconds, Double distanceMeters) {
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

        Set<LocalDate> activeDates = walkLogRepository.findActiveDatesInMonthByUser(user, startOfMonth, endOfMonth)
                .stream()
                .map(sqlDate -> sqlDate.toLocalDate())
                .collect(Collectors.toSet());

        return new MonthlyActivityResponse(activeDates);
    }
}