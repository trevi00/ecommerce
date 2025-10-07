package org.zb.ecommerce.domain.coupon.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DownloadCouponRequest {
    
    private String couponCode;
    
    public DownloadCouponRequest(String couponCode) {
        this.couponCode = couponCode;
    }
}