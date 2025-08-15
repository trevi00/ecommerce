package org.zb.ecommerce.domain.user.exception;

/**
 * 커스텀 런타임 예외 - 트랜잭션 롤백 테스트용
 */
public class CustomRuntimeException extends RuntimeException {
    
    public CustomRuntimeException(String message) {
        super(message);
    }
    
    public CustomRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}