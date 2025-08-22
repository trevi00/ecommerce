package org.zb.ecommerce.domain.payment.entity;

/**
 * 결제 상태 enum
 */
public enum PaymentStatus {
    PENDING("결제 대기"),
    COMPLETED("결제 완료"), 
    FAILED("결제 실패"),
    CANCELLED("결제 취소");
    
    private final String description;
    
    PaymentStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 결제가 완료된 상태인지 확인
     */
    public boolean isCompleted() {
        return this == COMPLETED;
    }
    
    /**
     * 결제 취소가 가능한 상태인지 확인
     */
    public boolean canCancel() {
        return this == PENDING;
    }
}