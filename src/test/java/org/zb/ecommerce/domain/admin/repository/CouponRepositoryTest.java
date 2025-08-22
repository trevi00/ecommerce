package org.zb.ecommerce.domain.admin.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.zb.ecommerce.config.BaseIntegrationTest;
import org.zb.ecommerce.domain.coupon.entity.Coupon;
import org.zb.ecommerce.domain.coupon.entity.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CouponRepositoryTest extends BaseIntegrationTest {
    
    @Autowired
    private CouponRepository couponRepository;
    
    @Test
    @DisplayName("쿠폰 코드로 조회 성공")
    void findByCode_Success() {
        // given
        String code = "WELCOME10";
        Coupon coupon = createTestCoupon("웰컴 쿠폰", code, DiscountType.PERCENTAGE, BigDecimal.valueOf(10));
        couponRepository.save(coupon);
        
        // when
        Optional<Coupon> result = couponRepository.findByCode(code);
        
        // then
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo(code);
        assertThat(result.get().getName()).isEqualTo("웰컴 쿠폰");
    }
    
    @Test
    @DisplayName("존재하지 않는 쿠폰 코드로 조회 시 빈 결과 반환")
    void findByCode_NotFound() {
        // given
        String nonExistentCode = "NOTEXIST";
        
        // when
        Optional<Coupon> result = couponRepository.findByCode(nonExistentCode);
        
        // then
        assertThat(result).isEmpty();
    }
    
    @Test
    @DisplayName("쿠폰 코드 존재 여부 확인 - 존재하는 경우")
    void existsByCode_True() {
        // given
        String code = "WELCOME10";
        Coupon coupon = createTestCoupon("웰컴 쿠폰", code, DiscountType.PERCENTAGE, BigDecimal.valueOf(10));
        couponRepository.save(coupon);
        
        // when
        boolean exists = couponRepository.existsByCode(code);
        
        // then
        assertThat(exists).isTrue();
    }
    
    @Test
    @DisplayName("쿠폰 코드 존재 여부 확인 - 존재하지 않는 경우")
    void existsByCode_False() {
        // given
        String nonExistentCode = "NOTEXIST";
        
        // when
        boolean exists = couponRepository.existsByCode(nonExistentCode);
        
        // then
        assertThat(exists).isFalse();
    }
    
    @Test
    @DisplayName("활성화된 쿠폰만 조회")
    void findByIsActiveTrue() {
        // given
        Coupon activeCoupon1 = createTestCoupon("활성 쿠폰1", "ACTIVE1", DiscountType.PERCENTAGE, BigDecimal.valueOf(10));
        Coupon activeCoupon2 = createTestCoupon("활성 쿠폰2", "ACTIVE2", DiscountType.FIXED_AMOUNT, BigDecimal.valueOf(5000));
        Coupon inactiveCoupon = createTestCoupon("비활성 쿠폰", "INACTIVE", DiscountType.PERCENTAGE, BigDecimal.valueOf(20));
        
        couponRepository.save(activeCoupon1);
        couponRepository.save(activeCoupon2);
        couponRepository.save(inactiveCoupon);
        
        // 비활성화
        inactiveCoupon.deactivate();
        couponRepository.save(inactiveCoupon);
        
        // when
        List<Coupon> result = couponRepository.findByIsActiveTrue();
        
        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("code")
                .containsExactlyInAnyOrder("ACTIVE1", "ACTIVE2");
    }
    
    @Test
    @DisplayName("사용 가능한 쿠폰 조회 (활성화 + 유효기간)")
    void findByIsActiveTrueAndValidPeriod() {
        // given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime validStart = now.minusDays(1);
        LocalDateTime validEnd = now.plusDays(1);
        LocalDateTime expiredEnd = now.minusHours(1);
        LocalDateTime futureStart = now.plusDays(1);
        
        Coupon validCoupon = createTestCouponWithPeriod("유효 쿠폰", "VALID", validStart, validEnd);
        Coupon expiredCoupon = createTestCouponWithPeriod("만료 쿠폰", "EXPIRED", validStart, expiredEnd);
        Coupon futureCoupon = createTestCouponWithPeriod("미래 쿠폰", "FUTURE", futureStart, validEnd);
        
        couponRepository.save(validCoupon);
        couponRepository.save(expiredCoupon);
        couponRepository.save(futureCoupon);
        
        // when
        List<Coupon> result = couponRepository.findByIsActiveTrueAndValidFromLessThanEqualAndValidToGreaterThanEqual(
                now, now);
        
        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("VALID");
    }
    
    @Test
    @DisplayName("만료된 쿠폰 조회")
    void findByValidToBefore() {
        // given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime validEnd = now.plusDays(1);
        LocalDateTime expiredEnd = now.minusHours(1);
        
        Coupon validCoupon = createTestCouponWithPeriod("유효 쿠폰", "VALID", now.minusDays(1), validEnd);
        Coupon expiredCoupon = createTestCouponWithPeriod("만료 쿠폰", "EXPIRED", now.minusDays(1), expiredEnd);
        
        couponRepository.save(validCoupon);
        couponRepository.save(expiredCoupon);
        
        // when
        List<Coupon> result = couponRepository.findByValidToBefore(now);
        
        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("EXPIRED");
    }
    
    @Test
    @DisplayName("쿠폰명으로 검색 (대소문자 무시)")
    void findByNameContainingIgnoreCase() {
        // given
        Coupon coupon1 = createTestCoupon("Welcome Coupon", "WELCOME1", DiscountType.PERCENTAGE, BigDecimal.valueOf(10));
        Coupon coupon2 = createTestCoupon("welcome new user", "WELCOME2", DiscountType.FIXED_AMOUNT, BigDecimal.valueOf(5000));
        Coupon coupon3 = createTestCoupon("Special Discount", "SPECIAL", DiscountType.PERCENTAGE, BigDecimal.valueOf(20));
        
        couponRepository.save(coupon1);
        couponRepository.save(coupon2);
        couponRepository.save(coupon3);
        
        // when
        List<Coupon> result = couponRepository.findByNameContainingIgnoreCase("welcome");
        
        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("code")
                .containsExactlyInAnyOrder("WELCOME1", "WELCOME2");
    }
    
    private Coupon createTestCoupon(String name, String code, DiscountType discountType, BigDecimal discountValue) {
        return Coupon.builder()
                .name(name)
                .code(code)
                .discountType(discountType)
                .discountValue(discountValue)
                .minOrderAmount(BigDecimal.ZERO)
                .maxDiscountAmount(BigDecimal.valueOf(50000))
                .validFrom(LocalDateTime.now().minusDays(1))
                .validTo(LocalDateTime.now().plusDays(30))
                .maxUsageCount(100)
                .build();
    }
    
    private Coupon createTestCouponWithPeriod(String name, String code, LocalDateTime validFrom, LocalDateTime validTo) {
        return Coupon.builder()
                .name(name)
                .code(code)
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(10))
                .minOrderAmount(BigDecimal.ZERO)
                .maxDiscountAmount(BigDecimal.valueOf(50000))
                .validFrom(validFrom)
                .validTo(validTo)
                .maxUsageCount(100)
                .build();
    }
}