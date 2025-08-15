package org.zb.ecommerce.domain.payment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zb.ecommerce.domain.payment.dto.PaymentRequest;
import org.zb.ecommerce.domain.payment.dto.PaymentResponse;
import org.zb.ecommerce.domain.payment.service.PaymentService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        log.info("Creating payment for orderId: {}", request.orderId());
        PaymentResponse response = paymentService.createPayment(request);
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
}