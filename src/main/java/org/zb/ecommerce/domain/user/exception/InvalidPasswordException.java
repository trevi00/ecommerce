package org.zb.ecommerce.domain.user.exception;

import org.zb.ecommerce.global.exception.BusinessException;
import org.zb.ecommerce.global.exception.ErrorCode;

/**
 * 비밀번호가 일치하지 않을 때 발생하는 예외
 */
public class InvalidPasswordException extends BusinessException {
    
    public InvalidPasswordException() {
        super(ErrorCode.INVALID_PASSWORD);
    }
}
