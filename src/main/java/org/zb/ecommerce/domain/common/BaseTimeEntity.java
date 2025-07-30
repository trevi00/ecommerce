package org.zb.ecommerce.domain.common;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

/**
 * 공통 시간 정보를 담는 Base Entity
 * DDD의 Value Object 역할
 */
@Getter
public abstract class BaseTimeEntity {
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
