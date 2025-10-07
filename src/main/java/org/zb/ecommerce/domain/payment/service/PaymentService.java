package org.zb.ecommerce.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zb.ecommerce.domain.coupon.service.CouponValidationService;
import org.zb.ecommerce.domain.order.dto.OrderResponse;
import org.zb.ecommerce.domain.order.entity.Order;
import org.zb.ecommerce.domain.order.exception.OrderNotFoundException;
import org.zb.ecommerce.domain.order.repository.OrderRepository;
import org.zb.ecommerce.domain.payment.dto.CreatePaymentRequest;
import org.zb.ecommerce.domain.payment.dto.PaymentRequest;
import org.zb.ecommerce.domain.payment.dto.PaymentResponse;
import org.zb.ecommerce.domain.payment.dto.PaymentWithOrderResponse;
import org.zb.ecommerce.domain.payment.entity.Payment;
import org.zb.ecommerce.domain.payment.entity.PaymentStatus;
import org.zb.ecommerce.domain.payment.exception.InvalidPaymentStatusException;
import org.zb.ecommerce.domain.payment.exception.PaymentAlreadyExistsException;
import org.zb.ecommerce.domain.payment.exception.PaymentNotFoundException;
import org.zb.ecommerce.domain.payment.repository.PaymentRepository;
import org.zb.ecommerce.global.annotation.CustomTransactional;
import org.zb.ecommerce.global.exception.BusinessException;
import org.zb.ecommerce.global.exception.ErrorCode;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final CouponValidationService couponValidationService;

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
    
    /**
     * 주문 정보를 검증하여 결제 생성
     */
    @CustomTransactional
    public PaymentWithOrderResponse createPaymentForOrder(Long userId, CreatePaymentRequest request) {
        // 주문 정보 조회 및 검증
        Order order = orderRepository.findByIdAndUserId(request.getOrderId(), userId)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다."));
        
        // 이미 결제가 존재하는지 확인
        if (paymentRepository.existsByOrderId(request.getOrderId())) {
            throw new PaymentAlreadyExistsException(request.getOrderId());
        }
        
        // 결제 금액 검증 (할인이 적용된 최종 금액과 일치하는지 확인)
        validatePaymentAmount(order, request.getAmount());
        
        // 결제 생성
        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .paymentMethod(request.getPaymentMethod())
                .amount(request.getAmount())
                .build();
        
        Payment savedPayment = paymentRepository.save(payment);
        
        log.info("Payment created for order: {}, amount: {}, discount: {}", 
                order.getOrderNumber(), order.getFinalAmount(), order.getDiscountAmount());
        
        return PaymentWithOrderResponse.from(savedPayment, OrderResponse.from(order));
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

    @CustomTransactional
    public PaymentResponse cancelPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));

        if (!payment.canCancel()) {
            throw new InvalidPaymentStatusException(
                    "결제를 취소할 수 없는 상태입니다. 현재 상태: " + payment.getStatus());
        }

        // 주문의 할인 금액 조회
        BigDecimal discountAmount = orderRepository.findDiscountAmountById(payment.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다."));
        
        // 쿠폰 복구 (할인이 적용된 주문인 경우)
        if (discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            couponValidationService.restoreCoupon(payment.getOrderId());
            
            // 로깅을 위한 주문 번호 조회
            String orderNumber = orderRepository.findOrderNumberById(payment.getOrderId())
                    .orElse("UNKNOWN");
            
            log.info("Coupon restored for cancelled payment. Order: {}, discount: {}", 
                    orderNumber, discountAmount);
        }

        payment.cancel();
        Payment savedPayment = paymentRepository.save(payment);
        
        log.info("Payment cancelled: {}, orderId: {}", payment.getId(), payment.getOrderId());
        
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
    
    /**
     * 사용자별 결제 내역 조회 (주문 정보 포함)
     */
    @Transactional(readOnly = true)
    public List<PaymentWithOrderResponse> getUserPaymentsWithOrders(Long userId) {
        // 사용자의 모든 주문 조회
        List<Order> userOrders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        return userOrders.stream()
                .map(order -> {
                    // 각 주문에 대한 결제 정보 조회
                    return paymentRepository.findByOrderId(order.getId())
                            .map(payment -> PaymentWithOrderResponse.from(payment, OrderResponse.from(order)))
                            .orElse(null);
                })
                .filter(paymentWithOrder -> paymentWithOrder != null)
                .collect(Collectors.toList());
    }
    
    /**
     * 결제 상태별 조회
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status)
                .stream()
                .map(PaymentResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 결제 금액 검증
     */
    private void validatePaymentAmount(Order order, BigDecimal requestAmount) {
        BigDecimal finalAmount = order.getFinalAmount();
        
        if (requestAmount.compareTo(finalAmount) != 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, 
                    String.format("결제 금액이 일치하지 않습니다. 주문 최종 금액: %s, 요청 금액: %s", 
                            finalAmount, requestAmount));
        }
        
        // 할인이 적용된 경우 원래 금액과 할인 금액 로깅
        if (order.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            log.info("Payment validation - Order: {}, Original: {}, Discount: {}, Final: {}", 
                    order.getOrderNumber(), order.getTotalAmount(), 
                    order.getDiscountAmount(), order.getFinalAmount());
        }
    }
}