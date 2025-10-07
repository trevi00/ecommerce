package org.zb.ecommerce.global.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.zb.ecommerce.global.exception.BusinessException;
import org.zb.ecommerce.global.exception.ErrorCode;

/**
 * JWT 토큰 기반 인증 인터셉터
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    
    private final JwtTokenProvider jwtTokenProvider;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        
        // LoginRequired 어노테이션 확인
        LoginRequired loginRequired = handlerMethod.getMethodAnnotation(LoginRequired.class);
        if (loginRequired == null) {
            loginRequired = handlerMethod.getBeanType().getAnnotation(LoginRequired.class);
        }
        
        if (loginRequired == null) {
            return true;
        }
        
        // 토큰 추출
        String token = extractToken(request);
        if (token == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        
        // 토큰 검증
        if (!jwtTokenProvider.validateToken(token)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        
        // 사용자 ID 추출 및 저장
        Long userId = jwtTokenProvider.getUserId(token);
        request.setAttribute("userId", userId);
        
        return true;
    }
    
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }
}
