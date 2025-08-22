package org.zb.ecommerce.domain.order.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.zb.ecommerce.domain.order.entity.Order;
import org.zb.ecommerce.domain.order.entity.OrderStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order 동적 쿼리를 위한 Custom Repository 구현체
 * StringBuilder를 사용한 효율적인 동적 쿼리 처리
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class OrderCustomRepositoryImpl implements OrderCustomRepository {
    
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    
    private static final String BASE_SELECT_SQL = 
        "SELECT id, user_id, order_number, status, " +
        "total_amount, discount_amount, final_amount, created_at, updated_at " +
        "FROM orders";
    
    @Override
    public List<Order> findOrdersWithDynamicConditions(Long userId, Long orderId, OrderStatus status,
                                                       LocalDateTime startDate, LocalDateTime endDate) {
        
        DynamicQueryBuilder queryBuilder = new DynamicQueryBuilder(BASE_SELECT_SQL);
        MapSqlParameterSource params = new MapSqlParameterSource();
        
        // 동적 조건 추가
        addUserIdCondition(queryBuilder, params, userId);
        addOrderIdCondition(queryBuilder, params, orderId);
        addStatusCondition(queryBuilder, params, status);
        addDateRangeConditions(queryBuilder, params, startDate, endDate);
        
        // 정렬 조건 추가
        queryBuilder.addOrderBy("created_at DESC");
        
        String finalSql = queryBuilder.build();
        
        log.debug("Dynamic query: {}", finalSql);
        log.debug("Parameters: {}", params.getValues());
        
        return namedParameterJdbcTemplate.query(finalSql, params, new OrderRowMapper());
    }
    
    @Override
    public List<Order> findOrdersForAdmin(Long orderId, OrderStatus status,
                                         LocalDateTime startDate, LocalDateTime endDate) {
        // 관리자용은 userId 조건 없이 호출
        return findOrdersWithDynamicConditions(null, orderId, status, startDate, endDate);
    }
    
    /**
     * 사용자 ID 조건 추가
     */
    private void addUserIdCondition(DynamicQueryBuilder queryBuilder, 
                                   MapSqlParameterSource params, Long userId) {
        if (userId != null) {
            queryBuilder.addCondition("user_id = :userId");
            params.addValue("userId", userId);
        }
    }
    
    /**
     * 주문 ID 조건 추가
     */
    private void addOrderIdCondition(DynamicQueryBuilder queryBuilder, 
                                   MapSqlParameterSource params, Long orderId) {
        if (orderId != null) {
            queryBuilder.addCondition("id = :orderId");
            params.addValue("orderId", orderId);
        }
    }
    
    /**
     * 주문 상태 조건 추가
     */
    private void addStatusCondition(DynamicQueryBuilder queryBuilder, 
                                   MapSqlParameterSource params, OrderStatus status) {
        if (status != null) {
            queryBuilder.addCondition("status = :status");
            params.addValue("status", status.name());
        }
    }
    
    /**
     * 날짜 범위 조건 추가
     */
    private void addDateRangeConditions(DynamicQueryBuilder queryBuilder, 
                                       MapSqlParameterSource params, 
                                       LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null) {
            queryBuilder.addCondition("created_at >= :startDate");
            params.addValue("startDate", startDate);
        }
        
        if (endDate != null) {
            queryBuilder.addCondition("created_at <= :endDate");
            params.addValue("endDate", endDate);
        }
    }
    
    /**
     * 동적 쿼리 빌더 클래스
     * StringBuilder를 사용한 효율적인 쿼리 구성
     */
    private static class DynamicQueryBuilder {
        private final StringBuilder sql;
        private final List<String> conditions = new ArrayList<>();
        private String orderBy;
        
        public DynamicQueryBuilder(String baseSql) {
            this.sql = new StringBuilder(baseSql);
        }
        
        public void addCondition(String condition) {
            conditions.add(condition);
        }
        
        public void addOrderBy(String orderBy) {
            this.orderBy = orderBy;
        }
        
        public String build() {
            if (!conditions.isEmpty()) {
                sql.append(" WHERE ");
                sql.append(String.join(" AND ", conditions));
            }
            
            if (orderBy != null && !orderBy.isEmpty()) {
                sql.append(" ORDER BY ").append(orderBy);
            }
            
            return sql.toString();
        }
    }
    
    /**
     * Order RowMapper
     * ResultSet을 Order 객체로 매핑
     */
    private static class OrderRowMapper implements RowMapper<Order> {
        @Override
        public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Order.builder()
                    .userId(rs.getLong("user_id"))
                    .totalAmount(rs.getBigDecimal("total_amount"))
                    .build()
                    .withDatabaseValues(
                            rs.getLong("id"),
                            rs.getString("order_number"),
                            OrderStatus.valueOf(rs.getString("status")),
                            rs.getBigDecimal("discount_amount"),
                            rs.getBigDecimal("final_amount"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getTimestamp("updated_at") != null ? 
                                rs.getTimestamp("updated_at").toLocalDateTime() : null
                    );
        }
    }
}