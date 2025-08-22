package org.zb.ecommerce.domain.order.dto;

/**
 * 장바구니에서 주문 생성 요청 DTO
 */
public record CreateOrderFromCartRequest(
    Long couponId  // 선택적 쿠폰 ID
) {
}