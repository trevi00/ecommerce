package org.zb.ecommerce.domain.payment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zb.ecommerce.domain.payment.dto.PaymentRequest;
import org.zb.ecommerce.domain.payment.dto.PaymentResponse;
import org.zb.ecommerce.domain.payment.entity.Payment;
import org.zb.ecommerce.domain.payment.entity.PaymentStatus;
import org.zb.ecommerce.domain.payment.exception.InvalidPaymentStatusException;
import org.zb.ecommerce.domain.payment.exception.PaymentAlreadyExistsException;
import org.zb.ecommerce.domain.payment.exception.PaymentNotFoundException;
import org.zb.ecommerce.domain.payment.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * 주니어 개발자가 작성한 PaymentService 테스트
 * 피드백 요구사항을 반영한 테스트 (assertAll, record 타입, Mockito verify 등)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService 테스트")
class PaymentServiceTest {
    
    @Mock
    private PaymentRepository paymentRepository;
    
    @InjectMocks
    private PaymentService paymentService;
    
    private Payment mockPayment;
    private PaymentRequest paymentRequest;
    
    @BeforeEach
    void setUp() {
        // 테스트용 결제 객체 생성
        mockPayment = createMockPayment();
        
        // 테스트용 결제 요청 DTO (record 타입)
        paymentRequest = new PaymentRequest(1L, "CREDIT_CARD", new BigDecimal("10000"));
    }
    
    @Test
    @DisplayName("결제 생성 성공")
    void createPayment_Success() {
        // given
        given(paymentRepository.existsByOrderId(1L)).willReturn(false);
        given(paymentRepository.save(any(Payment.class))).willReturn(mockPayment);
        
        // when
        PaymentResponse response = paymentService.createPayment(paymentRequest);
        
        // then - assertAll을 활용한 그룹화된 검증
        assertAll("결제 생성 성공 검증",
            () -> assertNotNull(response, "응답은 null이 아니어야 한다"),
            () -> assertEquals(1L, response.orderId(), "주문 ID가 일치해야 한다"),
            () -> assertEquals("CREDIT_CARD", response.paymentMethod(), "결제 수단이 일치해야 한다"),
            () -> assertEquals(new BigDecimal("10000"), response.amount(), "결제 금액이 일치해야 한다"),
            () -> assertEquals(PaymentStatus.PENDING, response.status(), "결제 상태는 PENDING이어야 한다"),
            () -> assertFalse(response.isCompleted(), "새로 생성된 결제는 완료 상태가 아니어야 한다"),
            () -> assertTrue(response.canCancel(), "새로 생성된 결제는 취소 가능해야 한다")
        );
        
        // Mockito verify로 메서드 호출 검증
        assertAll("메서드 호출 검증",
            () -> verify(paymentRepository).existsByOrderId(1L),
            () -> verify(paymentRepository).save(any(Payment.class))
        );
    }
    
    @Test
    @DisplayName("이미 존재하는 결제로 인한 생성 실패")
    void createPayment_PaymentAlreadyExists() {
        // given
        given(paymentRepository.existsByOrderId(1L)).willReturn(true);
        
        // when & then
        assertThatThrownBy(() -> paymentService.createPayment(paymentRequest))
                .isInstanceOf(PaymentAlreadyExistsException.class)
                .hasMessageContaining("이미 해당 주문에 대한 결제가 존재합니다");
        
        // 메서드 호출 검증
        verify(paymentRepository).existsByOrderId(1L);
    }
    
    @Test
    @DisplayName("결제 완료 성공")
    void completePayment_Success() {
        // given
        Payment pendingPayment = createPendingPayment();
        Payment completedPayment = createCompletedPayment();
        
        given(paymentRepository.findById(1L)).willReturn(Optional.of(pendingPayment));
        given(paymentRepository.save(any(Payment.class))).willReturn(completedPayment);
        
        // when
        PaymentResponse response = paymentService.completePayment(1L);
        
        // then - 다양한 Assertion 메서드 활용
        assertAll("결제 완료 성공 검증",
            () -> assertNotNull(response, "응답이 있어야 한다"),
            () -> assertEquals(PaymentStatus.COMPLETED, response.status(), "결제 상태가 COMPLETED여야 한다"),
            () -> assertTrue(response.isCompleted(), "완료된 결제여야 한다"),
            () -> assertFalse(response.canCancel(), "완료된 결제는 취소할 수 없어야 한다"),
            () -> assertNotNull(response.createdAt(), "생성 일시가 설정되어야 한다")
        );
        
        assertAll("메서드 호출 검증",
            () -> verify(paymentRepository).findById(1L),
            () -> verify(paymentRepository).save(any(Payment.class))
        );
    }
    
    @Test
    @DisplayName("존재하지 않는 결제 완료 실패")
    void completePayment_NotFound() {
        // given
        given(paymentRepository.findById(1L)).willReturn(Optional.empty());
        
        // when & then
        assertThatThrownBy(() -> paymentService.completePayment(1L))
                .isInstanceOf(PaymentNotFoundException.class);
        
        verify(paymentRepository).findById(1L);
    }
    
    @Test
    @DisplayName("이미 완료된 결제 완료 시도 실패")
    void completePayment_AlreadyCompleted() {
        // given
        Payment completedPayment = createCompletedPayment();
        given(paymentRepository.findById(1L)).willReturn(Optional.of(completedPayment));
        
        // when & then
        assertThatThrownBy(() -> paymentService.completePayment(1L))
                .isInstanceOf(InvalidPaymentStatusException.class)
                .hasMessageContaining("결제를 완료할 수 없는 상태입니다");
        
        verify(paymentRepository).findById(1L);
    }
    
    @Test
    @DisplayName("결제 취소 성공")
    void cancelPayment_Success() {
        // given
        Payment pendingPayment = createPendingPayment();
        Payment cancelledPayment = createCancelledPayment();
        
        given(paymentRepository.findById(1L)).willReturn(Optional.of(pendingPayment));
        given(paymentRepository.save(any(Payment.class))).willReturn(cancelledPayment);
        
        // when
        PaymentResponse response = paymentService.cancelPayment(1L);
        
        // then
        assertAll("결제 취소 성공 검증",
            () -> assertNotNull(response),
            () -> assertEquals(PaymentStatus.CANCELLED, response.status()),
            () -> assertFalse(response.isCompleted()),
            () -> assertFalse(response.canCancel())
        );
        
        assertAll("메서드 호출 검증",
            () -> verify(paymentRepository).findById(1L),
            () -> verify(paymentRepository).save(any(Payment.class))
        );
    }
    
    @Test
    @DisplayName("완료된 결제 취소 시도 실패")
    void cancelPayment_AlreadyCompleted() {
        // given
        Payment completedPayment = createCompletedPayment();
        given(paymentRepository.findById(1L)).willReturn(Optional.of(completedPayment));
        
        // when & then
        assertThatThrownBy(() -> paymentService.cancelPayment(1L))
                .isInstanceOf(InvalidPaymentStatusException.class)
                .hasMessageContaining("결제를 취소할 수 없는 상태입니다");
        
        verify(paymentRepository).findById(1L);
    }
    
    @Test
    @DisplayName("결제 실패 처리 성공")
    void failPayment_Success() {
        // given
        Payment pendingPayment = createPendingPayment();
        Payment failedPayment = createFailedPayment();
        
        given(paymentRepository.findById(1L)).willReturn(Optional.of(pendingPayment));
        given(paymentRepository.save(any(Payment.class))).willReturn(failedPayment);
        
        // when
        PaymentResponse response = paymentService.failPayment(1L);
        
        // then
        assertAll("결제 실패 처리 검증",
            () -> assertNotNull(response),
            () -> assertEquals(PaymentStatus.FAILED, response.status()),
            () -> assertFalse(response.isCompleted()),
            () -> assertFalse(response.canCancel())
        );
        
        assertAll("메서드 호출 검증",
            () -> verify(paymentRepository).findById(1L),
            () -> verify(paymentRepository).save(any(Payment.class))
        );
    }
    
    @Test
    @DisplayName("결제 조회 성공")
    void getPayment_Success() {
        // given
        given(paymentRepository.findById(1L)).willReturn(Optional.of(mockPayment));
        
        // when
        PaymentResponse response = paymentService.getPayment(1L);
        
        // then
        assertAll("결제 조회 성공 검증",
            () -> assertNotNull(response),
            () -> assertEquals(mockPayment.getOrderId(), response.orderId()),
            () -> assertEquals(mockPayment.getStatus(), response.status()),
            () -> assertEquals(mockPayment.getAmount(), response.amount())
        );
        
        verify(paymentRepository).findById(1L);
    }
    
    @Test
    @DisplayName("존재하지 않는 결제 조회 실패")
    void getPayment_NotFound() {
        // given
        given(paymentRepository.findById(1L)).willReturn(Optional.empty());
        
        // when & then
        assertThatThrownBy(() -> paymentService.getPayment(1L))
                .isInstanceOf(PaymentNotFoundException.class);
        
        verify(paymentRepository).findById(1L);
    }
    
    @Test
    @DisplayName("주문별 결제 조회 성공")
    void getPaymentByOrderId_Success() {
        // given
        given(paymentRepository.findByOrderId(1L)).willReturn(Optional.of(mockPayment));
        
        // when
        PaymentResponse response = paymentService.getPaymentByOrderId(1L);
        
        // then
        assertAll("주문별 결제 조회 성공 검증",
            () -> assertNotNull(response),
            () -> assertEquals(1L, response.orderId()),
            () -> assertEquals(mockPayment.getStatus(), response.status()),
            () -> assertEquals(mockPayment.getAmount(), response.amount())
        );
        
        verify(paymentRepository).findByOrderId(1L);
    }
    
    @Test
    @DisplayName("모든 결제 조회 성공")
    void getAllPayments_Success() {
        // given
        List<Payment> payments = List.of(mockPayment, createPendingPayment());
        given(paymentRepository.findAll()).willReturn(payments);
        
        // when
        List<PaymentResponse> responses = paymentService.getAllPayments();
        
        // then
        assertAll("모든 결제 조회 검증",
            () -> assertNotNull(responses),
            () -> assertEquals(2, responses.size()),
            () -> assertEquals(mockPayment.getOrderId(), responses.get(0).orderId()),
            () -> assertEquals(2L, responses.get(1).orderId())
        );
        
        verify(paymentRepository).findAll();
    }
    
    // === 헬퍼 메서드들 ===
    
    private Payment createMockPayment() {
        return Payment.builder()
                .orderId(1L)
                .paymentMethod("CREDIT_CARD")
                .amount(new BigDecimal("10000"))
                .build();
    }
    
    private Payment createPendingPayment() {
        return Payment.builder()
                .orderId(2L)
                .paymentMethod("BANK_TRANSFER")
                .amount(new BigDecimal("20000"))
                .build();
    }
    
    private Payment createCompletedPayment() {
        Payment payment = createMockPayment();
        payment.complete();
        return payment;
    }
    
    private Payment createCancelledPayment() {
        Payment payment = createPendingPayment();
        payment.cancel();
        return payment;
    }
    
    private Payment createFailedPayment() {
        Payment payment = createPendingPayment();
        payment.fail();
        return payment;
    }
}