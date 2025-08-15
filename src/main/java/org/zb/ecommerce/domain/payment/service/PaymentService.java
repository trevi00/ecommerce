package org.zb.ecommerce.domain.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zb.ecommerce.domain.payment.dto.PaymentRequest;
import org.zb.ecommerce.domain.payment.dto.PaymentResponse;
import org.zb.ecommerce.domain.payment.entity.Payment;
import org.zb.ecommerce.domain.payment.entity.PaymentStatus;
import org.zb.ecommerce.domain.payment.exception.InvalidPaymentStatusException;
import org.zb.ecommerce.domain.payment.exception.PaymentAlreadyExistsException;
import org.zb.ecommerce.domain.payment.exception.PaymentNotFoundException;
import org.zb.ecommerce.domain.payment.repository.PaymentRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        if (paymentRepository.existsByOrderId(request.orderId())) {
            throw new PaymentAlreadyExistsException(request.orderId());
        }

        Payment payment = Payment.builder()
                .orderId(request.orderId())
                .paymentMethod(request.paymentMethod())
                .amount(request.amount())
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        return PaymentResponse.from(savedPayment);
    }

    public PaymentResponse getPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
        return PaymentResponse.from(payment);
    }

    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException(orderId));
        return PaymentResponse.from(payment);
    }

    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(PaymentResponse::from)
                .toList();
    }

    @Transactional
    public PaymentResponse completePayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new InvalidPaymentStatusException(
                    "결제를 완료할 수 없는 상태입니다. 현재 상태: " + payment.getStatus());
        }

        payment.complete();
        Payment savedPayment = paymentRepository.save(payment);
        return PaymentResponse.from(savedPayment);
    }

    @Transactional
    public PaymentResponse cancelPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));

        if (!payment.canCancel()) {
            throw new InvalidPaymentStatusException(
                    "결제를 취소할 수 없는 상태입니다. 현재 상태: " + payment.getStatus());
        }

        payment.cancel();
        Payment savedPayment = paymentRepository.save(payment);
        return PaymentResponse.from(savedPayment);
    }

    @Transactional
    public PaymentResponse failPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));

        payment.fail();
        Payment savedPayment = paymentRepository.save(payment);
        return PaymentResponse.from(savedPayment);
    }
}