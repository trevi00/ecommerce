package org.zb.ecommerce.domain.payment.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * 결제 요청 DTO
 * record 타입으로 불변성 보장
 */
public record PaymentRequest(
        @NotNull(message = "주문 ID는 필수입니다")
        @Positive(message = "주문 ID는 양수여야 합니다")
        Long orderId,
        
        @NotBlank(message = "결제 수단은 필수입니다")
        @Size(max = 50, message = "결제 수단은 50자를 초과할 수 없습니다")
        String paymentMethod,
        
        @NotNull(message = "결제 금액은 필수입니다")
        @DecimalMin(value = "0.01", message = "결제 금액은 0.01 이상이어야 합니다")
        @Digits(integer = 10, fraction = 2, message = "결제 금액 형식이 올바르지 않습니다")
        BigDecimal amount
) {
    /**
     * record의 compact constructor
     * 추가적인 유효성 검사 수행
     */
    public PaymentRequest {
        if (paymentMethod != null && paymentMethod.trim().isEmpty()) {
            throw new IllegalArgumentException("결제 수단은 빈 문자열일 수 없습니다");
        }
        
        if (amount != null && amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("결제 금액은 0보다 커야 합니다");
        }
    }
}