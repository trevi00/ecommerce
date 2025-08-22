package org.zb.ecommerce.domain.order.repository;

import org.zb.ecommerce.domain.order.entity.Order;
import org.zb.ecommerce.domain.order.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Order 동적 쿼리를 위한 Custom Repository Interface
 */
public interface OrderCustomRepository {
    
    /**
     * 동적 조건으로 주문 검색
     */
    List<Order> findOrdersWithDynamicConditions(Long userId, Long orderId, OrderStatus status, 
                                               LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 관리자용 주문 검색 (사용자 ID 없이)
     */
    List<Order> findOrdersForAdmin(Long orderId, OrderStatus status, 
                                  LocalDateTime startDate, LocalDateTime endDate);
}