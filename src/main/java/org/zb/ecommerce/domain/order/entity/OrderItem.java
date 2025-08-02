package org.zb.ecommerce.domain.order.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

/**
 * 주문 항목 Entity
 * Order의 일부로 관리되는 Value Object 역할
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table("order_items")
public class OrderItem {
    
    @Id
    private Long id;
    
    private Long productId;
    
    private Integer quantity;
    
    private BigDecimal unitPrice;
    
    private BigDecimal totalPrice;
    
    @Builder
    public OrderItem(Long productId, Integer quantity, BigDecimal unitPrice) {
        validateProductId(productId);
        validateQuantity(quantity);
        validateUnitPrice(unitPrice);
        
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = calculateTotalPrice();
    }
    
    // 비즈니스 로직
    private BigDecimal calculateTotalPrice() {
        return this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
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
    
    private void validateUnitPrice(BigDecimal unitPrice) {
        if (unitPrice == null) {
            throw new IllegalArgumentException("단가는 필수입니다.");
        }
        if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("단가는 0보다 커야 합니다.");
        }
    }
}
