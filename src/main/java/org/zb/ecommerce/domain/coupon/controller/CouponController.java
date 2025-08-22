package org.zb.ecommerce.domain.coupon.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zb.ecommerce.domain.coupon.dto.AvailableCouponResponse;
import org.zb.ecommerce.domain.coupon.dto.DownloadCouponRequest;
import org.zb.ecommerce.domain.coupon.dto.UserCouponResponse;
import org.zb.ecommerce.domain.coupon.service.CouponService;
import org.zb.ecommerce.global.auth.AuthUser;
import org.zb.ecommerce.global.auth.LoginRequired;

import java.util.List;

/**
 * 사용자용 쿠폰 관련 API Controller
 */
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {
    
    private final CouponService couponService;
    
    /**
     * 다운로드 가능한 쿠폰 목록 조회
     */
    @GetMapping("/available")
    @LoginRequired
    public ResponseEntity<List<AvailableCouponResponse>> getAvailableCoupons(@AuthUser Long userId) {
        List<AvailableCouponResponse> response = couponService.getAvailableCoupons(userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 쿠폰 다운로드
     */
    @PostMapping("/download")
    @LoginRequired
    public ResponseEntity<UserCouponResponse> downloadCoupon(@AuthUser Long userId,
                                                            @RequestBody DownloadCouponRequest request) {
        UserCouponResponse response = couponService.downloadCoupon(userId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 내 쿠폰 목록 조회 (전체)
     */
    @GetMapping("/my")
    @LoginRequired
    public ResponseEntity<List<UserCouponResponse>> getMyCoupons(@AuthUser Long userId) {
        List<UserCouponResponse> response = couponService.getUserCoupons(userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 사용 가능한 내 쿠폰 목록 조회
     */
    @GetMapping("/my/available")
    @LoginRequired
    public ResponseEntity<List<UserCouponResponse>> getMyAvailableCoupons(@AuthUser Long userId) {
        List<UserCouponResponse> response = couponService.getAvailableUserCoupons(userId);
        return ResponseEntity.ok(response);
    }
}