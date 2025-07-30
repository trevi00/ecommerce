package org.zb.ecommerce.domain.payment.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.zb.ecommerce.domain.common.BaseTimeEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 Entity
 * DDD의 Aggregate Root 역할
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table("payments")
public class Payment extends BaseTimeEntity {
    
    @Id
    private Long id;
    
    private Long orderId;
    
    private String paymentMethod;
    
    private BigDecimal amount;
    
    private PaymentStatus status;
    
    private LocalDateTime paymentDate;
    
    @Builder
    public Payment(Long orderId, String paymentMethod, BigDecimal amount) {
        validateOrderId(orderId);
        validatePaymentMethod(paymentMethod);
        validateAmount(amount);
        
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
    }
    
    // 비즈니스 로직
    public void complete() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("대기 중인 결제만 완료할 수 있습니다.");
        }
        this.status = PaymentStatus.COMPLETED;
        this.paymentDate = LocalDateTime.now();
    }
    
    public void fail() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("대기 중인 결제만 실패 처리할 수 있습니다.");
        }
        this.status = PaymentStatus.FAILED;
    }
    
    public void cancel() {
        if (!this.status.canCancel()) {
            throw new IllegalStateException("완료된 결제만 취소할 수 있습니다.");
        }
        this.status = PaymentStatus.CANCELLED;
    }
    
    // 검증 로직
    private void validateOrderId(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("주문 ID가 올바르지 않습니다.");
        }
    }
    
    private void validatePaymentMethod(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            throw new IllegalArgumentException("결제 수단은 필수입니다.");
        }
    }
    
    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("결제 금액은 필수입니다.");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("결제 금액은 0보다 커야 합니다.");
        }
    }
}
