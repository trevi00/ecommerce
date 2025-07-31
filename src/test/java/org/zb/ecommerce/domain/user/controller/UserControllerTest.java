package org.zb.ecommerce.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.zb.ecommerce.domain.user.dto.LoginRequest;
import org.zb.ecommerce.domain.user.dto.SignUpRequest;
import org.zb.ecommerce.domain.user.dto.WithdrawRequest;
import org.zb.ecommerce.domain.user.entity.User;
import org.zb.ecommerce.domain.user.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserRepository userRepository;
    
    private SignUpRequest signUpRequest;
    private LoginRequest loginRequest;
    
    @BeforeEach
    void setUp() {
        signUpRequest = SignUpRequest.builder()
                .email("test@example.com")
                .password("password123")
                .name("홍길동")
                .phone("010-1234-5678")
                .build();
        
        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();
    }
    
    @Test
    @DisplayName("회원가입 성공")
    void signUp_Success() throws Exception {
        // when & then
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(signUpRequest.getEmail()))
                .andExpect(jsonPath("$.name").value(signUpRequest.getName()));
    }
    
    @Test
    @DisplayName("중복된 이메일로 회원가입 시 실패")
    void signUp_DuplicateEmail_Fail() throws Exception {
        // given
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)));
        
        // when & then
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));
    }
    
    @Test
    @DisplayName("로그인 성공")
    void login_Success() throws Exception {
        // given
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)));
        
        // when & then
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value(loginRequest.getEmail()));
    }
    
    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시 실패")
    void login_InvalidPassword_Fail() throws Exception {
        // given
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)));
        
        LoginRequest wrongPasswordRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("wrongPassword")
                .build();
        
        // when & then
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongPasswordRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_PASSWORD"));
    }
    
    @Test
    @DisplayName("회원 탈퇴 성공")
    void withdraw_Success() throws Exception {
        // given
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)));
        
        String loginResponse = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        String token = objectMapper.readTree(loginResponse).get("token").asText();
        
        WithdrawRequest withdrawRequest = new WithdrawRequest("password123");
        
        // when & then
        mockMvc.perform(delete("/api/users/withdraw")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawRequest)))
                .andExpect(status().isNoContent());
    }
    
    @Test
    @DisplayName("내 정보 조회 성공")
    void getMyInfo_Success() throws Exception {
        // given
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)));
        
        String loginResponse = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        String token = objectMapper.readTree(loginResponse).get("token").asText();
        
        // when & then
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(signUpRequest.getEmail()))
                .andExpect(jsonPath("$.name").value(signUpRequest.getName()));
    }
    
    @Test
    @DisplayName("인증 없이 내 정보 조회 시 실패")
    void getMyInfo_WithoutAuth_Fail() throws Exception {
        // when & then
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }
}
