package org.zb.ecommerce.domain.payment.exception;

/**
 * 이미 결제가 존재할 때 발생하는 예외
 */
public class PaymentAlreadyExistsException extends RuntimeException {
    
    public PaymentAlreadyExistsException(Long orderId) {
        super("이미 해당 주문에 대한 결제가 존재합니다. 주문 ID: " + orderId);
    }
    
    public PaymentAlreadyExistsException(String message) {
        super(message);
    }
}