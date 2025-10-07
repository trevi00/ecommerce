package org.zb.ecommerce.global.auth;

import java.lang.annotation.*;

/**
 * 로그인이 필요한 메서드에 사용하는 어노테이션
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LoginRequired {
}
