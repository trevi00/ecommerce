package org.zb.ecommerce.global.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증을 위한 유틸리티 클래스
 * 주니어 개발자가 구현한 간단한 JWT 처리
 */
@Component
public class JwtTokenProvider {
    
    private final Key key;
    private final long tokenValidityInMilliseconds;
    
    public JwtTokenProvider(
            @Value("${jwt.secret:mySecretKey}") String secretKey,
            @Value("${jwt.token-validity-in-seconds:3600}") long tokenValidityInSeconds) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.tokenValidityInMilliseconds = tokenValidityInSeconds * 1000;
    }
    
    /**
     * JWT 토큰 생성 (userId와 email 사용)
     */
    public String createToken(Long userId, String email) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + tokenValidityInMilliseconds);
        
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("email", email)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * JWT 토큰 생성 (subject만 사용)
     */
    public String createToken(String subject) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + tokenValidityInMilliseconds);
        
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 토큰에서 사용자 ID 추출
     */
    public String getSubject(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    
    /**
     * 토큰에서 사용자 ID를 Long으로 추출
     */
    public Long getUserId(String token) {
        String subject = getSubject(token);
        return Long.parseLong(subject);
    }
    
    /**
     * 토큰 만료 시간 반환 (밀리초)
     */
    public Long getExpirationTime() {
        return tokenValidityInMilliseconds;
    }
    
    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}