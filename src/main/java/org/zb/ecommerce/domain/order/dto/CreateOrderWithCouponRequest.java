package org.zb.ecommerce.domain.order.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CreateOrderWithCouponRequest {
    
    private List<OrderItemRequest> items;
    private Long userCouponId; // 사용할 사용자 쿠폰 ID
    
    public CreateOrderWithCouponRequest(List<OrderItemRequest> items, Long userCouponId) {
        this.items = items;
        this.userCouponId = userCouponId;
    }
    
    @Getter
    @NoArgsConstructor
    public static class OrderItemRequest {
        private Long productId;
        private Integer quantity;
        
        public OrderItemRequest(Long productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }
        
        public Long productId() {
            return productId;
        }
        
        public Integer quantity() {
            return quantity;
        }
    }
}