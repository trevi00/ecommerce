package org.zb.ecommerce.domain.product.repository;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zb.ecommerce.domain.product.entity.Product;

import java.util.List;
import java.util.Optional;

/**
 * Product Repository Interface
 */
@Repository
public interface ProductRepository extends CrudRepository<Product, Long> {
    
    /**
     * 카테고리별 상품 조회
     */
    List<Product> findByCategory(String category);
    
    /**
     * 상품명으로 검색
     */
    List<Product> findByNameContaining(String name);
    
    /**
     * 재고가 있는 상품만 조회
     */
    @Query("SELECT * FROM products WHERE stock_quantity > 0")
    List<Product> findAvailableProducts();
    
    /**
     * 여러 상품 ID로 조회
     */
    @Query("SELECT * FROM products WHERE id IN (:ids)")
    List<Product> findByIdIn(@Param("ids") List<Long> ids);
    
    /**
     * 재고 감소
     */
    @Modifying
    @Query("UPDATE products SET stock_quantity = stock_quantity - :quantity WHERE id = :id AND stock_quantity >= :quantity")
    int decreaseStock(@Param("id") Long id, @Param("quantity") int quantity);
    
    /**
     * 재고 증가
     */
    @Modifying
    @Query("UPDATE products SET stock_quantity = stock_quantity + :quantity WHERE id = :id")
    void increaseStock(@Param("id") Long id, @Param("quantity") int quantity);
}
