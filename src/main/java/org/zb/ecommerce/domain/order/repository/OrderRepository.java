package org.zb.ecommerce.domain.order.repository;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zb.ecommerce.domain.order.entity.Order;
import org.zb.ecommerce.domain.order.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Order Repository Interface
 */
@Repository
public interface OrderRepository extends CrudRepository<Order, Long> {
    
    /**
     * 사용자별 주문 조회
     */
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * 주문 번호로 조회
     */
    Optional<Order> findByOrderNumber(String orderNumber);
    
    /**
     * 사용자 ID와 주문 ID로 조회
     */
    Optional<Order> findByIdAndUserId(Long id, Long userId);
    
    /**
     * 상태별 주문 조회
     */
    List<Order> findByStatus(OrderStatus status);
    
    /**
     * 사용자별 상태별 주문 조회
     */
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);
    
    /**
     * 기간별 주문 조회
     */
    @Query("SELECT o.* FROM orders o WHERE o.user_id = :userId " +
           "AND o.created_at BETWEEN :startDate AND :endDate " +
           "ORDER BY o.created_at DESC")
    List<Order> findByUserIdAndPeriod(@Param("userId") Long userId, 
                                      @Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);
    
    /**
     * 주문 항목과 함께 조회
     */
    @Query("SELECT DISTINCT o.* FROM orders o " +
           "LEFT JOIN order_items oi ON o.id = oi.order_id " +
           "WHERE o.id = :orderId")
    Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);
}
