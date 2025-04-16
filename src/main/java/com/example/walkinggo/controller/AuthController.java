package com.example.walkinggo.controller;

import com.example.walkinggo.dto.AuthenticationResponse;
import com.example.walkinggo.dto.ErrorResponse;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 아이디 중복, 비밀번호 불일치)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(user);
            SignupResponse response = new SignupResponse(registeredUser.getUsername(), true, "회원가입이 성공적으로 완료되었습니다.");
            logger.info("회원가입 성공: {}", registeredUser.getUsername());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalStateException | IllegalArgumentException e) {
            logger.warn("회원가입 실패: {}", e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("회원가입 중 예상치 못한 오류 발생", e);
            return new ResponseEntity<>(new ErrorResponse("회원가입 처리 중 오류가 발생했습니다."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "로그인", description = "사용자 인증 후 JWT 토큰 및 사용자 정보를 발급합니다.")
    @ApiResponse(responseCode = "200", description = "로그인 성공, JWT 토큰 및 사용자 정보 발급", content = @Content(schema = @Schema(implementation = AuthenticationResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패 (아이디 또는 비밀번호 오류)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
            UserDetails userDetails = userDetailsService.loadUserByUsername(authentication.getName());
            final String jwt = jwtUtil.generateToken(userDetails);

            AuthenticationResponse response = new AuthenticationResponse(jwt, true, userDetails.getUsername(), "로그인 성공");
            logger.info("로그인 성공: {}", userDetails.getUsername());
            return ResponseEntity.ok(response);

        } catch (UsernameNotFoundException | BadCredentialsException e) {
            logger.warn("인증 실패: {}", e.getMessage());
            return new ResponseEntity<>(new ErrorResponse("인증 실패: 아이디 또는 비밀번호를 확인하세요."), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            logger.error("로그인 중 예상치 못한 오류 발생", e);
            return new ResponseEntity<>(new ErrorResponse("로그인 처리 중 오류가 발생했습니다."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}