package org.zb.ecommerce.domain.payment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.zb.ecommerce.config.BaseIntegrationTest;
import org.zb.ecommerce.domain.payment.dto.PaymentRequest;
import org.zb.ecommerce.domain.payment.dto.PaymentResponse;
import org.zb.ecommerce.domain.payment.entity.PaymentStatus;
import org.zb.ecommerce.domain.payment.exception.InvalidPaymentStatusException;
import org.zb.ecommerce.domain.payment.exception.PaymentAlreadyExistsException;
import org.zb.ecommerce.domain.payment.exception.PaymentNotFoundException;
import org.zb.ecommerce.domain.payment.repository.PaymentRepository;
import org.zb.ecommerce.domain.coupon.service.CouponValidationService;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaymentService 통합 테스트")
class PaymentServiceTest extends BaseIntegrationTest {
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @MockitoBean
    private CouponValidationService couponValidationService;
    
    private PaymentRequest paymentRequest;
    private TestDataSet testData;
    
    @BeforeEach
    void setUp() {
        // BaseIntegrationTest의 팩토리 메서드를 사용하여 테스트 데이터 생성
        testData = createCompleteTestData();
        
        // 테스트용 결제 요청 DTO
        paymentRequest = new PaymentRequest(testData.orderId, "CREDIT_CARD", new BigDecimal("10000.00"));
    }
    
    @Test
    @DisplayName("결제 생성 성공")
    void createPayment_Success() {
        // when
        PaymentResponse response = paymentService.createPayment(paymentRequest);
        
        // then - assertAll을 활용한 그룹화된 검증
        assertAll("결제 생성 성공 검증",
            () -> assertNotNull(response, "응답은 null이 아니어야 한다"),
            () -> assertEquals(testData.orderId, response.orderId(), "주문 ID가 일치해야 한다"),
            () -> assertEquals("CREDIT_CARD", response.paymentMethod(), "결제 수단이 일치해야 한다"),
            () -> assertEquals(new BigDecimal("10000.00"), response.amount(), "결제 금액이 일치해야 한다"),
            () -> assertEquals(PaymentStatus.PENDING, response.status(), "결제 상태는 PENDING이어야 한다"),
            () -> assertFalse(response.isCompleted(), "새로 생성된 결제는 완료 상태가 아니어야 한다"),
            () -> assertTrue(response.canCancel(), "새로 생성된 결제는 취소 가능해야 한다")
        );
        
        // 데이터베이스에 실제로 저장되었는지 확인
        assertThat(paymentRepository.findByOrderId(testData.orderId)).isPresent();
    }
    
    @Test
    @DisplayName("이미 존재하는 결제로 인한 생성 실패")
    void createPayment_PaymentAlreadyExists() {
        // given
        paymentService.createPayment(paymentRequest);
        
        // when & then
        assertThatThrownBy(() -> paymentService.createPayment(paymentRequest))
                .isInstanceOf(PaymentAlreadyExistsException.class)
                .hasMessageContaining("이미 해당 주문에 대한 결제가 존재합니다");
    }
    
    @Test
    @DisplayName("결제 완료 성공")
    void completePayment_Success() {
        // given
        PaymentResponse createdPayment = paymentService.createPayment(paymentRequest);
        
        // when
        PaymentResponse response = paymentService.completePayment(createdPayment.id());
        
        // then
        assertAll("결제 완료 성공 검증",
            () -> assertNotNull(response, "응답이 있어야 한다"),
            () -> assertEquals(PaymentStatus.COMPLETED, response.status(), "결제 상태가 COMPLETED여야 한다"),
            () -> assertTrue(response.isCompleted(), "완료된 결제여야 한다"),
            () -> assertFalse(response.canCancel(), "완료된 결제는 취소할 수 없어야 한다"),
            () -> assertNotNull(response.createdAt(), "생성 일시가 설정되어야 한다")
        );
    }
    
    @Test
    @DisplayName("존재하지 않는 결제 완료 실패")
    void completePayment_NotFound() {
        // when & then
        assertThatThrownBy(() -> paymentService.completePayment(999L))
                .isInstanceOf(PaymentNotFoundException.class);
    }
    
    @Test
    @DisplayName("이미 완료된 결제 완료 시도 실패")
    void completePayment_AlreadyCompleted() {
        // given
        PaymentResponse createdPayment = paymentService.createPayment(paymentRequest);
        paymentService.completePayment(createdPayment.id());
        
        // when & then
        assertThatThrownBy(() -> paymentService.completePayment(createdPayment.id()))
                .isInstanceOf(InvalidPaymentStatusException.class)
                .hasMessageContaining("결제를 완료할 수 없는 상태입니다");
    }
    
    @Test
    @DisplayName("결제 취소 성공")
    void cancelPayment_Success() {
        // given
        PaymentResponse createdPayment = paymentService.createPayment(paymentRequest);
        
        // when
        PaymentResponse response = paymentService.cancelPayment(createdPayment.id());
        
        // then
        assertAll("결제 취소 성공 검증",
            () -> assertNotNull(response),
            () -> assertEquals(PaymentStatus.CANCELLED, response.status()),
            () -> assertFalse(response.isCompleted()),
            () -> assertFalse(response.canCancel())
        );
    }
    
    @Test
    @DisplayName("완료된 결제 취소 시도 실패")
    void cancelPayment_AlreadyCompleted() {
        // given
        PaymentResponse createdPayment = paymentService.createPayment(paymentRequest);
        paymentService.completePayment(createdPayment.id());
        
        // when & then
        assertThatThrownBy(() -> paymentService.cancelPayment(createdPayment.id()))
                .isInstanceOf(InvalidPaymentStatusException.class)
                .hasMessageContaining("결제를 취소할 수 없는 상태입니다");
    }
    
    @Test
    @DisplayName("결제 실패 처리 성공")
    void failPayment_Success() {
        // given
        PaymentResponse createdPayment = paymentService.createPayment(paymentRequest);
        
        // when
        PaymentResponse response = paymentService.failPayment(createdPayment.id());
        
        // then
        assertAll("결제 실패 처리 검증",
            () -> assertNotNull(response),
            () -> assertEquals(PaymentStatus.FAILED, response.status()),
            () -> assertFalse(response.isCompleted()),
            () -> assertFalse(response.canCancel())
        );
    }
    
    @Test
    @DisplayName("결제 조회 성공")
    void getPayment_Success() {
        // given
        PaymentResponse createdPayment = paymentService.createPayment(paymentRequest);
        
        // when
        PaymentResponse response = paymentService.getPayment(createdPayment.id());
        
        // then
        assertAll("결제 조회 성공 검증",
            () -> assertNotNull(response),
            () -> assertEquals(createdPayment.orderId(), response.orderId()),
            () -> assertEquals(PaymentStatus.PENDING, response.status()),
            () -> assertEquals(createdPayment.amount(), response.amount())
        );
    }
    
    @Test
    @DisplayName("존재하지 않는 결제 조회 실패")
    void getPayment_NotFound() {
        // when & then
        assertThatThrownBy(() -> paymentService.getPayment(999L))
                .isInstanceOf(PaymentNotFoundException.class);
    }
    
    @Test
    @DisplayName("주문별 결제 조회 성공")
    void getPaymentByOrderId_Success() {
        // given
        PaymentResponse createdPayment = paymentService.createPayment(paymentRequest);
        
        // when
        PaymentResponse response = paymentService.getPaymentByOrderId(testData.orderId);
        
        // then
        assertAll("주문별 결제 조회 성공 검증",
            () -> assertNotNull(response),
            () -> assertEquals(testData.orderId, response.orderId()),
            () -> assertEquals(PaymentStatus.PENDING, response.status()),
            () -> assertEquals(createdPayment.amount(), response.amount())
        );
    }
    
    @Test
    @DisplayName("모든 결제 조회 성공")
    void getAllPayments_Success() {
        // given
        paymentService.createPayment(paymentRequest);
        
        // 두 번째 주문 데이터 생성
        TestDataSet secondTestData = createCompleteTestData();
        PaymentRequest secondRequest = new PaymentRequest(secondTestData.orderId, "BANK_TRANSFER", new BigDecimal("10000.00"));
        paymentService.createPayment(secondRequest);
        
        // when
        List<PaymentResponse> responses = paymentService.getAllPayments();
        
        // then
        assertAll("모든 결제 조회 검증",
            () -> assertNotNull(responses),
            () -> assertThat(responses).hasSizeGreaterThanOrEqualTo(2),
            () -> assertThat(responses).anyMatch(p -> p.orderId().equals(testData.orderId)),
            () -> assertThat(responses).anyMatch(p -> p.orderId().equals(secondTestData.orderId))
        );
    }
}