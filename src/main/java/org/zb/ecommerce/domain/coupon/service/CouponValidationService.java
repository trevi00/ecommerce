package org.zb.ecommerce.domain.coupon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zb.ecommerce.domain.admin.repository.CouponRepository;
import org.zb.ecommerce.domain.admin.repository.UserCouponRepository;
import org.zb.ecommerce.domain.coupon.entity.Coupon;
import org.zb.ecommerce.domain.coupon.entity.CouponStatus;
import org.zb.ecommerce.domain.coupon.entity.UserCoupon;
import org.zb.ecommerce.domain.coupon.exception.CouponNotAvailableException;
import org.zb.ecommerce.domain.coupon.exception.CouponNotFoundException;
import org.zb.ecommerce.global.annotation.CustomTransactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponValidationService {
    
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    
    /**
     * 쿠폰 사용 가능 여부 검증 및 할인 금액 계산
     */
    @Transactional(readOnly = true)
    public BigDecimal validateAndCalculateDiscount(Long userId, Long userCouponId, BigDecimal orderAmount) {
        if (userCouponId == null) {
            return BigDecimal.ZERO;
        }
        
        // 사용자 쿠폰 조회
        UserCoupon userCoupon = userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> new CouponNotFoundException("사용자 쿠폰을 찾을 수 없습니다."));
        
        // 본인 쿠폰인지 확인
        if (!userCoupon.getUserId().equals(userId)) {
            throw new CouponNotAvailableException("본인의 쿠폰이 아닙니다.");
        }
        
        // 쿠폰 상태 확인
        if (userCoupon.getStatus() != CouponStatus.AVAILABLE) {
            throw new CouponNotAvailableException("사용할 수 없는 쿠폰입니다. 상태: " + userCoupon.getStatus());
        }
        
        // 쿠폰 정보 조회
        Coupon coupon = couponRepository.findById(userCoupon.getCouponId())
                .orElseThrow(() -> new CouponNotFoundException("쿠폰 정보를 찾을 수 없습니다."));
        
        // 쿠폰 사용 가능 여부 확인
        if (!coupon.canUse()) {
            throw new CouponNotAvailableException("만료되었거나 비활성화된 쿠폰입니다.");
        }
        
        // 할인 금액 계산
        return coupon.calculateDiscountAmount(orderAmount);
    }
    
    /**
     * 쿠폰 사용 처리
     */
    @CustomTransactional
    public void useCoupon(Long userId, Long userCouponId, Long orderId) {
        if (userCouponId == null) {
            return;
        }
        
        UserCoupon userCoupon = userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> new CouponNotFoundException("사용자 쿠폰을 찾을 수 없습니다."));
        
        // 본인 쿠폰인지 재확인
        if (!userCoupon.getUserId().equals(userId)) {
            throw new CouponNotAvailableException("본인의 쿠폰이 아닙니다.");
        }
        
        // 쿠폰 사용 처리
        userCoupon.use(orderId);
        userCouponRepository.save(userCoupon);
        
        // 쿠폰 사용 횟수 증가
        Coupon coupon = couponRepository.findById(userCoupon.getCouponId())
                .orElseThrow(() -> new CouponNotFoundException("쿠폰 정보를 찾을 수 없습니다."));
        
        coupon.use();
        couponRepository.save(coupon);
    }
    
    /**
     * 주문 취소 시 쿠폰 복구
     */
    @CustomTransactional
    public void restoreCoupon(Long orderId) {
        // 해당 주문에 사용된 쿠폰 조회 (모든 사용자 쿠폰에서 orderId로 필터링)
        List<UserCoupon> allUserCoupons = (List<UserCoupon>) userCouponRepository.findAll();
        UserCoupon userCoupon = allUserCoupons.stream()
                .filter(uc -> orderId.equals(uc.getOrderId()))
                .findFirst()
                .orElse(null);
        
        if (userCoupon != null && userCoupon.getStatus() == CouponStatus.USED) {
            // 쿠폰 상태를 다시 사용 가능으로 변경
            UserCoupon restoredCoupon = UserCoupon.builder()
                    .userId(userCoupon.getUserId())
                    .couponId(userCoupon.getCouponId())
                    .status(CouponStatus.AVAILABLE)
                    .issuedAt(userCoupon.getIssuedAt())
                    .build();
            
            userCouponRepository.save(restoredCoupon);
            
            // 원래 사용된 쿠폰 삭제
            userCouponRepository.delete(userCoupon);
            
            // 쿠폰 사용 횟수 감소
            Coupon coupon = couponRepository.findById(userCoupon.getCouponId())
                    .orElse(null);
            
            if (coupon != null && coupon.getCurrentUsageCount() > 0) {
                coupon.cancelUse();
                couponRepository.save(coupon);
            }
        }
    }
}