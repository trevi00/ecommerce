package org.zb.ecommerce.domain.coupon.entity;

/**
 * 할인 타입 Enum
 */
public enum DiscountType {
    PERCENTAGE("비율 할인"),
    FIXED_AMOUNT("정액 할인");
    
    private final String description;
    
    DiscountType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
