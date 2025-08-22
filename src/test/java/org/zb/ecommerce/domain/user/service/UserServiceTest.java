package org.zb.ecommerce.domain.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.zb.ecommerce.config.BaseIntegrationTest;
import org.zb.ecommerce.domain.user.dto.LoginRequest;
import org.zb.ecommerce.domain.user.dto.LoginResponse;
import org.zb.ecommerce.domain.user.dto.SignUpRequest;
import org.zb.ecommerce.domain.user.dto.UserResponse;
import org.zb.ecommerce.domain.user.entity.User;
import org.zb.ecommerce.domain.user.entity.UserRole;
import org.zb.ecommerce.domain.user.exception.EmailAlreadyExistsException;
import org.zb.ecommerce.domain.user.exception.InvalidPasswordException;
import org.zb.ecommerce.domain.user.exception.UserNotFoundException;
import org.zb.ecommerce.domain.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserService 통합 테스트")
class UserServiceTest extends BaseIntegrationTest {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Nested
    @DisplayName("회원가입")
    class SignUp {
        
        @Test
        @DisplayName("정상적인 회원가입 - assertAll과 다양한 assertion 사용")
        void signUp_Success() {
            // given
            SignUpRequest request = new SignUpRequest(
                    "test@example.com",
                    "password123",
                    "홍길동",
                    "010-1234-5678"
            );
            
            // when
            UserResponse response = userService.signUp(request);
            
            // then - assertAll로 여러 검증을 그룹화
            assertAll(
                "회원가입 응답 검증",
                () -> assertNotNull(response, "응답이 null이 아니어야 함"),
                () -> assertEquals(request.email(), response.email()),
                () -> assertEquals(request.name(), response.name()),
                () -> assertNotNull(response.role(), "역할이 설정되어야 함"),
                () -> assertTrue(response.email().contains("@"), "유효한 이메일 형식이어야 함"),
                () -> assertFalse(response.name().isEmpty(), "이름이 비어있으면 안됨")
            );
            
            // 데이터베이스에 실제로 저장되었는지 확인
            assertThat(userRepository.findByEmail(request.email())).isPresent();
        }
        
        @Test
        @DisplayName("이미 존재하는 이메일로 회원가입 시 예외 발생")
        void signUp_EmailAlreadyExists_ThrowsException() {
            // given
            SignUpRequest firstRequest = new SignUpRequest(
                    "test@example.com",
                    "password123",
                    "홍길동",
                    "010-1234-5678"
            );
            userService.signUp(firstRequest);
            
            SignUpRequest secondRequest = new SignUpRequest(
                    "test@example.com",
                    "password456",
                    "김철수",
                    "010-9876-5432"
            );
            
            // when & then
            assertThatThrownBy(() -> userService.signUp(secondRequest))
                    .isInstanceOf(EmailAlreadyExistsException.class);
        }
    }
    
    @Nested
    @DisplayName("로그인")
    class Login {
        
        @BeforeEach
        void setUp() {
            // 테스트용 사용자 생성
            SignUpRequest signUpRequest = new SignUpRequest(
                    "test@example.com",
                    "password123",
                    "홍길동",
                    "010-1234-5678"
            );
            userService.signUp(signUpRequest);
        }
        
        @Test
        @DisplayName("정상적인 로그인 - 종합적인 검증")
        void login_Success() {
            // given
            LoginRequest request = new LoginRequest(
                    "test@example.com",
                    "password123"
            );
            
            // when
            LoginResponse response = userService.login(request);
            
            // then - assertAll과 다양한 assertion 활용
            assertAll(
                "로그인 응답 검증",
                () -> assertNotNull(response, "로그인 응답이 null이 아니어야 함"),
                () -> assertNotNull(response.token(), "토큰이 생성되어야 함"),
                () -> assertFalse(response.token().isEmpty(), "토큰이 비어있으면 안됨"),
                () -> assertNotNull(response.user(), "사용자 정보가 포함되어야 함"),
                () -> assertEquals("test@example.com", response.user().email()),
                () -> assertTrue(response.expiresIn() > 0, "만료 시간이 설정되어야 함")
            );
        }
        
        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 시 예외 발생")
        void login_UserNotFound_ThrowsException() {
            // given
            LoginRequest request = new LoginRequest(
                    "notfound@example.com",
                    "password123"
            );
            
            // when & then
            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(UserNotFoundException.class);
        }
        
        @Test
        @DisplayName("잘못된 비밀번호로 로그인 시 예외 발생")
        void login_InvalidPassword_ThrowsException() {
            // given
            LoginRequest request = new LoginRequest(
                    "test@example.com",
                    "wrongPassword"
            );
            
            // when & then
            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(InvalidPasswordException.class);
        }
    }
    
    @Nested
    @DisplayName("회원 탈퇴")
    class Withdraw {
        
        private Long userId;
        
        @BeforeEach
        void setUp() {
            // 테스트용 사용자 생성
            SignUpRequest signUpRequest = new SignUpRequest(
                    "test@example.com",
                    "password123",
                    "홍길동",
                    "010-1234-5678"
            );
            UserResponse userResponse = userService.signUp(signUpRequest);
            // 실제 저장된 사용자의 ID를 가져와야 함
            User savedUser = userRepository.findByEmail("test@example.com").orElseThrow();
            userId = savedUser.getId();
        }
        
        @Test
        @DisplayName("정상적인 회원 탈퇴 - assertThrows와 verify 검증")
        void withdraw_Success() {
            // given
            String password = "password123";
            
            // when & then - 예외가 발생하지 않음을 확인
            assertAll(
                "회원 탈퇴 검증",
                () -> assertDoesNotThrow(() -> userService.withdraw(userId, password)),
                () -> assertNotNull(userId, "사용자 ID가 존재해야 함"),
                () -> assertTrue(userId > 0, "유효한 사용자 ID여야 함"),
                () -> assertFalse(password.isEmpty(), "비밀번호가 비어있으면 안됨")
            );
            
            // 데이터베이스에서 실제로 삭제되었는지 확인
            assertThat(userRepository.findById(userId)).isEmpty();
        }
        
        @Test
        @DisplayName("존재하지 않는 사용자 탈퇴 시 예외 발생")
        void withdraw_UserNotFound_ThrowsException() {
            // given
            Long nonExistentUserId = 999L;
            String password = "password123";
            
            // when & then
            assertThatThrownBy(() -> userService.withdraw(nonExistentUserId, password))
                    .isInstanceOf(UserNotFoundException.class);
        }
        
        @Test
        @DisplayName("잘못된 비밀번호로 탈퇴 시 예외 발생")
        void withdraw_InvalidPassword_ThrowsException() {
            // given
            String wrongPassword = "wrongPassword";
            
            // when & then
            assertThatThrownBy(() -> userService.withdraw(userId, wrongPassword))
                    .isInstanceOf(InvalidPasswordException.class);
        }
    }
}