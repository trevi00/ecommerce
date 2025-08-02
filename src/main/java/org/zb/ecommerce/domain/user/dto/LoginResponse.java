package org.zb.ecommerce.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    
    private String token;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserResponse user;
    
    public static LoginResponse of(String token, Long expiresIn, UserResponse user) {
        return LoginResponse.builder()
                .token(token)
                .expiresIn(expiresIn)
                .user(user)
                .build();
    }
}
