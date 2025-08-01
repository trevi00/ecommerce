package org.zb.ecommerce.domain.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 주문 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {
    
    @NotEmpty(message = "주문 항목은 필수입니다.")
    @Valid
    private List<OrderItemRequest> items;
    
    private Long couponId;  // 선택적 쿠폰 ID
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemRequest {
        
        @NotNull(message = "상품 ID는 필수입니다.")
        private Long productId;
        
        @NotNull(message = "수량은 필수입니다.")
        @Positive(message = "수량은 0보다 커야 합니다.")
        private Integer quantity;
    }
}
