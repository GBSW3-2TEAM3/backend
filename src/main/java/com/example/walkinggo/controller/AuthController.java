package com.example.walkinggo.controller;

import com.example.walkinggo.dto.AuthenticationResponse;
import com.example.walkinggo.dto.SignupResponse;
import com.example.walkinggo.entity.User;
import com.example.walkinggo.service.CustomUserDetailsService;
import com.example.walkinggo.service.UserService;
import com.example.walkinggo.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Operation(summary = "사용자 등록", description = "새로운 사용자를 등록합니다.")
    @ApiResponse(responseCode = "201", description = "회원가입 성공", content = @Content(schema = @Schema(implementation = SignupResponse.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(user);
            SignupResponse response = new SignupResponse(registeredUser.getUsername(), true);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalStateException | IllegalArgumentException e) {
            logger.warn("회원가입 실패: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "로그인", description = "사용자 인증 후 JWT 토큰 및 사용자 정보를 발급합니다.")
    @ApiResponse(responseCode = "200", description = "로그인 성공, JWT 토큰 및 사용자 정보 발급", content = @Content(schema = @Schema(implementation = AuthenticationResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
            UserDetails userDetails = userDetailsService.loadUserByUsername(authentication.getName());
            final String jwt = jwtUtil.generateToken(userDetails);
            return ResponseEntity.ok(new AuthenticationResponse(jwt, true, userDetails.getUsername()));
        } catch (Exception e) {
            logger.warn("인증 실패: {}", e.getMessage());
            return new ResponseEntity<>("인증 실패", HttpStatus.UNAUTHORIZED);
        }
    }
}