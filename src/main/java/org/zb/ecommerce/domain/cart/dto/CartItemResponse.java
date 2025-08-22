package org.zb.ecommerce.domain.cart.dto;

import lombok.Builder;
import lombok.Getter;
import org.zb.ecommerce.domain.cart.entity.CartItem;

import java.time.LocalDateTime;

@Getter
@Builder
public class CartItemResponse {
    private Long id;
    private Long productId;
    private Integer quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static CartItemResponse from(CartItem cartItem) {
        return CartItemResponse.builder()
                .id(cartItem.getId())
                .productId(cartItem.getProductId())
                .quantity(cartItem.getQuantity())
                .createdAt(cartItem.getCreatedAt())
                .updatedAt(cartItem.getUpdatedAt())
                .build();
    }
}