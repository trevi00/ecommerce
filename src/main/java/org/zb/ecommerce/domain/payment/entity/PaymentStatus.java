package org.zb.ecommerce.domain.payment.entity;

/**
 * 결제 상태 Enum
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
    
    public boolean canCancel() {
        return this == COMPLETED;
    }
}
