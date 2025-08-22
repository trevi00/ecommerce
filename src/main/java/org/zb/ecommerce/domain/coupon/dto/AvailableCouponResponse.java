package org.zb.ecommerce.domain.coupon.dto;

import lombok.Builder;
import lombok.Getter;
import org.zb.ecommerce.domain.coupon.entity.Coupon;
import org.zb.ecommerce.domain.coupon.entity.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class AvailableCouponResponse {
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
    private boolean canDownload;
    private boolean isAlreadyOwned;
    
    public static AvailableCouponResponse from(Coupon coupon, boolean canDownload, boolean isAlreadyOwned) {
        return AvailableCouponResponse.builder()
                .id(coupon.getId())
                .name(coupon.getName())
                .code(coupon.getCode())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minOrderAmount(coupon.getMinOrderAmount())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .validFrom(coupon.getValidFrom())
                .validTo(coupon.getValidTo())
                .maxUsageCount(coupon.getMaxUsageCount())
                .currentUsageCount(coupon.getCurrentUsageCount())
                .canDownload(canDownload)
                .isAlreadyOwned(isAlreadyOwned)
                .build();
    }
}