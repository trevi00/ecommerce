package org.zb.ecommerce.domain.cart.repository;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zb.ecommerce.domain.cart.entity.Cart;

import java.util.Optional;

/**
 * Cart Repository Interface
 */
@Repository
public interface CartRepository extends CrudRepository<Cart, Long> {
    
    /**
     * 사용자 ID로 장바구니 조회
     */
    Optional<Cart> findByUserId(Long userId);
    
    /**
     * 사용자 ID로 장바구니와 아이템 함께 조회
     */
    @Query("SELECT c.* FROM carts c WHERE c.user_id = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);
    
    /**
     * 사용자의 장바구니 존재 여부 확인
     */
    boolean existsByUserId(Long userId);
}
