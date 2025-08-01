package org.zb.ecommerce.domain.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zb.ecommerce.domain.order.dto.CreateOrderFromCartRequest;
import org.zb.ecommerce.domain.order.dto.CreateOrderRequest;
import org.zb.ecommerce.domain.order.dto.OrderResponse;
import org.zb.ecommerce.domain.order.entity.OrderStatus;
import org.zb.ecommerce.domain.order.service.OrderService;
import org.zb.ecommerce.global.auth.AuthUser;
import org.zb.ecommerce.global.auth.LoginRequired;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 관련 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@LoginRequired
public class OrderController {
    
    private final OrderService orderService;
    
    /**
     * 주문 생성
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @AuthUser Long userId,
            @Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * 주문 상세 조회
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            @AuthUser Long userId,
            @PathVariable Long orderId) {
        OrderResponse response = orderService.getOrder(orderId, userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 내 주문 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders(@AuthUser Long userId) {
        List<OrderResponse> responses = orderService.getUserOrders(userId);
        return ResponseEntity.ok(responses);
    }
    
    /**
     * 주문 취소
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @AuthUser Long userId,
            @PathVariable Long orderId) {
        OrderResponse response = orderService.cancelOrder(orderId, userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 기간별 주문 조회
     */
    @GetMapping("/period")
    public ResponseEntity<List<OrderResponse>> getOrdersByPeriod(
            @AuthUser Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<OrderResponse> responses = orderService.getOrdersByPeriod(userId, startDate, endDate);
        return ResponseEntity.ok(responses);
    }
    
    /**
     * 상태별 주문 조회
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(
            @AuthUser Long userId,
            @PathVariable OrderStatus status) {
        List<OrderResponse> responses = orderService.getOrdersByStatus(userId, status);
        return ResponseEntity.ok(responses);
    }
    
    /**
     * 장바구니에서 주문 생성
     */
    @PostMapping("/cart")
    public ResponseEntity<OrderResponse> createOrderFromCart(
            @AuthUser Long userId,
            @RequestBody(required = false) CreateOrderFromCartRequest request) {
        if (request == null) {
            request = new CreateOrderFromCartRequest();
        }
        OrderResponse response = orderService.createOrderFromCart(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
