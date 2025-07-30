package org.zb.ecommerce.domain.coupon.entity;

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
 * 쿠폰 Entity
 * DDD의 Aggregate Root 역할
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table("coupons")
public class Coupon extends BaseTimeEntity {
    
    @Id
    private Long id;
    
    private String name;
    
    private String code;
    
    private DiscountType discountType;
    
    private BigDecimal discountValue;
    
    private BigDecimal minOrderAmount;
    
    private BigDecimal maxDiscountAmount;
    
    private LocalDateTime validFrom;
    
    private LocalDateTime validTo;
    
    private Integer maxUsageCount;
    
    private Integer currentUsageCount;
    
    private Boolean isActive;
    
    @Builder
    public Coupon(String name, String code, DiscountType discountType, BigDecimal discountValue,
                  BigDecimal minOrderAmount, BigDecimal maxDiscountAmount, 
                  LocalDateTime validFrom, LocalDateTime validTo, Integer maxUsageCount) {
        
        validateName(name);
        validateCode(code);
        validateDiscountType(discountType);
        validateDiscountValue(discountValue, discountType);
        validateValidPeriod(validFrom, validTo);
        validateMaxUsageCount(maxUsageCount);
        
        this.name = name;
        this.code = code;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minOrderAmount = minOrderAmount != null ? minOrderAmount : BigDecimal.ZERO;
        this.maxDiscountAmount = maxDiscountAmount;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.maxUsageCount = maxUsageCount;
        this.currentUsageCount = 0;
        this.isActive = true;
    }
    
    // 비즈니스 로직
    public BigDecimal calculateDiscountAmount(BigDecimal orderAmount) {
        if (!canUse()) {
            throw new IllegalStateException("사용할 수 없는 쿠폰입니다.");
        }
        
        if (orderAmount.compareTo(minOrderAmount) < 0) {
            throw new IllegalArgumentException("최소 주문 금액을 충족하지 못했습니다. 최소 금액: " + minOrderAmount);
        }
        
        BigDecimal discountAmount;
        
        if (discountType == DiscountType.PERCENTAGE) {
            discountAmount = orderAmount.multiply(discountValue).divide(BigDecimal.valueOf(100));
        } else {
            discountAmount = discountValue;
        }
        
        // 최대 할인 금액 적용
        if (maxDiscountAmount != null && discountAmount.compareTo(maxDiscountAmount) > 0) {
            discountAmount = maxDiscountAmount;
        }
        
        // 할인 금액이 주문 금액을 초과하지 않도록
        if (discountAmount.compareTo(orderAmount) > 0) {
            discountAmount = orderAmount;
        }
        
        return discountAmount;
    }
    
    public void use() {
        if (!canUse()) {
            throw new IllegalStateException("사용할 수 없는 쿠폰입니다.");
        }
        this.currentUsageCount++;
    }
    
    public void deactivate() {
        this.isActive = false;
    }
    
    public void activate() {
        this.isActive = true;
    }
    
    public boolean canUse() {
        return isActive && 
               !isExpired() && 
               currentUsageCount < maxUsageCount;
    }
    
    public boolean isExpired() {
        LocalDateTime now = LocalDateTime.now();
        return now.isBefore(validFrom) || now.isAfter(validTo);
    }
    
    // 검증 로직
    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("쿠폰명은 필수입니다.");
        }
    }
    
    private void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("쿠폰 코드는 필수입니다.");
        }
    }
    
    private void validateDiscountType(DiscountType discountType) {
        if (discountType == null) {
            throw new IllegalArgumentException("할인 타입은 필수입니다.");
        }
    }
    
    private void validateDiscountValue(BigDecimal discountValue, DiscountType discountType) {
        if (discountValue == null) {
            throw new IllegalArgumentException("할인 값은 필수입니다.");
        }
        if (discountValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("할인 값은 0보다 커야 합니다.");
        }
        
        if (discountType == DiscountType.PERCENTAGE && discountValue.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("비율 할인은 100%를 초과할 수 없습니다.");
        }
    }
    
    private void validateValidPeriod(LocalDateTime validFrom, LocalDateTime validTo) {
        if (validFrom == null || validTo == null) {
            throw new IllegalArgumentException("유효 기간은 필수입니다.");
        }
        if (validFrom.isAfter(validTo)) {
            throw new IllegalArgumentException("유효 시작일이 종료일보다 늦을 수 없습니다.");
        }
    }
    
    private void validateMaxUsageCount(Integer maxUsageCount) {
        if (maxUsageCount == null || maxUsageCount <= 0) {
            throw new IllegalArgumentException("최대 사용 횟수는 0보다 커야 합니다.");
        }
    }
}
