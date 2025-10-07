package org.zb.ecommerce.domain.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zb.ecommerce.domain.cart.entity.Cart;
import org.zb.ecommerce.domain.cart.entity.CartItem;
import org.zb.ecommerce.domain.cart.repository.CartRepository;
import org.zb.ecommerce.domain.cart.repository.CartItemRepository;
import org.zb.ecommerce.domain.order.dto.CreateOrderFromCartRequest;
import org.zb.ecommerce.domain.order.dto.CreateOrderFromCartWithCouponRequest;
import org.zb.ecommerce.domain.order.dto.CreateOrderRequest;
import org.zb.ecommerce.domain.order.dto.CreateOrderWithCouponRequest;
import org.zb.ecommerce.domain.order.dto.OrderResponse;
import org.zb.ecommerce.domain.order.entity.Order;
import org.zb.ecommerce.domain.order.entity.OrderItem;
import org.zb.ecommerce.domain.order.entity.OrderStatus;
import org.zb.ecommerce.domain.order.exception.OrderNotFoundException;
import org.zb.ecommerce.domain.order.repository.OrderRepository;
import org.zb.ecommerce.domain.order.repository.OrderItemRepository;
import org.zb.ecommerce.domain.product.entity.Product;
import org.zb.ecommerce.domain.product.repository.ProductRepository;
import org.zb.ecommerce.domain.product.service.ProductService;
import org.zb.ecommerce.domain.coupon.service.CouponValidationService;
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
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CouponValidationService couponValidationService;
    
    /**
     * 주문 생성
     */
    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        // 상품 정보 조회
        List<Long> productIds = request.items().stream()
                .map(CreateOrderRequest.OrderItemRequest::productId)
                .collect(Collectors.toList());
        
        List<Product> products = productService.getProductEntities(productIds);
        Map<Long, Product> productMap = products.stream()
                .filter(p -> p != null && p.getId() != null)
                .collect(Collectors.toMap(Product::getId, p -> p));
        
        // 주문 항목 생성 및 재고 확인
        List<OrderItem> orderItems = new ArrayList<>();
        for (CreateOrderRequest.OrderItemRequest itemRequest : request.items()) {
            Product product = productMap.get(itemRequest.productId());
            if (product == null) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, 
                    "상품을 찾을 수 없습니다. ID: " + itemRequest.productId());
            }
            
            // 재고 확인
            if (!product.isAvailable(itemRequest.quantity())) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK, 
                    "재고가 부족합니다. 상품: " + product.getName());
            }
            
            // 주문 항목 생성
            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .quantity(itemRequest.quantity())
                    .unitPrice(product.getPrice())
                    .build();
            
            orderItems.add(orderItem);
        }
        
        // 총 금액 계산
        BigDecimal totalAmount = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 주문 생성
        Order order = Order.builder()
                .userId(userId)
                .totalAmount(totalAmount)
                .build();
        
        // 주문 저장
        Order savedOrder = orderRepository.save(order);
        
        // 주문 항목들에 orderId 설정 후 저장
        List<OrderItem> savedOrderItems = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            OrderItem itemWithOrderId = OrderItem.builder()
                    .orderId(savedOrder.getId())
                    .productId(orderItem.getProductId())
                    .quantity(orderItem.getQuantity())
                    .unitPrice(orderItem.getUnitPrice())
                    .build();
            savedOrderItems.add(orderItemRepository.save(itemWithOrderId));
        }
        
        // 재고 차감
        for (CreateOrderRequest.OrderItemRequest itemRequest : request.items()) {
            Product product = productMap.get(itemRequest.productId());
            if (product == null) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, 
                    "상품을 찾을 수 없습니다. ID: " + itemRequest.productId());
            }
            int updatedRows = productRepository.decreaseStock(product.getId(), itemRequest.quantity());
            if (updatedRows == 0) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK,
                    "재고 차감 실패. 상품: " + product.getName());
            }
        }
        
        log.info("Order created: {}, userId: {}", savedOrder.getOrderNumber(), userId);
        
        return OrderResponse.from(savedOrder, savedOrderItems);
    }
    
    /**
     * 쿠폰을 사용한 주문 생성
     */
    @Transactional
    public OrderResponse createOrderWithCoupon(Long userId, CreateOrderWithCouponRequest request) {
        // 상품 정보 조회
        List<Long> productIds = request.getItems().stream()
                .map(CreateOrderWithCouponRequest.OrderItemRequest::productId)
                .collect(Collectors.toList());
        
        List<Product> products = productService.getProductEntities(productIds);
        Map<Long, Product> productMap = products.stream()
                .filter(p -> p != null && p.getId() != null)
                .collect(Collectors.toMap(Product::getId, p -> p));
        
        // 주문 항목 생성 및 재고 확인
        List<OrderItem> orderItems = new ArrayList<>();
        for (CreateOrderWithCouponRequest.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productMap.get(itemRequest.productId());
            if (product == null) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, 
                    "상품을 찾을 수 없습니다. ID: " + itemRequest.productId());
            }
            
            // 재고 확인
            if (!product.isAvailable(itemRequest.quantity())) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK, 
                    "재고가 부족합니다. 상품: " + product.getName());
            }
            
            // 주문 항목 생성
            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .quantity(itemRequest.quantity())
                    .unitPrice(product.getPrice())
                    .build();
            
            orderItems.add(orderItem);
        }
        
        // 총 금액 계산
        BigDecimal totalAmount = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 주문 생성 (할인 적용 전)
        Order order = Order.builder()
                .userId(userId)
                .totalAmount(totalAmount)
                .build();
        
        // 쿠폰 할인 계산 및 적용
        BigDecimal discountAmount = couponValidationService.validateAndCalculateDiscount(
                userId, request.getUserCouponId(), order.getTotalAmount());
        
        if (discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            order.applyCoupon(discountAmount);
        }
        
        // 주문 저장
        Order savedOrder = orderRepository.save(order);
        
        // 주문 항목들에 orderId 설정 후 저장
        for (OrderItem orderItem : orderItems) {
            OrderItem itemWithOrderId = OrderItem.builder()
                    .orderId(savedOrder.getId())
                    .productId(orderItem.getProductId())
                    .quantity(orderItem.getQuantity())
                    .unitPrice(orderItem.getUnitPrice())
                    .build();
            orderItemRepository.save(itemWithOrderId);
        }
        
        // 재고 차감
        for (CreateOrderWithCouponRequest.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productMap.get(itemRequest.productId());
            if (product == null) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, 
                    "상품을 찾을 수 없습니다. ID: " + itemRequest.productId());
            }
            int updatedRows = productRepository.decreaseStock(product.getId(), itemRequest.quantity());
            if (updatedRows == 0) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK,
                    "재고 차감 실패. 상품: " + product.getName());
            }
        }
        
        // 쿠폰 사용 처리
        couponValidationService.useCoupon(userId, request.getUserCouponId(), savedOrder.getId());
        
        log.info("Order with coupon created: {}, userId: {}, discount: {}", 
                savedOrder.getOrderNumber(), userId, discountAmount);
        
        return OrderResponse.from(savedOrder);
    }
    
    /**
     * 주문 상세 조회
     */
    public OrderResponse getOrder(Long orderId, Long userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        
        return OrderResponse.from(order, orderItems);
    }
    
    /**
     * 장바구니에서 쿠폰을 사용한 주문 생성
     */
    @Transactional
    public OrderResponse createOrderFromCartWithCoupon(Long userId, CreateOrderFromCartWithCouponRequest request) {
        // 장바구니 조회
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND, "장바구니가 비어있습니다."));
        
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new BusinessException(ErrorCode.CART_EMPTY, "장바구니가 비어있습니다.");
        }
        
        // 장바구니 아이템을 주문 요청으로 변환
        List<CreateOrderWithCouponRequest.OrderItemRequest> orderItems = cartItems.stream()
                .map(cartItem -> new CreateOrderWithCouponRequest.OrderItemRequest(
                        cartItem.getProductId(), cartItem.getQuantity()))
                .collect(Collectors.toList());
        
        // 쿠폰을 사용한 주문 요청 생성
        CreateOrderWithCouponRequest orderRequest = new CreateOrderWithCouponRequest(orderItems, request.getUserCouponId());
        
        // 주문 생성
        OrderResponse orderResponse = createOrderWithCoupon(userId, orderRequest);
        
        // 장바구니 비우기
        cartItemRepository.deleteByCartId(cart.getId());
        
        log.info("Order from cart with coupon created: {}, userId: {}", 
                orderResponse.getOrderNumber(), userId);
        
        return orderResponse;
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
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        for (OrderItem item : orderItems) {
            productRepository.increaseStock(item.getProductId(), item.getQuantity());
        }
        
        // 쿠폰 복구 (할인이 적용된 주문인 경우)
        if (order.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            couponValidationService.restoreCoupon(order.getId());
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
        List<Order> orders = orderRepository.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, startDate, endDate);
        
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
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND, "장바구니가 비어있습니다."));
        
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new BusinessException(ErrorCode.CART_EMPTY, "장바구니가 비어있습니다.");
        }
        
        // 장바구니 아이템을 주문 요청으로 변환
        List<CreateOrderRequest.OrderItemRequest> orderItems = cartItems.stream()
                .map(cartItem -> new CreateOrderRequest.OrderItemRequest(
                        cartItem.getProductId(),
                        cartItem.getQuantity()
                ))
                .collect(Collectors.toList());
        
        CreateOrderRequest createOrderRequest = new CreateOrderRequest(
                orderItems,
                request.couponId()
        );
        
        // 주문 생성
        OrderResponse orderResponse = createOrder(userId, createOrderRequest);
        
        // 장바구니 비우기
        cartItemRepository.deleteByCartId(cart.getId());
        
        log.info("Order created from cart. userId: {}, orderId: {}", userId, orderResponse.getId());
        
        return orderResponse;
    }
}
