package com.example.walkinggo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

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
}