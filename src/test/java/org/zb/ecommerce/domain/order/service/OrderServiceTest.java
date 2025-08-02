package org.zb.ecommerce.domain.order.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zb.ecommerce.domain.order.dto.CreateOrderRequest;
import org.zb.ecommerce.domain.order.dto.OrderResponse;
import org.zb.ecommerce.domain.order.entity.Order;
import org.zb.ecommerce.domain.order.entity.OrderItem;
import org.zb.ecommerce.domain.order.entity.OrderStatus;
import org.zb.ecommerce.domain.order.exception.OrderNotFoundException;
import org.zb.ecommerce.domain.order.repository.OrderRepository;
import org.zb.ecommerce.domain.product.entity.Product;
import org.zb.ecommerce.domain.product.repository.ProductRepository;
import org.zb.ecommerce.domain.product.service.ProductService;
import org.zb.ecommerce.global.exception.BusinessException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * OrderService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    
    @InjectMocks
    private OrderService orderService;
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private ProductService productService;
    
    private Product testProduct1;
    private Product testProduct2;
    private Order testOrder;
    
    @BeforeEach
    void setUp() {
        testProduct1 = Product.builder()
                .name("노트북")
                .price(new BigDecimal("1500000"))
                .stockQuantity(10)
                .build();
        
        testProduct2 = Product.builder()
                .name("마우스")
                .price(new BigDecimal("50000"))
                .stockQuantity(20)
                .build();
        
        List<OrderItem> orderItems = Arrays.asList(
                OrderItem.builder()
                        .productId(1L)
                        .quantity(1)
                        .unitPrice(testProduct1.getPrice())
                        .build(),
                OrderItem.builder()
                        .productId(2L)
                        .quantity(2)
                        .unitPrice(testProduct2.getPrice())
                        .build()
        );
        
        testOrder = Order.builder()
                .userId(1L)
                .orderItems(orderItems)
                .build();
    }
    
    @Nested
    @DisplayName("주문 생성")
    class CreateOrder {
        
        @Test
        @DisplayName("정상적인 주문 생성")
        void createOrder_Success() {
            // given
            Long userId = 1L;
            CreateOrderRequest request = CreateOrderRequest.builder()
                    .items(Arrays.asList(
                            CreateOrderRequest.OrderItemRequest.builder()
                                    .productId(1L)
                                    .quantity(1)
                                    .build(),
                            CreateOrderRequest.OrderItemRequest.builder()
                                    .productId(2L)
                                    .quantity(2)
                                    .build()
                    ))
                    .build();
            
            given(productService.getProductEntities(anyList()))
                    .willReturn(Arrays.asList(testProduct1, testProduct2));
            given(orderRepository.save(any(Order.class))).willReturn(testOrder);
            given(productRepository.decreaseStock(1L, 1)).willReturn(1);
            given(productRepository.decreaseStock(2L, 2)).willReturn(1);
            
            // when
            OrderResponse response = orderService.createOrder(userId, request);
            
            // then
            assertThat(response).isNotNull();
            assertThat(response.getUserId()).isEqualTo(userId);
            assertThat(response.getItems()).hasSize(2);
            
            verify(productRepository).decreaseStock(1L, 1);
            verify(productRepository).decreaseStock(2L, 2);
        }
        
        @Test
        @DisplayName("재고 부족으로 주문 실패")
        void createOrder_InsufficientStock_ThrowsException() {
            // given
            Long userId = 1L;
            CreateOrderRequest request = CreateOrderRequest.builder()
                    .items(Arrays.asList(
                            CreateOrderRequest.OrderItemRequest.builder()
                                    .productId(1L)
                                    .quantity(100)  // 재고보다 많은 수량
                                    .build()
                    ))
                    .build();
            
            given(productService.getProductEntities(anyList()))
                    .willReturn(Arrays.asList(testProduct1));
            
            // when & then
            assertThatThrownBy(() -> orderService.createOrder(userId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("재고가 부족합니다");
            
            verify(orderRepository, never()).save(any(Order.class));
        }
    }
    
    @Nested
    @DisplayName("주문 조회")
    class GetOrder {
        
        @Test
        @DisplayName("정상적인 주문 조회")
        void getOrder_Success() {
            // given
            Long orderId = 1L;
            Long userId = 1L;
            
            given(orderRepository.findByIdAndUserId(orderId, userId))
                    .willReturn(Optional.of(testOrder));
            
            // when
            OrderResponse response = orderService.getOrder(orderId, userId);
            
            // then
            assertThat(response).isNotNull();
            assertThat(response.getUserId()).isEqualTo(userId);
        }
        
        @Test
        @DisplayName("존재하지 않는 주문 조회 시 예외 발생")
        void getOrder_NotFound_ThrowsException() {
            // given
            Long orderId = 999L;
            Long userId = 1L;
            
            given(orderRepository.findByIdAndUserId(orderId, userId))
                    .willReturn(Optional.empty());
            
            // when & then
            assertThatThrownBy(() -> orderService.getOrder(orderId, userId))
                    .isInstanceOf(OrderNotFoundException.class);
        }
    }
    
    @Nested
    @DisplayName("주문 취소")
    class CancelOrder {
        
        @Test
        @DisplayName("정상적인 주문 취소")
        void cancelOrder_Success() {
            // given
            Long orderId = 1L;
            Long userId = 1L;
            
            given(orderRepository.findByIdAndUserId(orderId, userId))
                    .willReturn(Optional.of(testOrder));
            given(orderRepository.save(any(Order.class))).willReturn(testOrder);
            
            // when
            OrderResponse response = orderService.cancelOrder(orderId, userId);
            
            // then
            assertThat(response).isNotNull();
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            
            verify(productRepository).increaseStock(1L, 1);
            verify(productRepository).increaseStock(2L, 2);
        }
    }
}
