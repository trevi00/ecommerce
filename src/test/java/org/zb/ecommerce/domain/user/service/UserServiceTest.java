package org.zb.ecommerce.domain.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import org.zb.ecommerce.global.auth.JwtTokenProvider;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * UserService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @InjectMocks
    private UserService userService;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .name("홍길동")
                .phone("010-1234-5678")
                .role(UserRole.GENERAL)
                .build();
    }
    
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
            
            given(userRepository.existsByEmail(request.email())).willReturn(false);
            given(passwordEncoder.encode(request.password())).willReturn("encodedPassword");
            given(userRepository.save(any(User.class))).willReturn(testUser);
            
            // when
            UserResponse response = userService.signUp(request);
            
            // then - assertAll로 여러 검증을 그룹화
            assertAll(
                "회원가입 응답 검증",
                () -> assertNotNull(response, "응답이 null이 아니어야 함"),
                () -> assertEquals(request.email(), response.getEmail()),
                () -> assertEquals(request.name(), response.getName()),
                () -> assertNotNull(response.getRole(), "역할이 설정되어야 함"),
                () -> assertTrue(response.getEmail().contains("@"), "유효한 이메일 형식이어야 함"),
                () -> assertFalse(response.getName().isEmpty(), "이름이 비어있으면 안됨")
            );
            
            // Mockito verify 검증 추가
            verify(userRepository).save(any(User.class));
            verify(passwordEncoder).encode(request.password());
            verify(userRepository).existsByEmail(request.email());
        }
        
        @Test
        @DisplayName("이미 존재하는 이메일로 회원가입 시 예외 발생")
        void signUp_EmailAlreadyExists_ThrowsException() {
            // given
            SignUpRequest request = new SignUpRequest(
                    "test@example.com",
                    "password123",
                    "홍길동",
                    null
            );
            
            given(userRepository.existsByEmail(request.email())).willReturn(true);
            
            // when & then
            assertThatThrownBy(() -> userService.signUp(request))
                    .isInstanceOf(EmailAlreadyExistsException.class);
            
            verify(userRepository, never()).save(any(User.class));
        }
    }
    
    @Nested
    @DisplayName("로그인")
    class Login {
        
        @Test
        @DisplayName("정상적인 로그인 - 종합적인 검증")
        void login_Success() {
            // given
            LoginRequest request = new LoginRequest(
                    "test@example.com",
                    "password123"
            );
            
            given(userRepository.findByEmail(request.email())).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches(request.password(), testUser.getPassword())).willReturn(true);
            given(jwtTokenProvider.createToken(any(), anyString())).willReturn("jwt-token");
            given(jwtTokenProvider.getExpirationTime()).willReturn(86400L);
            
            // when
            LoginResponse response = userService.login(request);
            
            // then - assertAll과 다양한 assertion 활용
            assertAll(
                "로그인 응답 검증",
                () -> assertNotNull(response, "로그인 응답이 null이 아니어야 함"),
                () -> assertEquals("jwt-token", response.getToken()),
                () -> assertTrue(response.getToken().startsWith("jwt")),
                () -> assertFalse(response.getToken().isEmpty()),
                () -> assertNotNull(response.getUser()),
                () -> assertEquals(testUser.getEmail(), response.getUser().getEmail()),
                () -> assertTrue(response.getExpiresIn() > 0)
            );
            
            // Mockito verify 검증
            verify(userRepository).findByEmail(request.email());
            verify(passwordEncoder).matches(request.password(), testUser.getPassword());
            verify(jwtTokenProvider).createToken(any(), anyString());
            verify(jwtTokenProvider).getExpirationTime();
        }
        
        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 시 예외 발생")
        void login_UserNotFound_ThrowsException() {
            // given
            LoginRequest request = new LoginRequest(
                    "notfound@example.com",
                    "password123"
            );
            
            given(userRepository.findByEmail(request.email())).willReturn(Optional.empty());
            
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
            
            given(userRepository.findByEmail(request.email())).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches(request.password(), testUser.getPassword())).willReturn(false);
            
            // when & then
            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(InvalidPasswordException.class);
        }
    }
    
    @Nested
    @DisplayName("회원 탈퇴")
    class Withdraw {
        
        @Test
        @DisplayName("정상적인 회원 탈퇴 - assertThrows와 verify 검증")
        void withdraw_Success() {
            // given
            Long userId = 1L;
            String password = "password123";
            
            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches(password, testUser.getPassword())).willReturn(true);
            
            // when & then - 예외가 발생하지 않음을 확인
            assertAll(
                "회원 탈퇴 검증",
                () -> assertDoesNotThrow(() -> userService.withdraw(userId, password)),
                () -> assertNotNull(testUser, "테스트 사용자가 존재해야 함"),
                () -> assertTrue(userId > 0, "유효한 사용자 ID여야 함"),
                () -> assertFalse(password.isEmpty(), "비밀번호가 비어있으면 안됨")
            );
            
            // Mockito verify 검증
            verify(userRepository).findById(userId);
            verify(passwordEncoder).matches(password, testUser.getPassword());
            verify(userRepository).deleteById(userId);
        }
        
        @Test
        @DisplayName("존재하지 않는 사용자 탈퇴 시 예외 발생")
        void withdraw_UserNotFound_ThrowsException() {
            // given
            Long userId = 999L;
            String password = "password123";
            
            given(userRepository.findById(userId)).willReturn(Optional.empty());
            
            // when & then
            assertThatThrownBy(() -> userService.withdraw(userId, password))
                    .isInstanceOf(UserNotFoundException.class);
        }
        
        @Test
        @DisplayName("잘못된 비밀번호로 탈퇴 시 예외 발생")
        void withdraw_InvalidPassword_ThrowsException() {
            // given
            Long userId = 1L;
            String password = "wrongPassword";
            
            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches(password, testUser.getPassword())).willReturn(false);
            
            // when & then
            assertThatThrownBy(() -> userService.withdraw(userId, password))
                    .isInstanceOf(InvalidPasswordException.class);
        }
    }
}
