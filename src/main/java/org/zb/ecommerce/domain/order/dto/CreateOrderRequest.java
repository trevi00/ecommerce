package org.zb.ecommerce.domain.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * 주문 생성 요청 DTO - record로 변환
 */
public record CreateOrderRequest(
    @NotEmpty(message = "주문 항목은 필수입니다.")
    @Valid
    List<OrderItemRequest> items,
    
    Long couponId  // 선택적 쿠폰 ID
) {
    
    /**
     * 주문 항목 요청 DTO - nested record
     */
    public record OrderItemRequest(
        @NotNull(message = "상품 ID는 필수입니다.")
        Long productId,
        
        @NotNull(message = "수량은 필수입니다.")
        @Positive(message = "수량은 0보다 커야 합니다.")
        Integer quantity
    ) {}
}
