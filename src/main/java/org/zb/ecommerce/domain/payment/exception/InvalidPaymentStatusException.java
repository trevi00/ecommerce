package org.zb.ecommerce.domain.payment.exception;

import org.zb.ecommerce.domain.payment.entity.PaymentStatus;

/**
 * 잘못된 결제 상태일 때 발생하는 예외
 */
public class InvalidPaymentStatusException extends RuntimeException {
    
    public InvalidPaymentStatusException(PaymentStatus currentStatus, String action) {
        super(String.format("현재 결제 상태(%s)에서는 %s 할 수 없습니다.", 
                currentStatus.getDescription(), action));
    }
    
    public InvalidPaymentStatusException(String message) {
        super(message);
    }
}