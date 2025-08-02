package org.zb.ecommerce.domain.coupon.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * 사용자 쿠폰 Entity
 * 사용자가 보유한 쿠폰을 나타내는 Entity
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table("user_coupons")
public class UserCoupon {
    
    @Id
    private Long id;
    
    private Long userId;
    
    private Long couponId;
    
    private Long orderId;
    
    private CouponStatus status;
    
    private LocalDateTime issuedAt;
    
    private LocalDateTime usedAt;
    
    @Builder
    public UserCoupon(Long userId, Long couponId) {
        validateUserId(userId);
        validateCouponId(couponId);
        
        this.userId = userId;
        this.couponId = couponId;
        this.status = CouponStatus.AVAILABLE;
        this.issuedAt = LocalDateTime.now();
    }
    
    // 비즈니스 로직
    public void use(Long orderId) {
        validateOrderId(orderId);
        
        if (this.status != CouponStatus.AVAILABLE) {
            throw new IllegalStateException("사용 가능한 쿠폰이 아닙니다. 현재 상태: " + this.status.getDescription());
        }
        
        this.orderId = orderId;
        this.status = CouponStatus.USED;
        this.usedAt = LocalDateTime.now();
    }
    
    public void expire() {
        if (this.status != CouponStatus.AVAILABLE) {
            throw new IllegalStateException("사용 가능한 쿠폰만 만료 처리할 수 있습니다.");
        }
        this.status = CouponStatus.EXPIRED;
    }
    
    public boolean canUse() {
        return this.status == CouponStatus.AVAILABLE;
    }
    
    // 검증 로직
    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("사용자 ID가 올바르지 않습니다.");
        }
    }
    
    private void validateCouponId(Long couponId) {
        if (couponId == null || couponId <= 0) {
            throw new IllegalArgumentException("쿠폰 ID가 올바르지 않습니다.");
        }
    }
    
    private void validateOrderId(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("주문 ID가 올바르지 않습니다.");
        }
    }
}
