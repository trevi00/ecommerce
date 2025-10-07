package org.zb.ecommerce.domain.payment.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Payment Entity 단위 테스트
 * 주니어 개발자가 작성한 간단한 엔티티 테스트
 */
@DisplayName("Payment Entity 테스트")
class PaymentTest {

    @Test
    @DisplayName("결제 생성 - 성공")
    void createPayment_Success() {
        // given
        Long orderId = 1L;
        String paymentMethod = "CREDIT_CARD";
        BigDecimal amount = BigDecimal.valueOf(10000);

        // when
        Payment payment = Payment.builder()
                .orderId(orderId)
                .paymentMethod(paymentMethod)
                .amount(amount)
                .build();

        // then - assertAll을 활용한 그룹화된 검증
        assertAll("결제 생성 성공 검증",
            () -> assertNotNull(payment, "결제 객체는 null이 아니어야 한다"),
            () -> assertEquals(orderId, payment.getOrderId(), "주문 ID가 일치해야 한다"),
            () -> assertEquals(paymentMethod, payment.getPaymentMethod(), "결제 수단이 일치해야 한다"),
            () -> assertEquals(amount, payment.getAmount(), "결제 금액이 일치해야 한다"),
            () -> assertEquals(PaymentStatus.PENDING, payment.getStatus(), "결제 상태는 PENDING이어야 한다"),
            () -> assertNotNull(payment.getCreatedAt(), "생성일시가 설정되어야 한다"),
            () -> assertNotNull(payment.getUpdatedAt(), "수정일시가 설정되어야 한다"),
            () -> assertNull(payment.getPaymentDate(), "결제일시는 초기에 null이어야 한다")
        );
    }

    @Test
    @DisplayName("결제 완료 - 성공")
    void completePayment_Success() {
        // given
        Payment payment = Payment.builder()
                .orderId(1L)
                .paymentMethod("CREDIT_CARD")
                .amount(BigDecimal.valueOf(10000))
                .build();

        // when
        payment.complete();

        // then
        assertAll("결제 완료 검증",
            () -> assertEquals(PaymentStatus.COMPLETED, payment.getStatus(), "결제 상태가 COMPLETED여야 한다"),
            () -> assertNotNull(payment.getPaymentDate(), "결제 완료일시가 설정되어야 한다"),
            () -> assertTrue(payment.isCompleted(), "완료된 결제여야 한다"),
            () -> assertFalse(payment.canCancel(), "완료된 결제는 취소할 수 없어야 한다")
        );
    }

    @Test
    @DisplayName("결제 취소 - 성공")
    void cancelPayment_Success() {
        // given
        Payment payment = Payment.builder()
                .orderId(1L)
                .paymentMethod("CREDIT_CARD")
                .amount(BigDecimal.valueOf(10000))
                .build();

        // when
        payment.cancel();

        // then
        assertAll("결제 취소 검증",
            () -> assertEquals(PaymentStatus.CANCELLED, payment.getStatus(), "결제 상태가 CANCELLED여야 한다"),
            () -> assertFalse(payment.isCompleted(), "취소된 결제는 완료 상태가 아니어야 한다"),
            () -> assertFalse(payment.canCancel(), "취소된 결제는 재취소할 수 없어야 한다")
        );
    }

    @Test
    @DisplayName("결제 실패 - 성공")
    void failPayment_Success() {
        // given
        Payment payment = Payment.builder()
                .orderId(1L)
                .paymentMethod("CREDIT_CARD")
                .amount(BigDecimal.valueOf(10000))
                .build();

        // when
        payment.fail();

        // then
        assertAll("결제 실패 검증",
            () -> assertEquals(PaymentStatus.FAILED, payment.getStatus(), "결제 상태가 FAILED여야 한다"),
            () -> assertFalse(payment.isCompleted(), "실패한 결제는 완료 상태가 아니어야 한다"),
            () -> assertFalse(payment.canCancel(), "실패한 결제는 취소할 수 없어야 한다")
        );
    }

    @Test
    @DisplayName("완료된 결제 완료 시도 - 실패")
    void completeCompletedPayment_Fail() {
        // given
        Payment payment = Payment.builder()
                .orderId(1L)
                .paymentMethod("CREDIT_CARD")
                .amount(BigDecimal.valueOf(10000))
                .build();
        payment.complete();

        // when & then
        assertThrows(IllegalStateException.class, payment::complete,
            "이미 완료된 결제는 재완료할 수 없어야 한다");
    }

    @Test
    @DisplayName("완료된 결제 취소 시도 - 실패")
    void cancelCompletedPayment_Fail() {
        // given
        Payment payment = Payment.builder()
                .orderId(1L)
                .paymentMethod("CREDIT_CARD")
                .amount(BigDecimal.valueOf(10000))
                .build();
        payment.complete();

        // when & then
        assertThrows(IllegalStateException.class, payment::cancel,
            "완료된 결제는 취소할 수 없어야 한다");
    }

    @Test
    @DisplayName("완료되지 않은 결제 실패 시도 - 실패")
    void failNonPendingPayment_Fail() {
        // given
        Payment payment = Payment.builder()
                .orderId(1L)
                .paymentMethod("CREDIT_CARD")
                .amount(BigDecimal.valueOf(10000))
                .build();
        payment.complete();

        // when & then
        assertThrows(IllegalStateException.class, payment::fail,
            "완료된 결제는 실패 처리할 수 없어야 한다");
    }
}