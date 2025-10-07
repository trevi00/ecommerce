package org.zb.ecommerce.domain.admin.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.zb.ecommerce.config.BaseIntegrationTest;
import org.zb.ecommerce.domain.coupon.entity.CouponStatus;
import org.zb.ecommerce.domain.coupon.entity.UserCoupon;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UserCouponRepositoryTest extends BaseIntegrationTest {
    
    @Autowired
    private UserCouponRepository userCouponRepository;
    
    @Test
    @DisplayName("사용자별 쿠폰 조회")
    void findByUserId() {
        // given
        Long userId = 1L;
        UserCoupon userCoupon1 = createTestUserCoupon(userId, 1L, CouponStatus.AVAILABLE);
        UserCoupon userCoupon2 = createTestUserCoupon(userId, 2L, CouponStatus.USED);
        UserCoupon userCoupon3 = createTestUserCoupon(2L, 3L, CouponStatus.AVAILABLE);
        
        userCouponRepository.save(userCoupon1);
        userCouponRepository.save(userCoupon2);
        userCouponRepository.save(userCoupon3);
        
        // when
        List<UserCoupon> result = userCouponRepository.findByUserId(userId);
        
        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("couponId")
                .containsExactlyInAnyOrder(1L, 2L);
    }
    
    @Test
    @DisplayName("사용자별 상태별 쿠폰 조회")
    void findByUserIdAndStatus() {
        // given
        Long userId = 1L;
        UserCoupon availableCoupon1 = createTestUserCoupon(userId, 1L, CouponStatus.AVAILABLE);
        UserCoupon availableCoupon2 = createTestUserCoupon(userId, 2L, CouponStatus.AVAILABLE);
        UserCoupon usedCoupon = createTestUserCoupon(userId, 3L, CouponStatus.USED);
        
        userCouponRepository.save(availableCoupon1);
        userCouponRepository.save(availableCoupon2);
        userCouponRepository.save(usedCoupon);
        
        // when
        List<UserCoupon> result = userCouponRepository.findByUserIdAndStatus(userId, CouponStatus.AVAILABLE);
        
        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("couponId")
                .containsExactlyInAnyOrder(1L, 2L);
    }
    
    @Test
    @DisplayName("사용자의 특정 쿠폰 조회")
    void findByUserIdAndCouponId() {
        // given
        Long userId = 1L;
        Long couponId = 1L;
        UserCoupon userCoupon = createTestUserCoupon(userId, couponId, CouponStatus.AVAILABLE);
        userCouponRepository.save(userCoupon);
        
        // when
        Optional<UserCoupon> result = userCouponRepository.findByUserIdAndCouponId(userId, couponId);
        
        // then
        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(userId);
        assertThat(result.get().getCouponId()).isEqualTo(couponId);
    }
    
    @Test
    @DisplayName("사용자가 특정 쿠폰을 보유하지 않은 경우 빈 결과 반환")
    void findByUserIdAndCouponId_NotFound() {
        // given
        Long userId = 1L;
        Long nonExistentCouponId = 999L;
        
        // when
        Optional<UserCoupon> result = userCouponRepository.findByUserIdAndCouponId(userId, nonExistentCouponId);
        
        // then
        assertThat(result).isEmpty();
    }
    
    @Test
    @DisplayName("사용자가 특정 쿠폰을 이미 가지고 있는지 확인 - 보유하는 경우")
    void existsByUserIdAndCouponId_True() {
        // given
        Long userId = 1L;
        Long couponId = 1L;
        UserCoupon userCoupon = createTestUserCoupon(userId, couponId, CouponStatus.AVAILABLE);
        userCouponRepository.save(userCoupon);
        
        // when
        boolean exists = userCouponRepository.existsByUserIdAndCouponId(userId, couponId);
        
        // then
        assertThat(exists).isTrue();
    }
    
    @Test
    @DisplayName("사용자가 특정 쿠폰을 이미 가지고 있는지 확인 - 보유하지 않는 경우")
    void existsByUserIdAndCouponId_False() {
        // given
        Long userId = 1L;
        Long nonExistentCouponId = 999L;
        
        // when
        boolean exists = userCouponRepository.existsByUserIdAndCouponId(userId, nonExistentCouponId);
        
        // then
        assertThat(exists).isFalse();
    }
    
    @Test
    @DisplayName("쿠폰별 발급 수량 조회")
    void countByCouponId() {
        // given
        Long couponId = 1L;
        UserCoupon userCoupon1 = createTestUserCoupon(1L, couponId, CouponStatus.AVAILABLE);
        UserCoupon userCoupon2 = createTestUserCoupon(2L, couponId, CouponStatus.USED);
        UserCoupon userCoupon3 = createTestUserCoupon(3L, couponId, CouponStatus.EXPIRED);
        UserCoupon userCoupon4 = createTestUserCoupon(4L, 2L, CouponStatus.AVAILABLE); // 다른 쿠폰
        
        userCouponRepository.save(userCoupon1);
        userCouponRepository.save(userCoupon2);
        userCouponRepository.save(userCoupon3);
        userCouponRepository.save(userCoupon4);
        
        // when
        long count = userCouponRepository.countByCouponId(couponId);
        
        // then
        assertThat(count).isEqualTo(3);
    }
    
    @Test
    @DisplayName("사용자의 사용 가능한 쿠폰 수")
    void countByUserIdAndStatus() {
        // given
        Long userId = 1L;
        UserCoupon availableCoupon1 = createTestUserCoupon(userId, 1L, CouponStatus.AVAILABLE);
        UserCoupon availableCoupon2 = createTestUserCoupon(userId, 2L, CouponStatus.AVAILABLE);
        UserCoupon usedCoupon = createTestUserCoupon(userId, 3L, CouponStatus.USED);
        UserCoupon expiredCoupon = createTestUserCoupon(userId, 4L, CouponStatus.EXPIRED);
        
        userCouponRepository.save(availableCoupon1);
        userCouponRepository.save(availableCoupon2);
        userCouponRepository.save(usedCoupon);
        userCouponRepository.save(expiredCoupon);
        
        // when
        long availableCount = userCouponRepository.countByUserIdAndStatus(userId, CouponStatus.AVAILABLE);
        long usedCount = userCouponRepository.countByUserIdAndStatus(userId, CouponStatus.USED);
        
        // then
        assertThat(availableCount).isEqualTo(2);
        assertThat(usedCount).isEqualTo(1);
    }
    
    private UserCoupon createTestUserCoupon(Long userId, Long couponId, CouponStatus status) {
        return UserCoupon.builder()
                .userId(userId)
                .couponId(couponId)
                .status(status)
                .issuedAt(LocalDateTime.now())
                .build();
    }
}