package org.zb.ecommerce.domain.order.controller;

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
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.zb.ecommerce.config.TestContainerConfig;
import org.zb.ecommerce.domain.order.dto.CreateOrderRequest;
import org.zb.ecommerce.domain.user.dto.LoginRequest;
import org.zb.ecommerce.domain.user.dto.SignUpRequest;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * OrderController 최적화된 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Testcontainers
class OrderControllerTest extends TestContainerConfig {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private String authToken;
    private String testEmail;
    
    @BeforeEach
    void setUp() throws Exception {
        // 각 테스트마다 고유한 이메일 사용
        testEmail = "order-test-" + System.currentTimeMillis() + "@example.com";
        
        // 최적화된 사용자 생성 및 로그인
        SignUpRequest signUpRequest = new SignUpRequest(
                testEmail,
                "password123",
                "주문테스트",
                "010-1111-2222"
        );
        
        // 회원가입
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isCreated());
        
        // 로그인
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
    }
    
    @Test
    @DisplayName("주문 생성 성공")
    void createOrder_Success() throws Exception {
        // given
        CreateOrderRequest request = new CreateOrderRequest(
                Arrays.asList(
                        new CreateOrderRequest.OrderItemRequest(1L, 1),
                        new CreateOrderRequest.OrderItemRequest(2L, 2)
                ),
                null
        );
        
        // when & then
        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").exists())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
    
    @Test
    @DisplayName("인증 없이 주문 생성 시 실패")
    void createOrder_WithoutAuth_Fail() throws Exception {
        // given
        CreateOrderRequest request = new CreateOrderRequest(
                Arrays.asList(
                        new CreateOrderRequest.OrderItemRequest(1L, 1)
                ),
                null
        );
        
        // when & then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("주문 목록 조회 성공")
    void getMyOrders_Success() throws Exception {
        // given - 주문 생성
        CreateOrderRequest request = new CreateOrderRequest(
                Arrays.asList(
                        new CreateOrderRequest.OrderItemRequest(1L, 1)
                ),
                null
        );
        
        mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        
        // when & then
        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
    
    @Test
    @DisplayName("주문 취소 성공")
    void cancelOrder_Success() throws Exception {
        // given - 주문 생성
        CreateOrderRequest request = new CreateOrderRequest(
                Arrays.asList(
                        new CreateOrderRequest.OrderItemRequest(1L, 1)
                ),
                null
        );
        
        String createResponse = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        Long orderId = objectMapper.readTree(createResponse).get("id").asLong();
        
        // when & then
        mockMvc.perform(post("/api/orders/{orderId}/cancel", orderId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}