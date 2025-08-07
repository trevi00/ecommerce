package org.zb.ecommerce.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

/**
 * 사용자 비즈니스 로직을 처리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    /**
     * 회원가입
     */
    @Transactional
    public UserResponse signUp(SignUpRequest request) {
        // 이메일 중복 검사
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }
        
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.password());
        
        // User 엔티티 생성
        User user = User.builder()
                .email(request.email())
                .password(encodedPassword)
                .name(request.name())
                .phone(request.phone())
                .role(UserRole.GENERAL)
                .build();
        
        // 저장
        User savedUser = userRepository.save(user);
        log.info("New user signed up: {}", savedUser.getEmail());
        
        return UserResponse.from(savedUser);
    }
    
    /**
     * 로그인
     */
    public LoginResponse login(LoginRequest request) {
        // 사용자 조회
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("이메일 또는 비밀번호가 일치하지 않습니다."));
        
        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidPasswordException();
        }
        
        // JWT 토큰 생성
        String token = jwtTokenProvider.createToken(user.getId(), user.getEmail());
        Long expiresIn = jwtTokenProvider.getExpirationTime();
        
        log.info("User logged in: {}", user.getEmail());
        
        return LoginResponse.of(token, expiresIn, UserResponse.from(user));
    }
    
    /**
     * 회원 탈퇴
     */
    @Transactional
    public void withdraw(Long userId, String password) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());
        
        // 비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidPasswordException();
        }
        
        // 사용자 삭제
        userRepository.deleteById(userId);
        log.info("User withdrawn: {}", user.getEmail());
    }
    
    /**
     * 사용자 정보 조회
     */
    public UserResponse getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());
        
        return UserResponse.from(user);
    }
}
