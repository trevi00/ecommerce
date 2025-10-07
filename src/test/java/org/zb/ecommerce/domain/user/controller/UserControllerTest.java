package org.zb.ecommerce.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.zb.ecommerce.config.TestContainerConfig;
import org.zb.ecommerce.domain.user.dto.LoginRequest;
import org.zb.ecommerce.domain.user.dto.SignUpRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Testcontainers
class UserControllerTest extends TestContainerConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    
    private String testEmail;
    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        testEmail = "user-test-" + System.currentTimeMillis() + "@example.com";
    }

    @Test
    @DisplayName("회원가입 성공")
    void signUp_Success() throws Exception {
        // given
        SignUpRequest request = new SignUpRequest(
                testEmail,
                "password123",
                "테스트사용자",
                "010-1234-5678"
        );

        // when & then
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(testEmail))
                .andExpect(jsonPath("$.name").value("테스트사용자"))
                .andExpect(jsonPath("$.phone").value("010-1234-5678"))
                .andExpect(jsonPath("$.role").value("GENERAL"));
    }

    @Test
    @DisplayName("중복된 이메일로 회원가입 시 실패")
    void signUp_DuplicateEmail_Fail() throws Exception {
        // given - 먼저 회원가입
        SignUpRequest firstRequest = new SignUpRequest(
                testEmail,
                "password123",
                "테스트사용자1",
                "010-1234-5678"
        );
        
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // 동일한 이메일로 다시 회원가입 시도
        SignUpRequest duplicateRequest = new SignUpRequest(
                testEmail,
                "password456",
                "테스트사용자2",
                "010-9876-5432"
        );

        // when & then
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() throws Exception {
        // given - 먼저 회원가입
        SignUpRequest signUpRequest = new SignUpRequest(
                testEmail,
                "password123",
                "테스트사용자",
                "010-1234-5678"
        );
        
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isCreated());

        // 로그인 요청
        LoginRequest loginRequest = new LoginRequest(
                testEmail,
                "password123"
        );

        // when & then
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.email").value(testEmail))
                .andExpect(jsonPath("$.user.name").value("테스트사용자"));
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시 실패")
    void login_InvalidPassword_Fail() throws Exception {
        // given - 먼저 회원가입
        SignUpRequest signUpRequest = new SignUpRequest(
                testEmail,
                "password123",
                "테스트사용자",
                "010-1234-5678"
        );
        
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isCreated());

        // 잘못된 비밀번호로 로그인 시도
        LoginRequest loginRequest = new LoginRequest(
                testEmail,
                "wrongPassword"
        );

        // when & then
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 로그인 시 실패")
    void login_UserNotFound_Fail() throws Exception {
        // given
        LoginRequest loginRequest = new LoginRequest(
                "nonexistent@example.com",
                "password123"
        );

        // when & then
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("내 정보 조회 성공")
    void getMyInfo_Success() throws Exception {
        // given - 회원가입 및 로그인
        SignUpRequest signUpRequest = new SignUpRequest(
                testEmail,
                "password123",
                "테스트사용자",
                "010-1234-5678"
        );
        
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest(
                testEmail,
                "password123"
        );

        String loginResponse = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        authToken = objectMapper.readTree(loginResponse).get("token").asText();

        // when & then - 내 정보 조회
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(testEmail))
                .andExpect(jsonPath("$.name").value("테스트사용자"))
                .andExpect(jsonPath("$.phone").value("010-1234-5678"));
    }

    @Test
    @DisplayName("인증 없이 내 정보 조회 시 실패")
    void getMyInfo_WithoutAuth_Fail() throws Exception {
        // when & then
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }
}