package org.zb.ecommerce.domain.admin.dto;

import jakarta.validation.constraints.NotNull;
import org.zb.ecommerce.domain.user.entity.UserRole;

/**
 * 사용자 역할 업데이트 요청 DTO - record로 변환
 */
public record UpdateUserRoleRequest(
    @NotNull(message = "역할은 필수입니다.")
    UserRole role
) {}