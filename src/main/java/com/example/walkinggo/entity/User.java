package com.example.walkinggo.entity;

import jakarta.persistence.*;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "사용자 아이디")
    @Column(unique = true, nullable = false)
    private String username;

    @Schema(description = "사용자 비밀번호")
    @Column(nullable = false)
    private String password;

    @Transient
    private String passwordConfirm;

    @ManyToMany(mappedBy = "members", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<UserGroup> groups = new HashSet<>();

    @Schema(description = "사용자 체중 (kg)", example = "70.5")
    @Column
    private Double weightKg;

    @Schema(description = "사용자 목표 거리 (km)", example = "5.0")
    @Column
    private Double targetDistanceKm;
}