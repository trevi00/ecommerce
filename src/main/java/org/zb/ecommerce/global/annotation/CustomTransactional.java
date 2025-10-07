package org.zb.ecommerce.global.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

/**
 * 커스텀 트랜잭션 어노테이션
 * Spring의 @Transactional을 확장하여 프로젝트 특화 설정을 제공
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Transactional
public @interface CustomTransactional {
    
    /**
     * 트랜잭션 전파 옵션
     */
    @AliasFor(annotation = Transactional.class)
    Propagation propagation() default Propagation.REQUIRED;
    
    /**
     * 트랜잭션 격리 레벨
     */
    @AliasFor(annotation = Transactional.class)
    Isolation isolation() default Isolation.DEFAULT;
    
    /**
     * 읽기 전용 여부
     */
    @AliasFor(annotation = Transactional.class)
    boolean readOnly() default false;
    
    /**
     * 롤백할 예외 클래스들
     */
    @AliasFor(annotation = Transactional.class)
    Class<? extends Throwable>[] rollbackFor() default {};
    
    /**
     * 롤백하지 않을 예외 클래스들
     */
    @AliasFor(annotation = Transactional.class)
    Class<? extends Throwable>[] noRollbackFor() default {};
    
    /**
     * 트랜잭션 매니저 이름
     */
    @AliasFor(annotation = Transactional.class)
    String value() default "";
    
    /**
     * 트랜잭션 매니저 이름 (alias for value)
     */
    @AliasFor(annotation = Transactional.class)
    String transactionManager() default "";
    
    /**
     * 타임아웃 (초)
     */
    @AliasFor(annotation = Transactional.class)
    int timeout() default -1;
}