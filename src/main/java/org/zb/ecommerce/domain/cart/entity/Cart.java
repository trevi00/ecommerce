package org.zb.ecommerce.domain.cart.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.zb.ecommerce.domain.common.BaseTimeEntity;

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
    
    @Builder
    public Cart(Long userId) {
        validateUserId(userId);
        this.userId = userId;
    }
    
    // 검증 로직
    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("사용자 ID가 올바르지 않습니다.");
        }
    }
}