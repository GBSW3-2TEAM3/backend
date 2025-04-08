package com.example.walkinggo.service;

import com.example.walkinggo.entity.User;
import com.example.walkinggo.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Transactional
    public User registerUser(User user) {
        try {
            if (userRepository.findByUsername(user.getUsername()).isPresent()) {
                logger.warn("이미 존재하는 아이디입니다: {}", user.getUsername());
                throw new IllegalStateException("이미 존재하는 아이디입니다.");
            }
            if (!user.getPassword().equals(user.getPasswordConfirm())) {
                logger.warn("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
                throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User savedUser = userRepository.save(user);
            logger.info("사용자 등록 성공: {}", savedUser.getUsername());
            return savedUser;
        } catch (IllegalStateException | IllegalArgumentException e) {
            logger.error("사용자 등록 실패: {}", e.getMessage());
            throw e;
        }
    }

    public Optional<User> findByUsername(String username) {
        try {
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isPresent()) {
                logger.info("사용자 조회 성공: {}", username);
            } else {
                logger.info("사용자 조회 실패: {}", username);
            }
            return user;
        } catch (Exception e) {
            logger.error("사용자 조회 중 오류 발생: {}", e.getMessage());
            throw e;
        }
    }
}