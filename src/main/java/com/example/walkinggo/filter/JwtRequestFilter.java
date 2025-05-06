package com.example.walkinggo.filter;

import com.example.walkinggo.service.CustomUserDetailsService;
import com.example.walkinggo.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwt = authorizationHeader.substring(7);
                username = jwtUtil.extractUsername(jwt);
                logger.debug("JWT 토큰 발견: username='{}'", username);
            } else {
                logger.debug("Authorization 헤더 없거나 'Bearer '로 시작하지 않음");
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                logger.debug("SecurityContext 비어있음, username='{}' 사용자 정보 로드 시도", username);
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtUtil.validateToken(jwt, userDetails)) {
                    logger.debug("JWT 토큰 유효함, Authentication 객체 생성 및 SecurityContext 설정");
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    logger.debug("SecurityContext 설정 완료: {}", SecurityContextHolder.getContext().getAuthentication());
                } else {
                    logger.warn("JWT 토큰 유효하지 않음: username='{}'", username);
                }
            } else if (username == null) {
                logger.debug("JWT 토큰에서 username을 추출하지 못함 (토큰 없거나 파싱 오류)");
            } else {
                logger.debug("SecurityContext에 이미 인증 정보 존재함: {}", SecurityContextHolder.getContext().getAuthentication());
            }

            logger.debug("chain.doFilter 호출 전");
            chain.doFilter(request, response);
            logger.debug("chain.doFilter 호출 후");

        } catch (Exception e) {
            logger.error("JWT 인증 처리 중 예외 발생: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}