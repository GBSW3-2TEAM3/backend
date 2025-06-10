package com.example.walkinggo.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretString;

    private Key secretKey;

    private static final long JWT_TOKEN_VALIDITY = 30 * 60L;
    private final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(secretString);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserDetails userDetails) {
        try {
            Map<String, Object> claims = new HashMap<>();
            String token = createToken(claims, userDetails.getUsername());
            logger.info("JWT 토큰 생성 성공: {}", userDetails.getUsername());
            return token;
        } catch (Exception e) {
            logger.error("JWT 토큰 생성 실패: {}", e.getMessage());
            throw e;
        }
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                .signWith(secretKey, SignatureAlgorithm.HS512).compact();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (Exception e) {
            logger.error("클레임 추출 실패: {}", e.getMessage());
            throw e;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            logger.error("사용자 아이디 추출 실패: {}", e.getMessage());
            throw e;
        }
    }

    public Date extractExpiration(String token) {
        try {
            return extractClaim(token, Claims::getExpiration);
        } catch (Exception e) {
            logger.error("만료 시간 추출 실패: {}", e.getMessage());
            throw e;
        }
    }

    private Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            logger.error("토큰 만료 여부 확인 실패: {}", e.getMessage());
            throw e;
        }
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            logger.error("토큰 유효성 검증 실패: {}", e.getMessage());
            throw e;
        }
    }
}