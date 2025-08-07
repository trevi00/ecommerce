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
import static org.junit.jupiter.api.Assertions.*;
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
        @DisplayName("정상적인 주문 생성 - assertAll과 Mockito verify 활용")
        void createOrder_Success() {
            // given
            Long userId = 1L;
            CreateOrderRequest request = new CreateOrderRequest(
                    Arrays.asList(
                            new CreateOrderRequest.OrderItemRequest(1L, 1),
                            new CreateOrderRequest.OrderItemRequest(2L, 2)
                    ),
                    null
            );
            
            given(productService.getProductEntities(anyList()))
                    .willReturn(Arrays.asList(testProduct1, testProduct2));
            given(orderRepository.save(any(Order.class))).willReturn(testOrder);
            given(productRepository.decreaseStock(1L, 1)).willReturn(1);
            given(productRepository.decreaseStock(2L, 2)).willReturn(1);
            
            // when
            OrderResponse response = orderService.createOrder(userId, request);
            
            // then - assertAll로 그룹화된 검증
            assertAll(
                "주문 생성 응답 검증",
                () -> assertNotNull(response, "주문 생성 응답이 null이 아니어야 함"),
                () -> assertEquals(userId, response.getUserId()),
                () -> assertEquals(2, response.getItems().size()),
                () -> assertTrue(response.getItems().size() > 0, "주문 항목이 있어야 함"),
                () -> assertFalse(response.getItems().isEmpty(), "주문 항목 리스트가 비어있으면 안됨")
            );
            
            // Mockito verify 검증 - 상호작용 확인
            verify(productService).getProductEntities(anyList());
            verify(orderRepository).save(any(Order.class));
            verify(productRepository).decreaseStock(1L, 1);
            verify(productRepository).decreaseStock(2L, 2);
            
            // 추가적인 verify - 호출 횟수 및 순서 검증
            verify(productRepository, times(2)).decreaseStock(anyLong(), anyInt());
        }
        
        @Test
        @DisplayName("재고 부족으로 주문 실패")
        void createOrder_InsufficientStock_ThrowsException() {
            // given
            Long userId = 1L;
            CreateOrderRequest request = new CreateOrderRequest(
                    Arrays.asList(
                            new CreateOrderRequest.OrderItemRequest(1L, 100)  // 재고보다 많은 수량
                    ),
                    null
            );
            
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
        @DisplayName("정상적인 주문 조회 - 종합적인 검증")
        void getOrder_Success() {
            // given
            Long orderId = 1L;
            Long userId = 1L;
            
            given(orderRepository.findByIdAndUserId(orderId, userId))
                    .willReturn(Optional.of(testOrder));
            
            // when
            OrderResponse response = orderService.getOrder(orderId, userId);
            
            // then - assertAll과 다양한 assertion
            assertAll(
                "주문 조회 응답 검증",
                () -> assertNotNull(response, "주문 조회 응답이 null이 아니어야 함"),
                () -> assertEquals(userId, response.getUserId()),
                () -> assertTrue(orderId > 0, "유효한 주문 ID여야 함"),
                () -> assertFalse(response.getItems().isEmpty(), "주문 항목이 있어야 함")
            );
            
            // Mockito verify
            verify(orderRepository).findByIdAndUserId(orderId, userId);
            verify(orderRepository, times(1)).findByIdAndUserId(anyLong(), anyLong());
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
