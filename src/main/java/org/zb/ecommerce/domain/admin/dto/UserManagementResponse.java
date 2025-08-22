package org.zb.ecommerce.domain.admin.dto;

import lombok.Builder;
import lombok.Getter;
import org.zb.ecommerce.domain.user.entity.User;
import org.zb.ecommerce.domain.user.entity.UserRole;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserManagementResponse {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private UserRole role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static UserManagementResponse from(User user) {
        return UserManagementResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}