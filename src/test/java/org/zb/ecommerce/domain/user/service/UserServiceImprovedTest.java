package org.zb.ecommerce.domain.user.service;



import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

/**
 * UserService 개선된 테스트 클래스
 * - @Nested, @DisplayName 활용
 * - 다양한 assertion 메서드 사용
 * - Mock 기반 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 테스트")
class UserServiceImprovedTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserService userService;



    @Nested
    @DisplayName("회원가입 테스트")
    class SignUpTest {

        @Test
        @DisplayName("정상적인 회원가입이 성공한다")
        void signUp_Success() {
            // given
            SignUpRequest signUpRequest = new SignUpRequest(
                    "test@example.com",
                    "password123",
                    "테스트사용자",
                    "010-1234-5678"
            );

            User savedUser = User.builder()
                    .email(signUpRequest.email())
                    .password("encoded_password")
                    .name(signUpRequest.name())
                    .phone(signUpRequest.phone())
                    .role(UserRole.GENERAL)
                    .build();
            
            // 테스트를 위해 ID 설정
            ReflectionTestUtils.setField(savedUser, "id", 1L);

            given(userRepository.existsByEmail(signUpRequest.email())).willReturn(false);
            given(passwordEncoder.encode(signUpRequest.password())).willReturn("encoded_password");
            given(userRepository.save(any(User.class))).willReturn(savedUser);

            // when
            UserResponse result = userService.signUp(signUpRequest);

            // then - 다양한 assertion 메서드 활용
            assertAll("회원가입 결과 검증",
                    () -> assertNotNull(result, "결과는 null이 아니어야 한다"),
                    () -> assertEquals(1L, result.id(), "ID가 일치해야 한다"),
                    () -> assertEquals(signUpRequest.email(), result.email(), "이메일이 일치해야 한다"),
                    () -> assertEquals(signUpRequest.name(), result.name(), "이름이 일치해야 한다"),
                    () -> assertEquals(signUpRequest.phone(), result.phone(), "전화번호가 일치해야 한다"),
                    () -> assertEquals(UserRole.GENERAL, result.role(), "기본 역할은 GENERAL이어야 한다")
            );

            assertThat(result.email()).isEqualTo(signUpRequest.email());
            assertTrue(result.id() > 0, "ID는 0보다 커야 한다");

            // Mockito verify 검증
            verify(userRepository).existsByEmail(signUpRequest.email());
            verify(passwordEncoder).encode(signUpRequest.password());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("이미 존재하는 이메일로 회원가입 시 예외가 발생한다")
        void signUp_EmailAlreadyExists_ThrowsException() {
            // given
            SignUpRequest signUpRequest = new SignUpRequest(
                    "existing@example.com",
                    "password123",
                    "테스트사용자",
                    "010-1234-5678"
            );

            given(userRepository.existsByEmail(signUpRequest.email())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.signUp(signUpRequest))
                    .isInstanceOf(EmailAlreadyExistsException.class);

            // 검증
            verify(userRepository).existsByEmail(signUpRequest.email());
            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTest {

        @Test
        @DisplayName("올바른 자격증명으로 로그인이 성공한다")
        void login_Success() {
            // given
            LoginRequest loginRequest = new LoginRequest(
                    "test@example.com",
                    "password123"
            );

            User user = User.builder()
                    .email(loginRequest.email())
                    .password("encoded_password")
                    .name("테스트사용자")
                    .phone("010-1234-5678")
                    .role(UserRole.GENERAL)
                    .build();
            
            // 테스트를 위해 ID 설정
            ReflectionTestUtils.setField(user, "id", 1L);

            String expectedToken = "jwt.token.here";
            Long expiresIn = 3600L;

            given(userRepository.findByEmail(loginRequest.email())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(loginRequest.password(), user.getPassword())).willReturn(true);
            given(jwtTokenProvider.createToken(user.getId(), user.getEmail())).willReturn(expectedToken);
            given(jwtTokenProvider.getExpirationTime()).willReturn(expiresIn);

            // when
            LoginResponse result = userService.login(loginRequest);

            // then
            assertAll("로그인 결과 검증",
                    () -> assertNotNull(result, "로그인 결과는 null이 아니어야 한다"),
                    () -> assertEquals(expectedToken, result.token(), "토큰이 일치해야 한다"),
                    () -> assertNotNull(result.user(), "사용자 정보는 null이 아니어야 한다"),
                    () -> assertEquals(user.getId(), result.user().id(), "사용자 ID가 일치해야 한다"),
                    () -> assertEquals(user.getEmail(), result.user().email(), "이메일이 일치해야 한다")
            );

            assertThat(result.token()).isNotEmpty();
            assertTrue(result.token().length() > 10, "토큰 길이는 10자보다 길어야 한다");

            // Mockito verify
            then(userRepository).should().findByEmail(loginRequest.email());
            then(passwordEncoder).should().matches(loginRequest.password(), user.getPassword());
            then(jwtTokenProvider).should().createToken(user.getId(), user.getEmail());
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 로그인 시 예외가 발생한다")
        void login_UserNotFound_ThrowsException() {
            // given
            LoginRequest loginRequest = new LoginRequest(
                    "nonexistent@example.com",
                    "password123"
            );

            given(userRepository.findByEmail(loginRequest.email())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.login(loginRequest))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessage("이메일 또는 비밀번호가 일치하지 않습니다.");

            // 다른 메서드들이 호출되지 않았는지 확인
            verify(passwordEncoder, never()).matches(anyString(), anyString());
            verify(jwtTokenProvider, never()).createToken(any(), anyString());
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 시 예외가 발생한다")
        void login_WrongPassword_ThrowsException() {
            // given
            LoginRequest loginRequest = new LoginRequest(
                    "test@example.com",
                    "wrongpassword"
            );

            User user = User.builder()
                    .email(loginRequest.email())
                    .password("encoded_password")
                    .name("테스트사용자")
                    .phone("010-1234-5678")
                    .role(UserRole.GENERAL)
                    .build();

            given(userRepository.findByEmail(loginRequest.email())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(loginRequest.password(), user.getPassword())).willReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.login(loginRequest))
                    .isInstanceOf(InvalidPasswordException.class);

            // JWT 토큰이 생성되지 않았는지 확인
            verify(jwtTokenProvider, never()).createToken(any(), anyString());
        }
    }

    @Nested
    @DisplayName("사용자 조회 테스트")
    class FindUserTest {

        @Test
        @DisplayName("ID로 사용자 조회가 성공한다")
        void findById_Success() {
            // given
            Long userId = 1L;
            User user = User.builder()
                    .email("test@example.com")
                    .name("테스트사용자")
                    .phone("010-1234-5678")
                    .password("password123456789") // 8자 이상
                    .role(UserRole.GENERAL)
                    .build();
            
            // 테스트를 위해 ID 설정
            ReflectionTestUtils.setField(user, "id", userId);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // when
            UserResponse result = userService.findById(userId);

            // then
            assertAll("사용자 조회 결과 검증",
                    () -> assertNotNull(result),
                    () -> assertEquals(userId, result.id()),
                    () -> assertEquals(user.getEmail(), result.email()),
                    () -> assertEquals(user.getName(), result.name())
            );

            assertThat(result)
                    .extracting("id", "email", "name")
                    .containsExactly(userId, user.getEmail(), user.getName());
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 예외가 발생한다")
        void findById_NotFound_ThrowsException() {
            // given
            Long nonExistentId = 999L;
            given(userRepository.findById(nonExistentId)).willReturn(Optional.empty());

            // when & then
            assertThrows(UserNotFoundException.class, 
                    () -> userService.findById(nonExistentId));

            verify(userRepository).findById(nonExistentId);
        }

        @Test
        @DisplayName("이메일로 사용자 조회가 성공한다")
        void findByEmail_Success() {
            // given
            String email = "test@example.com";
            User user = User.builder()
                    .email(email)
                    .name("테스트사용자")
                    .phone("010-1234-5678")
                    .password("password123456789") // 8자 이상
                    .role(UserRole.GENERAL)
                    .build();
            
            // 테스트를 위해 ID 설정
            ReflectionTestUtils.setField(user, "id", 1L);

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

            // when
            UserResponse result = userService.findByEmail(email);

            // then
            assertNotNull(result);
            assertEquals(email, result.email());
            assertThat(result.email()).isEqualTo(email);
        }
    }

    @Nested
    @DisplayName("사용자 검증 테스트")
    class ValidateUserTest {

        @Test
        @DisplayName("사용자 존재 여부 확인")
        void validateUserExists() {
            // given
            String email = "test@example.com";
            
            // when - 존재하는 경우
            given(userRepository.existsByEmail(email)).willReturn(true);
            boolean exists = userRepository.existsByEmail(email);
            
            // then
            assertTrue(exists, "사용자가 존재해야 한다");
            
            // when - 존재하지 않는 경우
            given(userRepository.existsByEmail("nonexistent@example.com")).willReturn(false);
            boolean notExists = userRepository.existsByEmail("nonexistent@example.com");
            
            // then
            assertFalse(notExists, "사용자가 존재하지 않아야 한다");
        }
    }
}