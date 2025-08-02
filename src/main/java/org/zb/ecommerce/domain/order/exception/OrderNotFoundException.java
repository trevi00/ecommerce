package org.zb.ecommerce.domain.order.exception;

import org.zb.ecommerce.global.exception.BusinessException;
import org.zb.ecommerce.global.exception.ErrorCode;

/**
 * 주문을 찾을 수 없을 때 발생하는 예외
 */
public class OrderNotFoundException extends BusinessException {
    
    public OrderNotFoundException() {
        super(ErrorCode.ORDER_NOT_FOUND);
    }
    
    public OrderNotFoundException(Long orderId) {
        super(ErrorCode.ORDER_NOT_FOUND, "주문을 찾을 수 없습니다. ID: " + orderId);
    }
}
