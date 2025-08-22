package org.zb.ecommerce.domain.admin.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.zb.ecommerce.domain.coupon.entity.CouponStatus;
import org.zb.ecommerce.domain.coupon.entity.UserCoupon;

import java.util.List;
import java.util.Optional;

/**
 * UserCoupon Repository Interface
 */
@Repository
public interface UserCouponRepository extends CrudRepository<UserCoupon, Long> {
    
    /**
     * 사용자별 쿠폰 조회
     */
    List<UserCoupon> findByUserId(Long userId);
    
    /**
     * 사용자별 상태별 쿠폰 조회
     */
    List<UserCoupon> findByUserIdAndStatus(Long userId, CouponStatus status);
    
    /**
     * 사용자의 특정 쿠폰 조회
     */
    Optional<UserCoupon> findByUserIdAndCouponId(Long userId, Long couponId);
    
    /**
     * 사용자가 특정 쿠폰을 이미 가지고 있는지 확인
     */
    boolean existsByUserIdAndCouponId(Long userId, Long couponId);
    
    /**
     * 쿠폰별 발급 수량 조회
     */
    long countByCouponId(Long couponId);
    
    /**
     * 사용자의 사용 가능한 쿠폰 수
     */
    long countByUserIdAndStatus(Long userId, CouponStatus status);
}