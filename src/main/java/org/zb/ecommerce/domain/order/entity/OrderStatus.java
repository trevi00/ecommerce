package org.zb.ecommerce.domain.order.entity;

/**
 * 주문 상태 Enum
 */
public enum OrderStatus {
    PENDING("주문 대기"),
    CONFIRMED("주문 확정"),
    CANCELLED("주문 취소");
    
    private final String description;
    
    OrderStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean canCancel() {
        return this == PENDING || this == CONFIRMED;
    }
}
