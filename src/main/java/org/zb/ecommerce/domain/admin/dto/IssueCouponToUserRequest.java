package org.zb.ecommerce.domain.admin.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 사용자에게 쿠폰 발급 요청 DTO - record로 변환
 */
public record IssueCouponToUserRequest(
    @NotNull(message = "사용자 ID는 필수입니다.")
    Long userId,
    
    @NotNull(message = "쿠폰 ID는 필수입니다.")
    Long couponId
) {}