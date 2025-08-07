package org.zb.ecommerce.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 로그인 요청 DTO - record로 변환
 */
public record LoginRequest(
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    String email,
    
    @NotBlank(message = "비밀번호는 필수입니다.")
    String password
) {}
