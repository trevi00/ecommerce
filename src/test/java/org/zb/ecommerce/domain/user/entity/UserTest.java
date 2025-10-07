package org.zb.ecommerce.domain.user.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.*;

/**
 * User Entity 단위 테스트
 * TDD 원칙에 따라 테스트 먼저 작성
 */
class UserTest {
    
    @Nested
    @DisplayName("User 생성")
    class UserCreation {
        
        @Test
        @DisplayName("정상적인 User 생성")
        void createUser_Success() {
            // given
            String email = "test@example.com";
            String password = "password123";
            String name = "홍길동";
            String phone = "010-1234-5678";
            
            // when
            User user = User.builder()
                    .email(email)
                    .password(password)
                    .name(name)
                    .phone(phone)
                    .role(UserRole.GENERAL)
                    .build();
            
            // then
            assertThat(user.getEmail()).isEqualTo(email);
            assertThat(user.getPassword()).isEqualTo(password);
            assertThat(user.getName()).isEqualTo(name);
            assertThat(user.getPhone()).isEqualTo(phone);
            assertThat(user.getRole()).isEqualTo(UserRole.GENERAL);
        }
        
        @Test
        @DisplayName("role이 null일 때 GENERAL로 설정")
        void createUser_WithNullRole_DefaultsToGeneral() {
            // when
            User user = User.builder()
                    .email("test@example.com")
                    .password("password123")
                    .name("홍길동")
                    .build();
            
            // then
            assertThat(user.getRole()).isEqualTo(UserRole.GENERAL);
        }
        
        @Test
        @DisplayName("이메일이 null일 때 예외 발생")
        void createUser_WithNullEmail_ThrowsException() {
            // when & then
            assertThatThrownBy(() -> User.builder()
                    .email(null)
                    .password("password123")
                    .name("홍길동")
                    .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이메일은 필수입니다.");
        }
        
        @Test
        @DisplayName("잘못된 이메일 형식일 때 예외 발생")
        void createUser_WithInvalidEmail_ThrowsException() {
            // when & then
            assertThatThrownBy(() -> User.builder()
                    .email("invalid-email")
                    .password("password123")
                    .name("홍길동")
                    .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("올바른 이메일 형식이 아닙니다.");
        }
        
        @Test
        @DisplayName("비밀번호가 8자 미만일 때 예외 발생")
        void createUser_WithShortPassword_ThrowsException() {
            // when & then
            assertThatThrownBy(() -> User.builder()
                    .email("test@example.com")
                    .password("short")
                    .name("홍길동")
                    .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("비밀번호는 8자 이상이어야 합니다.");
        }
    }
    
    @Nested
    @DisplayName("User 비즈니스 로직")
    class UserBusinessLogic {
        
        @Test
        @DisplayName("일반 회원을 VIP로 업그레이드")
        void upgradeToVip_Success() {
            // given
            User user = User.builder()
                    .email("test@example.com")
                    .password("password123")
                    .name("홍길동")
                    .role(UserRole.GENERAL)
                    .build();
            
            // when
            user.upgradeToVip();
            
            // then
            assertThat(user.getRole()).isEqualTo(UserRole.VIP);
        }
        
        @Test
        @DisplayName("이미 VIP인 회원 업그레이드 시 예외 발생")
        void upgradeToVip_AlreadyVip_ThrowsException() {
            // given
            User user = User.builder()
                    .email("test@example.com")
                    .password("password123")
                    .name("홍길동")
                    .role(UserRole.VIP)
                    .build();
            
            // when & then
            assertThatThrownBy(() -> user.upgradeToVip())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("이미 VIP 회원입니다.");
        }
        
        @Test
        @DisplayName("전화번호 업데이트")
        void updatePhone_Success() {
            // given
            User user = User.builder()
                    .email("test@example.com")
                    .password("password123")
                    .name("홍길동")
                    .phone("010-1234-5678")
                    .build();
            
            String newPhone = "010-9876-5432";
            
            // when
            user.updatePhone(newPhone);
            
            // then
            assertThat(user.getPhone()).isEqualTo(newPhone);
        }
        
        @Test
        @DisplayName("비밀번호 업데이트")
        void updatePassword_Success() {
            // given
            User user = User.builder()
                    .email("test@example.com")
                    .password("oldPassword123")
                    .name("홍길동")
                    .build();
            
            String newPassword = "newPassword123";
            
            // when
            user.updatePassword(newPassword);
            
            // then
            assertThat(user.getPassword()).isEqualTo(newPassword);
        }
    }
}
