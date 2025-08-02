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
        @DisplayName("정상적인 회원가입")
        void signUp_Success() {
            // given
            SignUpRequest request = SignUpRequest.builder()
                    .email("test@example.com")
                    .password("password123")
                    .name("홍길동")
                    .phone("010-1234-5678")
                    .build();
            
            given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
            given(passwordEncoder.encode(request.getPassword())).willReturn("encodedPassword");
            given(userRepository.save(any(User.class))).willReturn(testUser);
            
            // when
            UserResponse response = userService.signUp(request);
            
            // then
            assertThat(response.getEmail()).isEqualTo(request.getEmail());
            assertThat(response.getName()).isEqualTo(request.getName());
            verify(userRepository).save(any(User.class));
        }
        
        @Test
        @DisplayName("이미 존재하는 이메일로 회원가입 시 예외 발생")
        void signUp_EmailAlreadyExists_ThrowsException() {
            // given
            SignUpRequest request = SignUpRequest.builder()
                    .email("test@example.com")
                    .password("password123")
                    .name("홍길동")
                    .build();
            
            given(userRepository.existsByEmail(request.getEmail())).willReturn(true);
            
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
        @DisplayName("정상적인 로그인")
        void login_Success() {
            // given
            LoginRequest request = LoginRequest.builder()
                    .email("test@example.com")
                    .password("password123")
                    .build();
            
            given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches(request.getPassword(), testUser.getPassword())).willReturn(true);
            given(jwtTokenProvider.createToken(any(), anyString())).willReturn("jwt-token");
            given(jwtTokenProvider.getExpirationTime()).willReturn(86400L);
            
            // when
            LoginResponse response = userService.login(request);
            
            // then
            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getUser().getEmail()).isEqualTo(testUser.getEmail());
        }
        
        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 시 예외 발생")
        void login_UserNotFound_ThrowsException() {
            // given
            LoginRequest request = LoginRequest.builder()
                    .email("notfound@example.com")
                    .password("password123")
                    .build();
            
            given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());
            
            // when & then
            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(UserNotFoundException.class);
        }
        
        @Test
        @DisplayName("잘못된 비밀번호로 로그인 시 예외 발생")
        void login_InvalidPassword_ThrowsException() {
            // given
            LoginRequest request = LoginRequest.builder()
                    .email("test@example.com")
                    .password("wrongPassword")
                    .build();
            
            given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches(request.getPassword(), testUser.getPassword())).willReturn(false);
            
            // when & then
            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(InvalidPasswordException.class);
        }
    }
    
    @Nested
    @DisplayName("회원 탈퇴")
    class Withdraw {
        
        @Test
        @DisplayName("정상적인 회원 탈퇴")
        void withdraw_Success() {
            // given
            Long userId = 1L;
            String password = "password123";
            
            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches(password, testUser.getPassword())).willReturn(true);
            
            // when
            userService.withdraw(userId, password);
            
            // then
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
