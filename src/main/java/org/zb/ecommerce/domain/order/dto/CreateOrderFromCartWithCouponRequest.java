package org.zb.ecommerce.domain.order.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateOrderFromCartWithCouponRequest {
    
    private Long userCouponId; // 사용할 사용자 쿠폰 ID
    
    public CreateOrderFromCartWithCouponRequest(Long userCouponId) {
        this.userCouponId = userCouponId;
    }
}