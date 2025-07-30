package org.zb.ecommerce.domain.order.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Order Entity 단위 테스트
 */
class OrderTest {
    
    @Nested
    @DisplayName("Order 생성")
    class OrderCreation {
        
        @Test
        @DisplayName("정상적인 Order 생성")
        void createOrder_Success() {
            // given
            Long userId = 1L;
            List<OrderItem> orderItems = Arrays.asList(
                    OrderItem.builder()
                            .productId(1L)
                            .quantity(2)
                            .unitPrice(new BigDecimal("10000"))
                            .build(),
                    OrderItem.builder()
                            .productId(2L)
                            .quantity(1)
                            .unitPrice(new BigDecimal("5000"))
                            .build()
            );
            
            // when
            Order order = Order.builder()
                    .userId(userId)
                    .orderItems(orderItems)
                    .build();
            
            // then
            assertThat(order.getUserId()).isEqualTo(userId);
            assertThat(order.getOrderNumber()).isNotNull();
            assertThat(order.getOrderNumber()).startsWith("ORD-");
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("25000"));
            assertThat(order.getDiscountAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(order.getFinalAmount()).isEqualByComparingTo(new BigDecimal("25000"));
            assertThat(order.getOrderItems()).hasSize(2);
        }
        
        @Test
        @DisplayName("주문 항목이 없을 때 예외 발생")
        void createOrder_WithEmptyItems_ThrowsException() {
            // when & then
            assertThatThrownBy(() -> Order.builder()
                    .userId(1L)
                    .orderItems(null)
                    .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("주문 항목이 비어있습니다.");
        }
        
        @Test
        @DisplayName("잘못된 userId로 생성 시 예외 발생")
        void createOrder_WithInvalidUserId_ThrowsException() {
            // given
            List<OrderItem> orderItems = Arrays.asList(
                    OrderItem.builder()
                            .productId(1L)
                            .quantity(1)
                            .unitPrice(new BigDecimal("10000"))
                            .build()
            );
            
            // when & then
            assertThatThrownBy(() -> Order.builder()
                    .userId(null)
                    .orderItems(orderItems)
                    .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("사용자 ID가 올바르지 않습니다.");
        }
    }
    
    @Nested
    @DisplayName("주문 상태 관리")
    class OrderStatusManagement {
        
        @Test
        @DisplayName("주문 확정 성공")
        void confirmOrder_Success() {
            // given
            Order order = createSampleOrder();
            
            // when
            order.confirm();
            
            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        }
        
        @Test
        @DisplayName("이미 확정된 주문 확정 시 예외 발생")
        void confirmOrder_AlreadyConfirmed_ThrowsException() {
            // given
            Order order = createSampleOrder();
            order.confirm();
            
            // when & then
            assertThatThrownBy(() -> order.confirm())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("대기 중인 주문만 확정할 수 있습니다.");
        }
        
        @Test
        @DisplayName("주문 취소 성공")
        void cancelOrder_Success() {
            // given
            Order order = createSampleOrder();
            
            // when
            order.cancel();
            
            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }
        
        @Test
        @DisplayName("이미 취소된 주문 취소 시 예외 발생")
        void cancelOrder_AlreadyCancelled_ThrowsException() {
            // given
            Order order = createSampleOrder();
            order.cancel();
            
            // when & then
            assertThatThrownBy(() -> order.cancel())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("취소할 수 없는 주문 상태입니다.");
        }
    }
    
    @Nested
    @DisplayName("쿠폰 적용")
    class CouponApplication {
        
        @Test
        @DisplayName("쿠폰 적용 성공")
        void applyCoupon_Success() {
            // given
            Order order = createSampleOrder();
            BigDecimal discountAmount = new BigDecimal("5000");
            
            // when
            order.applyCoupon(discountAmount);
            
            // then
            assertThat(order.getDiscountAmount()).isEqualByComparingTo(discountAmount);
            assertThat(order.getFinalAmount()).isEqualByComparingTo(new BigDecimal("5000"));
        }
        
        @Test
        @DisplayName("할인 금액이 총 금액보다 클 때 예외 발생")
        void applyCoupon_DiscountExceedsTotal_ThrowsException() {
            // given
            Order order = createSampleOrder();
            BigDecimal discountAmount = new BigDecimal("15000");
            
            // when & then
            assertThatThrownBy(() -> order.applyCoupon(discountAmount))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("할인 금액이 총 금액보다 클 수 없습니다.");
        }
    }
    
    private Order createSampleOrder() {
        List<OrderItem> orderItems = Arrays.asList(
                OrderItem.builder()
                        .productId(1L)
                        .quantity(1)
                        .unitPrice(new BigDecimal("10000"))
                        .build()
        );
        
        return Order.builder()
                .userId(1L)
                .orderItems(orderItems)
                .build();
    }
}
