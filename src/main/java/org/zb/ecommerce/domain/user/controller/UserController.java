package org.zb.ecommerce.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zb.ecommerce.domain.user.dto.LoginRequest;
import org.zb.ecommerce.domain.user.dto.LoginResponse;
import org.zb.ecommerce.domain.user.dto.SignUpRequest;
import org.zb.ecommerce.domain.user.dto.UserResponse;
import org.zb.ecommerce.domain.user.dto.WithdrawRequest;
import org.zb.ecommerce.domain.user.service.UserService;
import org.zb.ecommerce.global.auth.AuthUser;
import org.zb.ecommerce.global.auth.LoginRequired;

/**
 * 사용자 관련 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        UserResponse response = userService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 회원 탈퇴
     */
    @DeleteMapping("/withdraw")
    @LoginRequired
    public ResponseEntity<Void> withdraw(
            @AuthUser Long userId,
            @Valid @RequestBody WithdrawRequest request) {
        userService.withdraw(userId, request.getPassword());
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 내 정보 조회
     */
    @GetMapping("/me")
    @LoginRequired
    public ResponseEntity<UserResponse> getMyInfo(@AuthUser Long userId) {
        UserResponse response = userService.getUserInfo(userId);
        return ResponseEntity.ok(response);
    }
}
