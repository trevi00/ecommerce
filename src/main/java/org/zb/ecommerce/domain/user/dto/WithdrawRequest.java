package org.zb.ecommerce.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원 탈퇴 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawRequest {
    
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}
