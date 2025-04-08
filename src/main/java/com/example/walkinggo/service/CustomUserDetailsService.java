package com.example.walkinggo.service;

import com.example.walkinggo.entity.User;
import com.example.walkinggo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
            logger.info("사용자 조회 성공: {}", username);
            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPassword(),
                    Collections.singletonList(new SimpleGrantedAuthority("USER"))
            );
        } catch (UsernameNotFoundException e) {
            logger.error("사용자를 찾을 수 없습니다: {}", username);
            throw e;
        } catch (Exception e) {
            logger.error("사용자 조회 중 오류 발생: {}", e.getMessage());
            throw e;
        }
    }
}