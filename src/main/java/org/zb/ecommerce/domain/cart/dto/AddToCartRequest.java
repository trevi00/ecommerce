package org.zb.ecommerce.domain.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 장바구니 추가 요청 DTO
 */
public record AddToCartRequest(
    @NotNull(message = "상품 ID는 필수입니다.")
    Long productId,
    
    @NotNull(message = "수량은 필수입니다.")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
    Integer quantity
) {}