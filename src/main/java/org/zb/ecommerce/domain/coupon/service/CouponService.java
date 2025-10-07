package org.zb.ecommerce.domain.coupon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zb.ecommerce.domain.admin.dto.CouponResponse;
import org.zb.ecommerce.domain.admin.repository.CouponRepository;
import org.zb.ecommerce.domain.admin.repository.UserCouponRepository;
import org.zb.ecommerce.domain.coupon.dto.AvailableCouponResponse;
import org.zb.ecommerce.domain.coupon.dto.DownloadCouponRequest;
import org.zb.ecommerce.domain.coupon.dto.UserCouponResponse;
import org.zb.ecommerce.domain.coupon.entity.Coupon;
import org.zb.ecommerce.domain.coupon.entity.CouponStatus;
import org.zb.ecommerce.domain.coupon.entity.UserCoupon;
import org.zb.ecommerce.domain.coupon.exception.CouponAlreadyOwnedException;
import org.zb.ecommerce.domain.coupon.exception.CouponNotAvailableException;
import org.zb.ecommerce.domain.coupon.exception.CouponNotFoundException;
import org.zb.ecommerce.global.annotation.CustomTransactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService {
    
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    
    @CustomTransactional
    public UserCouponResponse downloadCoupon(Long userId, DownloadCouponRequest request) {
        // 쿠폰 코드로 쿠폰 조회
        Coupon coupon = couponRepository.findByCode(request.getCouponCode())
                .orElseThrow(() -> new CouponNotFoundException("존재하지 않는 쿠폰 코드입니다."));
        
        // 쿠폰 다운로드 가능 여부 확인
        validateCouponDownloadable(userId, coupon);
        
        // 사용자 쿠폰 생성
        UserCoupon userCoupon = UserCoupon.builder()
                .userId(userId)
                .couponId(coupon.getId())
                .status(CouponStatus.AVAILABLE)
                .issuedAt(LocalDateTime.now())
                .build();
        
        UserCoupon savedUserCoupon = userCouponRepository.save(userCoupon);
        
        return UserCouponResponse.from(savedUserCoupon);
    }
    
    @Transactional(readOnly = true)
    public List<AvailableCouponResponse> getAvailableCoupons(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        
        // 다운로드 가능한 쿠폰들 조회 (활성화 + 유효기간 내)
        List<Coupon> availableCoupons = couponRepository
                .findByIsActiveTrueAndValidFromLessThanEqualAndValidToGreaterThanEqual(now, now);
        
        // 사용자가 이미 보유한 쿠폰들 조회
        List<Long> ownedCouponIds = userCouponRepository.findByUserId(userId)
                .stream()
                .map(UserCoupon::getCouponId)
                .collect(Collectors.toList());
        
        return availableCoupons.stream()
                .map(coupon -> {
                    boolean isAlreadyOwned = ownedCouponIds.contains(coupon.getId());
                    boolean canDownload = coupon.canUse() && !isAlreadyOwned;
                    
                    return AvailableCouponResponse.from(coupon, canDownload, isAlreadyOwned);
                })
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<UserCouponResponse> getUserCoupons(Long userId) {
        List<UserCoupon> userCoupons = userCouponRepository.findByUserId(userId);
        
        // 쿠폰 정보도 함께 조회
        List<Long> couponIds = userCoupons.stream()
                .map(UserCoupon::getCouponId)
                .collect(Collectors.toList());
        
        Map<Long, Coupon> couponMap = ((List<Coupon>) couponRepository.findAllById(couponIds))
                .stream()
                .collect(Collectors.toMap(Coupon::getId, coupon -> coupon));
        
        return userCoupons.stream()
                .map(userCoupon -> {
                    Coupon coupon = couponMap.get(userCoupon.getCouponId());
                    CouponResponse couponResponse = coupon != null ? CouponResponse.from(coupon) : null;
                    return UserCouponResponse.fromWithCoupon(userCoupon, couponResponse);
                })
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<UserCouponResponse> getAvailableUserCoupons(Long userId) {
        List<UserCoupon> availableCoupons = userCouponRepository
                .findByUserIdAndStatus(userId, CouponStatus.AVAILABLE);
        
        // 쿠폰 정보도 함께 조회
        List<Long> couponIds = availableCoupons.stream()
                .map(UserCoupon::getCouponId)
                .collect(Collectors.toList());
        
        Map<Long, Coupon> couponMap = ((List<Coupon>) couponRepository.findAllById(couponIds))
                .stream()
                .collect(Collectors.toMap(Coupon::getId, coupon -> coupon));
        
        return availableCoupons.stream()
                .filter(userCoupon -> {
                    Coupon coupon = couponMap.get(userCoupon.getCouponId());
                    return coupon != null && coupon.canUse();
                })
                .map(userCoupon -> {
                    Coupon coupon = couponMap.get(userCoupon.getCouponId());
                    CouponResponse couponResponse = CouponResponse.from(coupon);
                    return UserCouponResponse.fromWithCoupon(userCoupon, couponResponse);
                })
                .collect(Collectors.toList());
    }
    
    private void validateCouponDownloadable(Long userId, Coupon coupon) {
        // 이미 보유한 쿠폰인지 확인
        if (userCouponRepository.existsByUserIdAndCouponId(userId, coupon.getId())) {
            throw new CouponAlreadyOwnedException("이미 보유중인 쿠폰입니다.");
        }
        
        // 쿠폰 사용 가능 여부 확인
        if (!coupon.canUse()) {
            throw new CouponNotAvailableException("다운로드할 수 없는 쿠폰입니다. (비활성화, 만료, 또는 사용 한도 초과)");
        }
    }
}