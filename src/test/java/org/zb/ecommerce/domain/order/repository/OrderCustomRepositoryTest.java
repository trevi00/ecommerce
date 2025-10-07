package org.zb.ecommerce.domain.order.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.zb.ecommerce.config.TestContainerConfig;
import org.zb.ecommerce.domain.order.entity.Order;
import org.zb.ecommerce.domain.order.entity.OrderItem;
import org.zb.ecommerce.domain.order.entity.OrderStatus;
import org.zb.ecommerce.domain.user.entity.User;
import org.zb.ecommerce.domain.user.entity.UserRole;
import org.zb.ecommerce.domain.user.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Testcontainers
class OrderCustomRepositoryTest extends TestContainerConfig {
    
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private OrderCustomRepositoryImpl orderCustomRepository;
    private User testUser;
    private Order testOrder1;
    private Order testOrder2;
    
    @BeforeEach
    void setUp() throws Exception {
        orderCustomRepository = new OrderCustomRepositoryImpl(namedParameterJdbcTemplate);
        
        // 기존 데이터 정리
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        userRepository.deleteAll();
        
        // 테스트용 사용자 생성 (동적 이메일로 변경)
        String uniqueEmail = "testuser-" + System.currentTimeMillis() + "@example.com";
        testUser = User.builder()
                .email(uniqueEmail)
                .password("password123")
                .name("테스트사용자")
                .phone("010-1234-5678")
                .role(UserRole.GENERAL)
                .build();
        testUser = userRepository.save(testUser);

        // 테스트용 주문 데이터 생성
        testOrder1 = Order.builder()
                .userId(testUser.getId())
                .totalAmount(new BigDecimal("10000"))
                .build();
        testOrder1 = orderRepository.save(testOrder1);

        testOrder2 = Order.builder()
                .userId(testUser.getId())
                .totalAmount(new BigDecimal("20000"))
                .build();
        testOrder2.confirm(); // 확정 상태로 변경
        testOrder2 = orderRepository.save(testOrder2);
    }
    
    @AfterEach
    void tearDown() {
        // 테스트 후 데이터 정리
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        userRepository.deleteAll();
    }
    
    @Test
    @DisplayName("사용자 ID로 동적 조건 검색 성공")
    void findOrdersWithDynamicConditions_ByUserId() {
        // given
        // testUser와 testOrder1, testOrder2가 이미 설정됨 (PENDING, CONFIRMED 상태)
        
        // when
        List<Order> result = orderCustomRepository.findOrdersWithDynamicConditions(
                testUser.getId(), null, null, null, null);
        
        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("orderNumber")
                .containsExactlyInAnyOrder(testOrder1.getOrderNumber(), testOrder2.getOrderNumber());
    }
    
    @Test
    @DisplayName("주문 상태로 동적 조건 검색 성공")
    void findOrdersWithDynamicConditions_ByStatus() {
        // given
        // testOrder1: PENDING, testOrder2: CONFIRMED 상태
        
        // when
        List<Order> result = orderCustomRepository.findOrdersWithDynamicConditions(
                null, null, OrderStatus.PENDING, null, null);
        
        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderNumber()).isEqualTo(testOrder1.getOrderNumber());
    }
    
    @Test
    @DisplayName("사용자 ID와 상태 조합으로 동적 조건 검색 성공")
    void findOrdersWithDynamicConditions_ByUserIdAndStatus() {
        // given
        // testUser의 testOrder1: PENDING, testOrder2: CONFIRMED 상태
        
        // when
        List<Order> result = orderCustomRepository.findOrdersWithDynamicConditions(
                testUser.getId(), null, OrderStatus.PENDING, null, null);
        
        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderNumber()).isEqualTo(testOrder1.getOrderNumber());
    }
    
    @Test
    @DisplayName("기간으로 동적 조건 검색 성공")
    void findOrdersWithDynamicConditions_ByDateRange() {
        // given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusDays(1);
        LocalDateTime endDate = now.plusDays(1);
        
        // when
        List<Order> result = orderCustomRepository.findOrdersWithDynamicConditions(
                testUser.getId(), null, null, startDate, endDate);
        
        // then
        assertThat(result).hasSize(2);
    }
    
    @Test
    @DisplayName("관리자용 검색 - 사용자 ID 없이 검색")
    void findOrdersForAdmin() {
        // given
        // testOrder1: PENDING, testOrder2: CONFIRMED 상태
        
        // when
        List<Order> result = orderCustomRepository.findOrdersForAdmin(
                null, OrderStatus.PENDING, null, null);
        
        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderNumber()).isEqualTo(testOrder1.getOrderNumber());
    }
    
    @Test
    @DisplayName("모든 조건이 null인 경우 전체 주문 조회")
    void findOrdersWithDynamicConditions_AllNull() {
        // given
        // testOrder1, testOrder2가 이미 설정됨
        
        // when
        List<Order> result = orderCustomRepository.findOrdersWithDynamicConditions(
                null, null, null, null, null);
        
        // then
        assertThat(result).hasSize(2);
    }
}