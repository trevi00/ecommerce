package org.zb.ecommerce.domain.user.exception;

/**
 * 커스텀 체크 예외 - 트랜잭션 롤백 테스트용
 */
public class CustomCheckedException extends Exception {
    
    public CustomCheckedException(String message) {
        super(message);
    }
    
    public CustomCheckedException(String message, Throwable cause) {
        super(message, cause);
    }
}