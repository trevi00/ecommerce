package org.zb.ecommerce.domain.order.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.zb.ecommerce.domain.common.BaseTimeEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 주문 Entity
 * DDD의 Aggregate Root 역할
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table("orders")
public class Order extends BaseTimeEntity {
    
    @Id
    private Long id;
    
    private Long userId;
    
    private String orderNumber;
    
    private BigDecimal totalAmount;
    
    private BigDecimal discountAmount;
    
    private BigDecimal finalAmount;
    
    private OrderStatus status;
    
    @MappedCollection(idColumn = "order_id")
    private List<OrderItem> orderItems = new ArrayList<>();
    
    @Builder
    public Order(Long userId, List<OrderItem> orderItems) {
        validateUserId(userId);
        validateOrderItems(orderItems);
        
        this.userId = userId;
        this.orderNumber = generateOrderNumber();
        this.status = OrderStatus.PENDING;
        this.discountAmount = BigDecimal.ZERO;
        
        // 주문 항목 설정 및 금액 계산
        setOrderItems(orderItems);
        calculateAmounts();
    }
    
    // 비즈니스 로직
    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("대기 중인 주문만 확정할 수 있습니다.");
        }
        this.status = OrderStatus.CONFIRMED;
    }
    
    public void cancel() {
        if (!this.status.canCancel()) {
            throw new IllegalStateException("취소할 수 없는 주문 상태입니다.");
        }
        this.status = OrderStatus.CANCELLED;
    }
    
    public void applyCoupon(BigDecimal discountAmount) {
        if (discountAmount == null || discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("할인 금액이 올바르지 않습니다.");
        }
        if (discountAmount.compareTo(this.totalAmount) > 0) {
            throw new IllegalArgumentException("할인 금액이 총 금액보다 클 수 없습니다.");
        }
        
        this.discountAmount = discountAmount;
        calculateFinalAmount();
    }
    
    private void setOrderItems(List<OrderItem> items) {
        this.orderItems.clear();
        if (items != null) {
            this.orderItems.addAll(items);
        }
    }
    
    private void calculateAmounts() {
        this.totalAmount = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        calculateFinalAmount();
    }
    
    private void calculateFinalAmount() {
        this.finalAmount = this.totalAmount.subtract(this.discountAmount);
    }
    
    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    // 검증 로직
    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("사용자 ID가 올바르지 않습니다.");
        }
    }
    
    private void validateOrderItems(List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            throw new IllegalArgumentException("주문 항목이 비어있습니다.");
        }
    }
}
