package org.zb.ecommerce.domain.user.exception;

import org.zb.ecommerce.global.exception.BusinessException;
import org.zb.ecommerce.global.exception.ErrorCode;

/**
 * 이메일이 이미 존재할 때 발생하는 예외
 */
public class EmailAlreadyExistsException extends BusinessException {
    
    public EmailAlreadyExistsException() {
        super(ErrorCode.EMAIL_ALREADY_EXISTS);
    }
    
    public EmailAlreadyExistsException(String email) {
        super(ErrorCode.EMAIL_ALREADY_EXISTS, "이미 존재하는 이메일입니다: " + email);
    }
}
