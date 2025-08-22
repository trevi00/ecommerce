package org.zb.ecommerce.domain.payment.repository;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zb.ecommerce.domain.payment.entity.Payment;
import org.zb.ecommerce.domain.payment.entity.PaymentStatus;

import java.util.List;
import java.util.Optional;

/**
 * 결제 Repository
 */
@Repository
public interface PaymentRepository extends CrudRepository<Payment, Long> {
    
    /**
     * 주문 ID로 결제 조회
     */
    @Query("SELECT * FROM payments WHERE order_id = :orderId")
    Optional<Payment> findByOrderId(@Param("orderId") Long orderId);
    
    /**
     * 주문 ID로 결제 존재 여부 확인
     */
    @Query("SELECT COUNT(*) > 0 FROM payments WHERE order_id = :orderId")
    boolean existsByOrderId(@Param("orderId") Long orderId);
    
    /**
     * 결제 상태로 결제 목록 조회
     */
    List<Payment> findByStatus(PaymentStatus status);
    
    /**
     * 결제 상태로 결제 목록 조회 (생성일 내림차순)
     */
    @Query("SELECT * FROM payments WHERE status = :status ORDER BY created_at DESC")
    List<Payment> findByStatusOrderByCreatedAtDesc(@Param("status") String status);
    
    /**
     * 결제 수단으로 결제 목록 조회
     */
    @Query("SELECT * FROM payments WHERE payment_method = :paymentMethod")
    List<Payment> findByPaymentMethod(@Param("paymentMethod") String paymentMethod);
    
    /**
     * 모든 결제 조회
     */
    @Query("SELECT * FROM payments ORDER BY created_at DESC")
    List<Payment> findAll();
    
    /**
     * 완료된 결제 개수 조회 (간단한 통계용)
     */
    @Query("SELECT COUNT(*) FROM payments WHERE status = 'COMPLETED'")
    long countCompletedPayments();
    
    /**
     * 특정 주문의 완료된 결제가 있는지 확인 (중복 결제 방지용)
     */
    @Query("SELECT COUNT(*) > 0 FROM payments WHERE order_id = :orderId AND status = 'COMPLETED'")
    boolean existsCompletedPaymentByOrderId(@Param("orderId") Long orderId);
}