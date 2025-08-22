package org.zb.ecommerce.domain.user.dto;

/**
 * 로그인 응답 DTO - record로 변환
 */
public record LoginResponse(
    String token,
    String tokenType,
    Long expiresIn,
    UserResponse user
) {
    public LoginResponse(String token, Long expiresIn, UserResponse user) {
        this(token, "Bearer", expiresIn, user);
    }
    
    public static LoginResponse of(String token, Long expiresIn, UserResponse user) {
        return new LoginResponse(token, expiresIn, user);
    }
}