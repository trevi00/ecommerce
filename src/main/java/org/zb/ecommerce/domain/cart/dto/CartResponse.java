package org.zb.ecommerce.domain.cart.dto;

import lombok.Builder;
import lombok.Getter;
import org.zb.ecommerce.domain.cart.entity.Cart;
import org.zb.ecommerce.domain.cart.entity.CartItem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class CartResponse {
    private Long id;
    private Long userId;
    private List<CartItemResponse> items;
    private int totalItemCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static CartResponse from(Cart cart, List<CartItem> cartItems) {
        int totalItemCount = cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
                
        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .items(cartItems.stream()
                        .map(CartItemResponse::from)
                        .collect(Collectors.toList()))
                .totalItemCount(totalItemCount)
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
}