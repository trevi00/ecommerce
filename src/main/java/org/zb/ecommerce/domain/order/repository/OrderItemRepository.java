package org.zb.ecommerce.domain.order.repository;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zb.ecommerce.domain.order.entity.OrderItem;

import java.util.List;

@Repository
public interface OrderItemRepository extends CrudRepository<OrderItem, Long> {
    
    @Query("SELECT * FROM order_items WHERE order_id = :orderId")
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);
    
    @Modifying
    @Query("DELETE FROM order_items WHERE order_id = :orderId")
    void deleteByOrderId(@Param("orderId") Long orderId);
    
    @Query("SELECT * FROM order_items WHERE order_id IN (:orderIds)")
    List<OrderItem> findByOrderIdIn(@Param("orderIds") List<Long> orderIds);
}