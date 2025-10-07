package org.zb.ecommerce.domain.product.exception;

import org.zb.ecommerce.global.exception.BusinessException;
import org.zb.ecommerce.global.exception.ErrorCode;

/**
 * 재고가 부족할 때 발생하는 예외
 */
public class InsufficientStockException extends BusinessException {
    
    public InsufficientStockException() {
        super(ErrorCode.INSUFFICIENT_STOCK);
    }
    
    public InsufficientStockException(String productName) {
        super(ErrorCode.INSUFFICIENT_STOCK, "재고가 부족합니다. 상품: " + productName);
    }
}
