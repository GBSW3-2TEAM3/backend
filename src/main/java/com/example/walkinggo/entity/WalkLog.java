package com.example.walkinggo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
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
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    private Long durationSeconds;

    private Double distanceMeters;

    private Integer steps;

    private Double caloriesBurned;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String routeCoordinatesJson;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Schema(description = "사용자가 등록한 경로 이름 (공개 시 사용)")
    @Column(length = 100)
    private String routeName;

    @Schema(description = "사용자가 등록한 경로 설명 (공개 시 사용)")
    @Column(length = 500)
    private String routeDescription;

    @Schema(description = "경로 공개(추천) 여부")
    @Column(nullable = false)
    private boolean isPublicRoute = false;

}