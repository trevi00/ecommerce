package org.zb.ecommerce.domain.coupon.dto;

import lombok.Builder;
import lombok.Getter;
import org.zb.ecommerce.domain.admin.dto.CouponResponse;
import org.zb.ecommerce.domain.coupon.entity.CouponStatus;
import org.zb.ecommerce.domain.coupon.entity.UserCoupon;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserCouponResponse {
    private Long id;
    private Long userId;
    private Long couponId;
    private Long orderId;
    private CouponStatus status;
    private LocalDateTime issuedAt;
    private LocalDateTime usedAt;
    private CouponResponse coupon;
    
    public static UserCouponResponse from(UserCoupon userCoupon) {
        return UserCouponResponse.builder()
                .id(userCoupon.getId())
                .userId(userCoupon.getUserId())
                .couponId(userCoupon.getCouponId())
                .orderId(userCoupon.getOrderId())
                .status(userCoupon.getStatus())
                .issuedAt(userCoupon.getIssuedAt())
                .usedAt(userCoupon.getUsedAt())
                .build();
    }
    
    public static UserCouponResponse fromWithCoupon(UserCoupon userCoupon, CouponResponse coupon) {
        return UserCouponResponse.builder()
                .id(userCoupon.getId())
                .userId(userCoupon.getUserId())
                .couponId(userCoupon.getCouponId())
                .orderId(userCoupon.getOrderId())
                .status(userCoupon.getStatus())
                .issuedAt(userCoupon.getIssuedAt())
                .usedAt(userCoupon.getUsedAt())
                .coupon(coupon)
                .build();
    }
}