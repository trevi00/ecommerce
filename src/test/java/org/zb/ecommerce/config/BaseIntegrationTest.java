package org.zb.ecommerce.config;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

/**
 * 통합 테스트를 위한 베이스 클래스
 * TestContainers와 Redis를 사용한 실제 데이터베이스 연동 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional
public abstract class BaseIntegrationTest extends TestContainerConfig {

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @BeforeEach
    void baseSetUp() {
        // 테스트 간 데이터 독립성 보장을 위한 정리
        cleanDatabase();
    }

    /**
     * 데이터베이스 정리 (외래키 순서 고려)
     */
    protected void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM cart_items");
        jdbcTemplate.update("DELETE FROM carts");
        jdbcTemplate.update("DELETE FROM user_coupons");
        jdbcTemplate.update("DELETE FROM order_items");
        jdbcTemplate.update("DELETE FROM payments");
        jdbcTemplate.update("DELETE FROM orders");
        jdbcTemplate.update("DELETE FROM coupons");
        jdbcTemplate.update("DELETE FROM products");
        jdbcTemplate.update("DELETE FROM users");
    }

    /**
     * 테스트용 사용자 생성
     */
    protected Long createTestUser(String email, String name) {
        jdbcTemplate.update(
            "INSERT INTO users (email, password, name, phone, role, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())",
            email, "password123", name, "010-1234-5678", "GENERAL"
        );
        return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }
    
    protected Long createTestUser() {
        String uniqueEmail = "test" + System.currentTimeMillis() + "@example.com";
        String uniqueName = "테스트사용자" + System.currentTimeMillis();
        return createTestUser(uniqueEmail, uniqueName);
    }

    /**
     * 테스트용 상품 생성
     */
    protected Long createTestProduct(String name, BigDecimal price, int stock) {
        jdbcTemplate.update(
            "INSERT INTO products (name, description, price, stock_quantity, created_at, updated_at) VALUES (?, ?, ?, ?, NOW(), NOW())",
            name, "테스트 상품 설명", price, stock
        );
        return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    /**
     * 테스트용 주문 생성
     */
    protected Long createTestOrder(Long userId, BigDecimal amount) {
        String orderNumber = "ORDER-" + System.currentTimeMillis();
        jdbcTemplate.update(
            "INSERT INTO orders (user_id, order_number, total_amount, discount_amount, final_amount, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())",
            userId, orderNumber, amount, BigDecimal.ZERO, amount, "PENDING"
        );
        return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    /**
     * 테스트용 주문 항목 생성
     */
    protected Long createTestOrderItem(Long orderId, Long productId, int quantity, BigDecimal unitPrice) {
        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        jdbcTemplate.update(
            "INSERT INTO order_items (order_id, product_id, quantity, unit_price, total_price, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())",
            orderId, productId, quantity, unitPrice, totalPrice
        );
        return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    /**
     * 완전한 테스트 시나리오 데이터 생성 (User + Product + Order + OrderItem)
     */
    protected TestDataSet createCompleteTestData() {
        Long userId = createTestUser();
        Long productId = createTestProduct("테스트 상품", new BigDecimal("10000.00"), 100);
        Long orderId = createTestOrder(userId, new BigDecimal("10000.00"));
        Long orderItemId = createTestOrderItem(orderId, productId, 1, new BigDecimal("10000.00"));
        
        return new TestDataSet(userId, productId, orderId, orderItemId);
    }

    /**
     * 테스트 데이터 세트를 담는 클래스
     */
    public static class TestDataSet {
        public final Long userId;
        public final Long productId;
        public final Long orderId;
        public final Long orderItemId;

        public TestDataSet(Long userId, Long productId, Long orderId, Long orderItemId) {
            this.userId = userId;
            this.productId = productId;
            this.orderId = orderId;
            this.orderItemId = orderItemId;
        }
    }
}