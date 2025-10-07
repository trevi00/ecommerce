package org.zb.ecommerce.domain.admin.dto;

import jakarta.validation.constraints.*;
import org.zb.ecommerce.domain.coupon.entity.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 쿠폰 생성 요청 DTO
 */
public record CreateCouponRequest(
    @NotBlank(message = "쿠폰 이름은 필수입니다.")
    @Size(max = 100, message = "쿠폰 이름은 100자를 초과할 수 없습니다.")
    String name,
    
    @NotBlank(message = "쿠폰 코드는 필수입니다.")
    @Size(max = 50, message = "쿠폰 코드는 50자를 초과할 수 없습니다.")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "쿠폰 코드는 대문자와 숫자만 포함해야 합니다.")
    String code,
    
    @NotNull(message = "할인 타입은 필수입니다.")
    DiscountType discountType,
    
    @NotNull(message = "할인 값은 필수입니다.")
    @DecimalMin(value = "0.01", message = "할인 값은 0.01 이상이어야 합니다.")
    BigDecimal discountValue,
    
    @DecimalMin(value = "0", message = "최소 주문 금액은 0 이상이어야 합니다.")
    BigDecimal minOrderAmount,
    
    @DecimalMin(value = "0", message = "최대 할인 금액은 0 이상이어야 합니다.")
    BigDecimal maxDiscountAmount,
    
    @NotNull(message = "유효 시작일은 필수입니다.")
    LocalDateTime validFrom,
    
    @NotNull(message = "유효 종료일은 필수입니다.")
    LocalDateTime validTo,
    
    @Min(value = 1, message = "최대 사용 횟수는 1 이상이어야 합니다.")
    Integer maxUsageCount
) {
    public CreateCouponRequest {
        if (validFrom != null && validTo != null && validFrom.isAfter(validTo)) {
            throw new IllegalArgumentException("유효 시작일은 종료일보다 이전이어야 합니다.");
        }
        
        if (discountType == DiscountType.PERCENTAGE && 
            discountValue != null && discountValue.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("퍼센트 할인은 100%를 초과할 수 없습니다.");
        }
    }
}