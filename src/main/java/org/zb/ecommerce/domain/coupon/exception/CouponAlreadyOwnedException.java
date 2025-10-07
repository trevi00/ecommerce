package org.zb.ecommerce.domain.coupon.exception;

import org.zb.ecommerce.global.exception.BusinessException;
import org.zb.ecommerce.global.exception.ErrorCode;

public class CouponAlreadyOwnedException extends BusinessException {
    
    public CouponAlreadyOwnedException() {
        super(ErrorCode.CONFLICT, "이미 보유중인 쿠폰입니다.");
    }
    
    public CouponAlreadyOwnedException(String message) {
        super(ErrorCode.CONFLICT, message);
    }
}