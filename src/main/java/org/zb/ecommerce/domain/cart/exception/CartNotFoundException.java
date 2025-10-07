package org.zb.ecommerce.domain.cart.exception;

import org.zb.ecommerce.global.exception.BusinessException;
import org.zb.ecommerce.global.exception.ErrorCode;

public class CartNotFoundException extends BusinessException {
    
    public CartNotFoundException() {
        super(ErrorCode.CART_NOT_FOUND, "장바구니를 찾을 수 없습니다.");
    }
    
    public CartNotFoundException(String message) {
        super(ErrorCode.CART_NOT_FOUND, message);
    }
}