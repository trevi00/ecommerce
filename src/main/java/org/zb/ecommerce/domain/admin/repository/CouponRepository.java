package org.zb.ecommerce.domain.admin.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.zb.ecommerce.domain.coupon.entity.Coupon;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Coupon Repository Interface
 */
@Repository
public interface CouponRepository extends CrudRepository<Coupon, Long> {
    
    /**
     * 쿠폰 코드로 조회
     */
    Optional<Coupon> findByCode(String code);
    
    /**
     * 쿠폰 코드 존재 여부 확인
     */
    boolean existsByCode(String code);
    
    /**
     * 활성화된 쿠폰 조회
     */
    List<Coupon> findByIsActiveTrue();
    
    /**
     * 사용 가능한 쿠폰 조회 (활성화 + 유효기간 + 사용횟수)
     */
    List<Coupon> findByIsActiveTrueAndValidFromLessThanEqualAndValidToGreaterThanEqual(
            LocalDateTime now1, LocalDateTime now2);
    
    /**
     * 만료된 쿠폰 조회
     */
    List<Coupon> findByValidToBefore(LocalDateTime now);
    
    /**
     * 쿠폰명으로 검색
     */
    List<Coupon> findByNameContainingIgnoreCase(String name);
}