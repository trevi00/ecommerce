package org.zb.ecommerce.global.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 에러 응답 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ErrorResponse {
    
    private String code;
    private String message;
    private LocalDateTime timestamp;
    
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.name(),
                errorCode.getMessage(),
                LocalDateTime.now()
        );
    }
    
    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(
                errorCode.name(),
                message,
                LocalDateTime.now()
        );
    }
}
