package org.zb.ecommerce.domain.payment.dto;

import org.zb.ecommerce.domain.payment.entity.Payment;
import org.zb.ecommerce.domain.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 응답 DTO
 * record 타입으로 불변성 보장
 */
public record PaymentResponse(
        Long id,
        Long orderId,
        String paymentMethod,
        BigDecimal amount,
        PaymentStatus status,
        LocalDateTime paymentDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    
    /**
     * Entity를 DTO로 변환하는 팩토리 메서드
     */
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId() != null ? payment.getId() : 0L,
                payment.getOrderId(),
                payment.getPaymentMethod(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getPaymentDate(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
    
    /**
     * 결제가 완료되었는지 확인하는 편의 메서드
     */
    public boolean isCompleted() {
        return status != null && status.isCompleted();
    }
    
    /**
     * 결제 취소가 가능한지 확인하는 편의 메서드
     */
    public boolean canCancel() {
        return status != null && status.canCancel();
    }
}