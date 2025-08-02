package org.zb.ecommerce.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 장바구니에서 주문 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderFromCartRequest {
    
    private Long couponId;  // 선택적 쿠폰 ID
}
