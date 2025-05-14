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

    @Query("SELECT DISTINCT FUNCTION('DATE', wl.startTime) FROM WalkLog wl WHERE wl.user = :user AND wl.startTime >= :startOfMonth AND wl.startTime < :endOfMonth")
    List<java.sql.Date> findActiveDatesInMonthByUser(@Param("user") User user, @Param("startOfMonth") LocalDateTime startOfMonth, @Param("endOfMonth") LocalDateTime endOfMonth);
}