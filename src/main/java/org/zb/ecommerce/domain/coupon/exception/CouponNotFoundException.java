package org.zb.ecommerce.domain.coupon.exception;

import org.zb.ecommerce.global.exception.BusinessException;
import org.zb.ecommerce.global.exception.ErrorCode;

public class CouponNotFoundException extends BusinessException {
    
    public CouponNotFoundException() {
        super(ErrorCode.NOT_FOUND, "쿠폰을 찾을 수 없습니다.");
    }
    
    public CouponNotFoundException(String message) {
        super(ErrorCode.NOT_FOUND, message);
    }
}