package org.zb.ecommerce.domain.coupon.exception;

import org.zb.ecommerce.global.exception.BusinessException;
import org.zb.ecommerce.global.exception.ErrorCode;

public class CouponNotAvailableException extends BusinessException {
    
    public CouponNotAvailableException() {
        super(ErrorCode.BAD_REQUEST, "다운로드할 수 없는 쿠폰입니다.");
    }
    
    public CouponNotAvailableException(String message) {
        super(ErrorCode.BAD_REQUEST, message);
    }
}