package org.zb.ecommerce.domain.product.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.zb.ecommerce.domain.common.BaseTimeEntity;

import java.math.BigDecimal;

/**
 * 상품 Entity
 * DDD의 Aggregate Root 역할
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table("products")
public class Product extends BaseTimeEntity {
    
    @Id
    private Long id;
    
    private String name;
    
    private String description;
    
    private BigDecimal price;
    
    private Integer stockQuantity;
    
    private String category;
    
    @Builder
    public Product(String name, String description, BigDecimal price, Integer stockQuantity, String category) {
        validateName(name);
        validatePrice(price);
        validateStockQuantity(stockQuantity);
        
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.category = category;
    }
    
    // 비즈니스 로직
    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 0보다 커야 합니다.");
        }
        if (this.stockQuantity < quantity) {
            throw new IllegalStateException("재고가 부족합니다. 현재 재고: " + this.stockQuantity);
        }
        this.stockQuantity -= quantity;
    }
    
    public void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 0보다 커야 합니다.");
        }
        this.stockQuantity += quantity;
    }
    
    public void updatePrice(BigDecimal newPrice) {
        validatePrice(newPrice);
        this.price = newPrice;
    }
    
    public void updateInfo(String name, String description, String category) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        this.description = description;
        this.category = category;
    }
    
    public boolean isAvailable(int quantity) {
        return this.stockQuantity >= quantity;
    }
    
    // 검증 로직
    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("상품명은 필수입니다.");
        }
    }
    
    private void validatePrice(BigDecimal price) {
        if (price == null) {
            throw new IllegalArgumentException("가격은 필수입니다.");
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("가격은 0보다 커야 합니다.");
        }
    }
    
    private void validateStockQuantity(Integer stockQuantity) {
        if (stockQuantity == null) {
            throw new IllegalArgumentException("재고 수량은 필수입니다.");
        }
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("재고 수량은 0 이상이어야 합니다.");
        }
    }
}
