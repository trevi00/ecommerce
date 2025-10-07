package org.zb.ecommerce.domain.payment.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 엔티티
 */
@Table("payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {
    
    @Id
    private Long id;
    
    private Long orderId;
    private String paymentMethod;
    private BigDecimal amount;
    private PaymentStatus status;
    private LocalDateTime paymentDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Builder
    public Payment(Long orderId, String paymentMethod, BigDecimal amount) {
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.status = PaymentStatus.PENDING; // 기본값은 대기 상태
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // === 비즈니스 로직 메서드 ===
    
    /**
     * 결제 완료 처리
     */
    public void complete() {
        if (!this.status.canCancel()) {
            throw new IllegalStateException("결제 완료할 수 없는 상태입니다: " + this.status);
        }
        
        this.status = PaymentStatus.COMPLETED;
        this.paymentDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 결제 취소 처리
     */
    public void cancel() {
        if (!this.status.canCancel()) {
            throw new IllegalStateException("결제 취소할 수 없는 상태입니다: " + this.status);
        }
        
        this.status = PaymentStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 결제 실패 처리
     */
    public void fail() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("결제 실패할 수 없는 상태입니다: " + this.status);
        }
        
        this.status = PaymentStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 결제가 완료되었는지 확인
     */
    public boolean isCompleted() {
        return this.status.isCompleted();
    }
    
    /**
     * 결제 취소가 가능한지 확인
     */
    public boolean canCancel() {
        return this.status.canCancel();
    }
}