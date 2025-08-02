package org.zb.ecommerce.domain.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zb.ecommerce.domain.cart.entity.Cart;
import org.zb.ecommerce.domain.cart.entity.CartItem;
import org.zb.ecommerce.domain.cart.repository.CartRepository;
import org.zb.ecommerce.domain.order.dto.CreateOrderFromCartRequest;
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
import org.zb.ecommerce.global.exception.ErrorCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 주문 비즈니스 로직을 처리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final CartRepository cartRepository;
    
    /**
     * 주문 생성
     */
    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        // 상품 정보 조회
        List<Long> productIds = request.getItems().stream()
                .map(CreateOrderRequest.OrderItemRequest::getProductId)
                .collect(Collectors.toList());
        
        List<Product> products = productService.getProductEntities(productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        
        // 주문 항목 생성 및 재고 확인
        List<OrderItem> orderItems = new ArrayList<>();
        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productMap.get(itemRequest.getProductId());
            
            // 재고 확인
            if (!product.isAvailable(itemRequest.getQuantity())) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK, 
                    "재고가 부족합니다. 상품: " + product.getName());
            }
            
            // 주문 항목 생성
            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();
            
            orderItems.add(orderItem);
        }
        
        // 주문 생성
        Order order = Order.builder()
                .userId(userId)
                .orderItems(orderItems)
                .build();
        
        // 주문 저장
        Order savedOrder = orderRepository.save(order);
        
        // 재고 차감
        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productMap.get(itemRequest.getProductId());
            int updatedRows = productRepository.decreaseStock(product.getId(), itemRequest.getQuantity());
            if (updatedRows == 0) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK,
                    "재고 차감 실패. 상품: " + product.getName());
            }
        }
        
        log.info("Order created: {}, userId: {}", savedOrder.getOrderNumber(), userId);
        
        return OrderResponse.from(savedOrder);
    }
    
    /**
     * 주문 상세 조회
     */
    public OrderResponse getOrder(Long orderId, Long userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        return OrderResponse.from(order);
    }
    
    /**
     * 사용자의 주문 목록 조회
     */
    public List<OrderResponse> getUserOrders(Long userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        return orders.stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 주문 취소
     */
    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        // 주문 취소
        order.cancel();
        
        // 재고 복구
        for (OrderItem item : order.getOrderItems()) {
            productRepository.increaseStock(item.getProductId(), item.getQuantity());
        }
        
        Order cancelledOrder = orderRepository.save(order);
        log.info("Order cancelled: {}, userId: {}", order.getOrderNumber(), userId);
        
        return OrderResponse.from(cancelledOrder);
    }
    
    /**
     * 주문 확정
     */
    @Transactional
    public OrderResponse confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        order.confirm();
        Order confirmedOrder = orderRepository.save(order);
        
        log.info("Order confirmed: {}", order.getOrderNumber());
        
        return OrderResponse.from(confirmedOrder);
    }
    
    /**
     * 기간별 주문 조회
     */
    public List<OrderResponse> getOrdersByPeriod(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepository.findByUserIdAndPeriod(userId, startDate, endDate);
        
        return orders.stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 상태별 주문 조회
     */
    public List<OrderResponse> getOrdersByStatus(Long userId, OrderStatus status) {
        List<Order> orders = orderRepository.findByUserIdAndStatus(userId, status);
        
        return orders.stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 장바구니에서 주문 생성
     */
    @Transactional
    public OrderResponse createOrderFromCart(Long userId, CreateOrderFromCartRequest request) {
        // 장바구니 조회
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND, "장바구니가 비어있습니다."));
        
        if (cart.getCartItems().isEmpty()) {
            throw new BusinessException(ErrorCode.CART_EMPTY, "장바구니가 비어있습니다.");
        }
        
        // 장바구니 아이템을 주문 요청으로 변환
        List<CreateOrderRequest.OrderItemRequest> orderItems = cart.getCartItems().stream()
                .map(cartItem -> CreateOrderRequest.OrderItemRequest.builder()
                        .productId(cartItem.getProductId())
                        .quantity(cartItem.getQuantity())
                        .build())
                .collect(Collectors.toList());
        
        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                .items(orderItems)
                .couponId(request.getCouponId())
                .build();
        
        // 주문 생성
        OrderResponse orderResponse = createOrder(userId, createOrderRequest);
        
        // 장바구니 비우기
        cart.clear();
        cartRepository.save(cart);
        
        log.info("Order created from cart. userId: {}, orderId: {}", userId, orderResponse.getId());
        
        return orderResponse;
    }
}
