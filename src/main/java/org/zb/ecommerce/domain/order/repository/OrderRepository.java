package org.zb.ecommerce.domain.order.repository;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zb.ecommerce.domain.order.entity.Order;
import org.zb.ecommerce.domain.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Order Repository Interface
 * 쿼리 메서드 우선 사용, 복잡한 쿼리만 @Query 적용
 */
@Repository
public interface OrderRepository extends CrudRepository<Order, Long>, OrderCustomRepository {
    
    /**
     * 사용자별 주문 조회 (생성일 내림차순)
     * 쿼리 메서드 사용 - 타입 안전성 보장
     */
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * 주문 번호로 조회
     * 쿼리 메서드 사용
     */
    Optional<Order> findByOrderNumber(String orderNumber);
    
    /**
     * 사용자 ID와 주문 ID로 조회
     * 쿼리 메서드 사용
     */
    Optional<Order> findByIdAndUserId(Long id, Long userId);
    
    /**
     * 상태별 주문 조회
     * 쿼리 메서드 사용
     */
    List<Order> findByStatus(OrderStatus status);
    
    /**
     * 사용자별 상태별 주문 조회
     * 쿼리 메서드 사용
     */
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);
    
    /**
     * 기간별 주문 조회
     * 쿼리 메서드 사용
     */
    List<Order> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(Long userId, 
                                                                   LocalDateTime startDate, 
                                                                   LocalDateTime endDate);
    
    /**
     * 최근 주문 조회
     * 쿼리 메서드 사용
     */
    List<Order> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * 특정 금액 이상 주문 조회
     * 쿼리 메서드 사용
     */
    List<Order> findByFinalAmountGreaterThanEqualOrderByCreatedAtDesc(BigDecimal amount);
    
    /**
     * 동적 조건 처리 - 간단한 NULL 체크
     */
    @Query("SELECT * FROM orders WHERE " +
           "(:orderId IS NULL OR id = :orderId) AND " +
           "(:status IS NULL OR status = :status) AND " +
           "(:userId IS NULL OR user_id = :userId)")
    List<Order> findByDynamicConditions(@Param("orderId") Long orderId, 
                                       @Param("status") String status,
                                       @Param("userId") Long userId);
    
    /**
     * 주문 통계용 - 월별 주문 수
     */
    @Query("SELECT COUNT(*) FROM orders WHERE " +
           "user_id = :userId AND " +
           "DATE_FORMAT(created_at, '%Y-%m') = :yearMonth")
    long countByUserIdAndYearMonth(@Param("userId") Long userId, 
                                  @Param("yearMonth") String yearMonth);
    
    /**
     * 결제 취소를 위한 할인 금액 조회
     */
    @Query("SELECT discount_amount FROM orders WHERE id = :orderId")
    Optional<BigDecimal> findDiscountAmountById(@Param("orderId") Long orderId);
    
    /**
     * 결제 취소를 위한 주문 번호 조회  
     */
    @Query("SELECT order_number FROM orders WHERE id = :orderId")
    Optional<String> findOrderNumberById(@Param("orderId") Long orderId);
}