package org.zb.ecommerce.domain.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 장바구니 아이템 수량 변경 요청 DTO - record로 변환
 */
public record UpdateCartItemRequest(
    @NotNull(message = "수량은 필수입니다.")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
    Integer quantity
) {}