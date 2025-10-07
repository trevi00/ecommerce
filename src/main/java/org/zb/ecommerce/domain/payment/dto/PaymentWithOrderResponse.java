package org.zb.ecommerce.domain.payment.dto;

import lombok.Builder;
import lombok.Getter;
import org.zb.ecommerce.domain.order.dto.OrderResponse;
import org.zb.ecommerce.domain.payment.entity.Payment;
import org.zb.ecommerce.domain.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 주문 정보와 함께 제공되는 결제 응답 DTO
 */
@Getter
@Builder
public class PaymentWithOrderResponse {
    private Long id;
    private Long orderId;
    private String paymentMethod;
    private BigDecimal amount;
    private PaymentStatus status;
    private LocalDateTime paymentDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private OrderResponse order;
    
    public static PaymentWithOrderResponse from(Payment payment, OrderResponse order) {
        return PaymentWithOrderResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .paymentMethod(payment.getPaymentMethod())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paymentDate(payment.getPaymentDate())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .order(order)
                .build();
    }
}