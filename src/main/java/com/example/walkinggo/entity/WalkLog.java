package com.example.walkinggo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "walk_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalkLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime startTime; // 산책 시작 시간

    @Column(nullable = false)
    private LocalDateTime endTime; // 산책 종료 시간

    private Long durationSeconds; // 산책 시간 (초)

    private Double distanceMeters; // 산책 거리 (미터)

    private Integer steps; // 걸음 수

    private Double caloriesBurned; // 소모 칼로리

    @Lob
    @Column(columnDefinition = "TEXT")
    private String routeCoordinatesJson;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}