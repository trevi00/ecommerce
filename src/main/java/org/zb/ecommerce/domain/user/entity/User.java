package org.zb.ecommerce.domain.user.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.zb.ecommerce.domain.common.BaseTimeEntity;

/**
 * 사용자 Entity
 * DDD의 Aggregate Root 역할
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table("users")
public class User extends BaseTimeEntity {
    
    @Id
    private Long id;
    
    private String email;
    
    private String password;
    
    private String name;
    
    private String phone;
    
    private UserRole role;
    
    @Builder
    public User(String email, String password, String name, String phone, UserRole role) {
        validateEmail(email);
        validatePassword(password);
        validateName(name);
        
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.role = role != null ? role : UserRole.GENERAL;
    }
    
    // 비즈니스 로직
    public void upgradeToVip() {
        if (this.role == UserRole.VIP) {
            throw new IllegalStateException("이미 VIP 회원입니다.");
        }
        this.role = UserRole.VIP;
    }
    
    public void updatePhone(String phone) {
        this.phone = phone;
    }
    
    public void updatePassword(String newPassword) {
        validatePassword(newPassword);
        this.password = newPassword;
    }
    
    // 검증 로직
    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다.");
        }
    }
    
    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다.");
        }
    }
    
    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }
    }
}
