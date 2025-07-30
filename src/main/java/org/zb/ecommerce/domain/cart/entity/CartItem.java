package org.zb.ecommerce.domain.cart.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.zb.ecommerce.domain.common.BaseTimeEntity;

/**
 * 장바구니 항목 Entity
 * Cart의 일부로 관리되는 Value Object 역할
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table("cart_items")
public class CartItem extends BaseTimeEntity {
    
    @Id
    private Long id;
    
    private Long productId;
    
    private Integer quantity;
    
    @Builder
    public CartItem(Long productId, Integer quantity) {
        validateProductId(productId);
        validateQuantity(quantity);
        
        this.productId = productId;
        this.quantity = quantity;
    }
    
    // 비즈니스 로직
    public void updateQuantity(Integer quantity) {
        validateQuantity(quantity);
        this.quantity = quantity;
    }
    
    public void increaseQuantity(Integer quantity) {
        validateQuantity(quantity);
        this.quantity += quantity;
    }
    
    public void decreaseQuantity(Integer quantity) {
        validateQuantity(quantity);
        if (this.quantity <= quantity) {
            throw new IllegalArgumentException("감소시킬 수량이 현재 수량보다 크거나 같습니다.");
        }
        this.quantity -= quantity;
    }
    
    // 검증 로직
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
