package org.zb.ecommerce;

import org.zb.ecommerce.domain.payment.entity.Payment;
import org.zb.ecommerce.domain.payment.entity.PaymentStatus;

import java.math.BigDecimal;

/**
 * 간단한 Payment 테스트
 */
public class SimplePaymentTest {
    
    public static void main(String[] args) {
        try {
            System.out.println("=== Payment 생성 테스트 시작 ===");
            
            // Payment 생성
            Payment payment = Payment.builder()
                    .orderId(1L)
                    .paymentMethod("CREDIT_CARD")
                    .amount(BigDecimal.valueOf(10000))
                    .build();
            
            System.out.println("결제 생성 성공");
            System.out.println("주문 ID: " + payment.getOrderId());
            System.out.println("결제 수단: " + payment.getPaymentMethod());
            System.out.println("금액: " + payment.getAmount());
            System.out.println("상태: " + payment.getStatus());
            System.out.println("취소 가능 여부: " + payment.canCancel());
            System.out.println("완료 여부: " + payment.isCompleted());
            
            // 결제 완료 테스트
            System.out.println("\n=== Payment 완료 테스트 ===");
            payment.complete();
            System.out.println("결제 완료 후 상태: " + payment.getStatus());
            System.out.println("완료 여부: " + payment.isCompleted());
            System.out.println("취소 가능 여부: " + payment.canCancel());
            System.out.println("결제일: " + payment.getPaymentDate());
            
            System.out.println("\n=== 모든 테스트 성공 ===");
            
        } catch (Exception e) {
            System.err.println("테스트 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}