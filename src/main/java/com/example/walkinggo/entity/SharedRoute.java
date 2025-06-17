package com.example.walkinggo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shared_routes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID shareId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "walk_log_id", nullable = false, unique = true)
    private WalkLog walkLog;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}