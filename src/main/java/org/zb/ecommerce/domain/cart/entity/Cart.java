package org.zb.ecommerce.domain.cart.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.zb.ecommerce.domain.common.BaseTimeEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 장바구니 Entity
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table("carts")
public class Cart extends BaseTimeEntity {
    
    @Id
    private Long id;
    
    private Long userId;
    
    @MappedCollection(idColumn = "cart_id")
    private List<CartItem> cartItems = new ArrayList<>();
    
    @Builder
    public Cart(Long userId) {
        validateUserId(userId);
        this.userId = userId;
    }
    
    // 비즈니스 로직
    public void addItem(Long productId, Integer quantity) {
        validateProductId(productId);
        validateQuantity(quantity);
        
        Optional<CartItem> existingItem = findItemByProductId(productId);
        
        if (existingItem.isPresent()) {
            existingItem.get().increaseQuantity(quantity);
        } else {
            CartItem newItem = CartItem.builder()
                    .productId(productId)
                    .quantity(quantity)
                    .build();
            this.cartItems.add(newItem);
        }
    }
    
    public void updateItemQuantity(Long productId, Integer quantity) {
        validateProductId(productId);
        validateQuantity(quantity);
        
        CartItem item = findItemByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니에 해당 상품이 없습니다."));
        
        item.updateQuantity(quantity);
    }
    
    public void removeItem(Long productId) {
        validateProductId(productId);
        
        boolean removed = cartItems.removeIf(item -> item.getProductId().equals(productId));
        if (!removed) {
            throw new IllegalArgumentException("장바구니에 해당 상품이 없습니다.");
        }
    }
    
    public void clear() {
        this.cartItems.clear();
    }
    
    public int getItemCount() {
        return cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
    
    private Optional<CartItem> findItemByProductId(Long productId) {
        return cartItems.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();
    }
    
    // 검증 로직
    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("사용자 ID가 올바르지 않습니다.");
        }
    }
    
    private void validateProductId(Long productId) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("상품 ID가 올바르지 않습니다.");
        }
    }
    
    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("수량은 0보다 커야 합니다.");
        }
    }
}
