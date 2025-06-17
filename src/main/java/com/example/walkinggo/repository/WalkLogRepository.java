package com.example.walkinggo.repository;

import com.example.walkinggo.entity.User;
import com.example.walkinggo.entity.WalkLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface WalkLogRepository extends JpaRepository<WalkLog, Long> {

    List<WalkLog> findByUserOrderByStartTimeDesc(User user);

    @Query("SELECT wl FROM WalkLog wl WHERE wl.user = :user AND wl.startTime >= :startOfDay AND wl.startTime < :endOfDay ORDER BY wl.startTime DESC")
    List<WalkLog> findByUserAndDate(@Param("user") User user, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    @Query(value = "SELECT DISTINCT DATE(start_time) FROM walk_logs WHERE user_id = :userId AND start_time >= :startOfMonth AND start_time < :endOfMonth", nativeQuery = true)
    List<java.sql.Date> findActiveDatesInMonthByUser(@Param("userId") Long userId, @Param("startOfMonth") LocalDateTime startOfMonth, @Param("endOfMonth") LocalDateTime endOfMonth);

    @Query("SELECT wl.user.id, SUM(wl.distanceMeters) FROM WalkLog wl WHERE wl.user IN :users GROUP BY wl.user.id")
    List<Object[]> findTotalDistanceByUsers(@Param("users") List<User> users);

    List<WalkLog> findByIsPublicRouteTrueOrderByCreatedAtDesc();
}