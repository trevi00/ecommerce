package org.zb.ecommerce.domain.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 결제 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
public class CreatePaymentRequest {
    
    private Long orderId;
    private String paymentMethod;
    private BigDecimal amount;
    
    public CreatePaymentRequest(Long orderId, String paymentMethod, BigDecimal amount) {
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
    }
}