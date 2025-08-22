package org.zb.ecommerce.domain.cart.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.zb.ecommerce.domain.cart.entity.CartItem;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends CrudRepository<CartItem, Long> {
    
    List<CartItem> findByCartId(Long cartId);
    
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
    
    void deleteByCartId(Long cartId);
    
    void deleteByCartIdAndProductId(Long cartId, Long productId);
}