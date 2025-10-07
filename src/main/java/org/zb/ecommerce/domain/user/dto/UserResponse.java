package org.zb.ecommerce.domain.user.dto;

import org.zb.ecommerce.domain.user.entity.User;
import org.zb.ecommerce.domain.user.entity.UserRole;

import java.time.LocalDateTime;

/**
 * 사용자 정보 응답 DTO - record로 변환
 */
public record UserResponse(
    Long id,
    String email,
    String name,
    String phone,
    UserRole role,
    LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}