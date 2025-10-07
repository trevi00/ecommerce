package org.zb.ecommerce.e2e;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.zb.ecommerce.annotation.IntegrationTest;
import org.zb.ecommerce.domain.cart.entity.CartItem;
import org.zb.ecommerce.domain.order.dto.OrderItemRequest;
import org.zb.ecommerce.domain.order.dto.OrderRequest;
import org.zb.ecommerce.domain.payment.dto.PaymentRequest;
import org.zb.ecommerce.domain.payment.entity.PaymentMethod;
import org.zb.ecommerce.domain.product.entity.Product;
import org.zb.ecommerce.domain.user.dto.SignupRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * E2E 통합 테스트
 * 실제 사용자 시나리오를 따라 전체 플로우를 테스트합니다.
 */
@IntegrationTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EcommerceE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private FixtureMonkey fixtureMonkey;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        RestAssured.port = port;

        // 테스트 데이터 초기화
        cleanUpDatabase();
        insertTestProducts();
    }

    @Test
    @DisplayName("E2E: 회원가입 → 로그인 → 상품조회 → 장바구니 → 주문 → 결제 전체 플로우 테스트")
    void testFullEcommercePurchaseFlow() {
        // 1. 회원가입
        SignupRequest signupRequest = fixtureMonkey.giveMeBuilder(SignupRequest.class)
                .set("email", "e2e.test@example.com")
                .set("password", "Test1234!")
                .set("name", "E2E Test User")
                .set("phone", "010-1234-5678")
                .sample();

        String userId = given()
                .contentType(ContentType.JSON)
                .body(signupRequest)
                .when()
                .post(baseUrl + "/api/users/signup")
                .then()
                .statusCode(201)
                .body("email", equalTo(signupRequest.email()))
                .extract()
                .path("id")
                .toString();

        // 2. 로그인
        Map<String, String> loginRequest = Map.of(
                "email", signupRequest.email(),
                "password", signupRequest.password()
        );

        String token = given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post(baseUrl + "/api/users/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .extract()
                .path("token");

        // 3. 상품 목록 조회
        List<Map<String, Object>> products = given()
                .when()
                .get(baseUrl + "/api/products")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .extract()
                .jsonPath()
                .getList("$");

        Long firstProductId = ((Number) products.get(0).get("id")).longValue();

        // 4. 상품 상세 조회
        given()
                .when()
                .get(baseUrl + "/api/products/" + firstProductId)
                .then()
                .statusCode(200)
                .body("id", equalTo(firstProductId.intValue()))
                .body("name", notNullValue())
                .body("price", notNullValue())
                .body("stockQuantity", greaterThan(0));

        // 5. 장바구니에 상품 추가
        Map<String, Object> addToCartRequest = Map.of(
                "productId", firstProductId,
                "quantity", 2
        );

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(addToCartRequest)
                .when()
                .post(baseUrl + "/api/carts/items")
                .then()
                .statusCode(200)
                .body("userId", notNullValue())
                .body("items.size()", equalTo(1))
                .body("items[0].productId", equalTo(firstProductId.intValue()))
                .body("items[0].quantity", equalTo(2));

        // 6. 장바구니 조회
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get(baseUrl + "/api/carts")
                .then()
                .statusCode(200)
                .body("items.size()", equalTo(1))
                .body("items[0].productId", equalTo(firstProductId.intValue()));

        // 7. 주문 생성
        OrderRequest orderRequest = new OrderRequest(
                List.of(new OrderItemRequest(firstProductId, 2))
        );

        Long orderId = given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(orderRequest)
                .when()
                .post(baseUrl + "/api/orders")
                .then()
                .statusCode(201)
                .body("orderNumber", notNullValue())
                .body("status", equalTo("PENDING"))
                .body("items.size()", equalTo(1))
                .extract()
                .path("id");

        // 8. 주문 조회
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get(baseUrl + "/api/orders/" + orderId)
                .then()
                .statusCode(200)
                .body("id", equalTo(orderId))
                .body("status", equalTo("PENDING"));

        // 9. 결제 진행
        PaymentRequest paymentRequest = new PaymentRequest(
                Long.valueOf(orderId.toString()),
                PaymentMethod.CREDIT_CARD,
                new BigDecimal("100000.00")
        );

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(paymentRequest)
                .when()
                .post(baseUrl + "/api/payments")
                .then()
                .statusCode(201)
                .body("orderId", equalTo(orderId))
                .body("paymentMethod", equalTo("CREDIT_CARD"))
                .body("paymentStatus", equalTo("COMPLETED"));

        // 10. 주문 상태 확인 (결제 완료 후)
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get(baseUrl + "/api/orders/" + orderId)
                .then()
                .statusCode(200)
                .body("status", equalTo("CONFIRMED"));

        // 11. 주문 내역 조회
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get(baseUrl + "/api/orders")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1))
                .body("[0].id", equalTo(orderId));
    }

    @Test
    @DisplayName("E2E: 쿠폰 적용 주문 플로우 테스트")
    void testCouponPurchaseFlow() {
        // 1. 회원가입 및 로그인
        SignupRequest signupRequest = fixtureMonkey.giveMeBuilder(SignupRequest.class)
                .set("email", "coupon.test@example.com")
                .set("password", "Test1234!")
                .set("name", "Coupon Test User")
                .set("phone", "010-9876-5432")
                .sample();

        given()
                .contentType(ContentType.JSON)
                .body(signupRequest)
                .when()
                .post(baseUrl + "/api/users/signup")
                .then()
                .statusCode(201);

        String token = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", signupRequest.email(),
                        "password", signupRequest.password()
                ))
                .when()
                .post(baseUrl + "/api/users/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");

        // 2. 사용 가능한 쿠폰 조회
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get(baseUrl + "/api/coupons")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));

        // 3. 주문 생성 (쿠폰 없이)
        List<Map<String, Object>> products = given()
                .get(baseUrl + "/api/products")
                .then()
                .extract()
                .jsonPath()
                .getList("$");

        Long productId = ((Number) products.get(0).get("id")).longValue();

        OrderRequest orderRequest = new OrderRequest(
                List.of(new OrderItemRequest(productId, 1))
        );

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(orderRequest)
                .when()
                .post(baseUrl + "/api/orders")
                .then()
                .statusCode(201)
                .body("discountAmount", equalTo(0.0f));
    }

    @Test
    @DisplayName("E2E: 재고 부족 시 주문 실패 시나리오")
    void testInsufficientStockScenario() {
        // 회원가입 및 로그인
        SignupRequest signupRequest = fixtureMonkey.giveMeBuilder(SignupRequest.class)
                .set("email", "stock.test@example.com")
                .set("password", "Test1234!")
                .set("name", "Stock Test User")
                .set("phone", "010-5555-5555")
                .sample();

        given()
                .contentType(ContentType.JSON)
                .body(signupRequest)
                .post(baseUrl + "/api/users/signup")
                .then()
                .statusCode(201);

        String token = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", signupRequest.email(),
                        "password", signupRequest.password()
                ))
                .post(baseUrl + "/api/users/login")
                .then()
                .extract()
                .path("token");

        // 재고보다 많은 수량 주문 시도
        List<Map<String, Object>> products = given()
                .get(baseUrl + "/api/products")
                .then()
                .extract()
                .jsonPath()
                .getList("$");

        Long productId = ((Number) products.get(0).get("id")).longValue();
        Integer stockQuantity = (Integer) products.get(0).get("stockQuantity");

        OrderRequest orderRequest = new OrderRequest(
                List.of(new OrderItemRequest(productId, stockQuantity + 100))
        );

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(orderRequest)
                .when()
                .post(baseUrl + "/api/orders")
                .then()
                .statusCode(400);
    }

    private void cleanUpDatabase() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.execute("TRUNCATE TABLE payments");
        jdbcTemplate.execute("TRUNCATE TABLE order_items");
        jdbcTemplate.execute("TRUNCATE TABLE orders");
        jdbcTemplate.execute("TRUNCATE TABLE cart_items");
        jdbcTemplate.execute("TRUNCATE TABLE cart");
        jdbcTemplate.execute("TRUNCATE TABLE user_coupons");
        jdbcTemplate.execute("TRUNCATE TABLE coupons");
        jdbcTemplate.execute("TRUNCATE TABLE products");
        jdbcTemplate.execute("TRUNCATE TABLE users");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    private void insertTestProducts() {
        jdbcTemplate.update(
                "INSERT INTO products (id, name, description, price, category, stock_quantity) VALUES (?, ?, ?, ?, ?, ?)",
                1L, "테스트 노트북", "고성능 테스트 노트북", new BigDecimal("1500000.00"), "전자제품", 10
        );
        jdbcTemplate.update(
                "INSERT INTO products (id, name, description, price, category, stock_quantity) VALUES (?, ?, ?, ?, ?, ?)",
                2L, "테스트 마우스", "무선 테스트 마우스", new BigDecimal("50000.00"), "전자제품", 50
        );
        jdbcTemplate.update(
                "INSERT INTO products (id, name, description, price, category, stock_quantity) VALUES (?, ?, ?, ?, ?, ?)",
                3L, "테스트 키보드", "기계식 테스트 키보드", new BigDecimal("150000.00"), "전자제품", 30
        );
    }
}
