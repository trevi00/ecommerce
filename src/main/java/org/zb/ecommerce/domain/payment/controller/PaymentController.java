package org.zb.ecommerce.domain.payment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zb.ecommerce.domain.payment.dto.CreatePaymentRequest;
import org.zb.ecommerce.domain.payment.dto.PaymentRequest;
import org.zb.ecommerce.domain.payment.dto.PaymentResponse;
import org.zb.ecommerce.domain.payment.dto.PaymentWithOrderResponse;
import org.zb.ecommerce.domain.payment.entity.PaymentStatus;
import org.zb.ecommerce.domain.payment.service.PaymentService;
import org.zb.ecommerce.global.auth.AuthUser;
import org.zb.ecommerce.global.auth.LoginRequired;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentRequest request) {
        log.info("Creating payment for orderId: {}", request.orderId());
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * 주문 검증을 통한 결제 생성 (쿠폰 할인 적용된 주문 지원)
     */
    @PostMapping("/for-order")
    @LoginRequired
    public ResponseEntity<PaymentWithOrderResponse> createPaymentForOrder(
            @AuthUser Long userId,
            @RequestBody CreatePaymentRequest request) {
        log.info("Creating payment for order with validation. UserId: {}, OrderId: {}", 
                userId, request.getOrderId());
        PaymentWithOrderResponse response = paymentService.createPaymentForOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long id) {
        PaymentResponse response = paymentService.getPayment(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable Long orderId) {
        PaymentResponse response = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        List<PaymentResponse> responses = paymentService.getAllPayments();
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<PaymentResponse> completePayment(@PathVariable Long id) {
        log.info("Completing payment: {}", id);
        PaymentResponse response = paymentService.completePayment(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(@PathVariable Long id) {
        log.info("Cancelling payment: {}", id);
        PaymentResponse response = paymentService.cancelPayment(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/fail")
    public ResponseEntity<PaymentResponse> failPayment(@PathVariable Long id) {
        log.info("Failing payment: {}", id);
        PaymentResponse response = paymentService.failPayment(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 내 결제 내역 조회 (주문 정보 포함)
     */
    @GetMapping("/my")
    @LoginRequired
    public ResponseEntity<List<PaymentWithOrderResponse>> getMyPayments(@AuthUser Long userId) {
        List<PaymentWithOrderResponse> responses = paymentService.getUserPaymentsWithOrders(userId);
        return ResponseEntity.ok(responses);
    }
    
    /**
     * 결제 상태별 조회 (관리자용)
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        List<PaymentResponse> responses = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(responses);
    }
}