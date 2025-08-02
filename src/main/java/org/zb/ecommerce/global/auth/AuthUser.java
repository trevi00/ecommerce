package org.zb.ecommerce.global.auth;

import java.lang.annotation.*;

/**
 * 인증된 사용자 ID를 주입받기 위한 어노테이션
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuthUser {
}
