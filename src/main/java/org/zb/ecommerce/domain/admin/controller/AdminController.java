package org.zb.ecommerce.domain.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zb.ecommerce.domain.admin.dto.*;
import org.zb.ecommerce.domain.admin.service.AdminService;
import org.zb.ecommerce.domain.user.dto.UserResponse;
import org.zb.ecommerce.domain.user.entity.UserRole;
import org.zb.ecommerce.domain.user.service.UserService;
import org.zb.ecommerce.global.auth.AuthUser;
import org.zb.ecommerce.global.auth.LoginRequired;
import org.zb.ecommerce.global.exception.BusinessException;
import org.zb.ecommerce.global.exception.ErrorCode;

import java.util.List;

/**
 * 관리자 기능 API Controller
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final AdminService adminService;
    private final UserService userService;
    
    /**
     * 관리자 대시보드 데이터 조회
     */
    @GetMapping("/dashboard")
    @LoginRequired
    public ResponseEntity<AdminDashboardResponse> getDashboard(@AuthUser Long userId) {
        validateAdminRole(userId);
        AdminDashboardResponse response = adminService.getDashboardData();
        return ResponseEntity.ok(response);
    }
    
    /**
     * 쿠폰 생성
     */
    @PostMapping("/coupons")
    @LoginRequired
    public ResponseEntity<CouponResponse> createCoupon(@AuthUser Long userId,
                                                      @RequestBody CreateCouponRequest request) {
        validateAdminRole(userId);
        CouponResponse response = adminService.createCoupon(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 모든 쿠폰 조회
     */
    @GetMapping("/coupons")
    @LoginRequired
    public ResponseEntity<List<CouponResponse>> getAllCoupons(@AuthUser Long userId) {
        validateAdminRole(userId);
        List<CouponResponse> response = adminService.getAllCoupons();
        return ResponseEntity.ok(response);
    }
    
    /**
     * 활성화된 쿠폰 조회
     */
    @GetMapping("/coupons/active")
    @LoginRequired
    public ResponseEntity<List<CouponResponse>> getActiveCoupons(@AuthUser Long userId) {
        validateAdminRole(userId);
        List<CouponResponse> response = adminService.getActiveCoupons();
        return ResponseEntity.ok(response);
    }
    
    /**
     * 쿠폰 검색
     */
    @GetMapping("/coupons/search")
    @LoginRequired
    public ResponseEntity<List<CouponResponse>> searchCoupons(@AuthUser Long userId,
                                                             @RequestParam String name) {
        validateAdminRole(userId);
        List<CouponResponse> response = adminService.searchCoupons(name);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 사용자에게 쿠폰 발급
     */
    @PostMapping("/coupons/issue")
    @LoginRequired
    public ResponseEntity<Void> issueCouponToUser(@AuthUser Long userId,
                                                 @RequestBody IssueCouponToUserRequest request) {
        validateAdminRole(userId);
        adminService.issueCouponToUser(request);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 쿠폰 비활성화
     */
    @PutMapping("/coupons/{couponId}/deactivate")
    @LoginRequired
    public ResponseEntity<Void> deactivateCoupon(@AuthUser Long userId,
                                                @PathVariable Long couponId) {
        validateAdminRole(userId);
        adminService.deactivateCoupon(couponId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 쿠폰 활성화
     */
    @PutMapping("/coupons/{couponId}/activate")
    @LoginRequired
    public ResponseEntity<Void> activateCoupon(@AuthUser Long userId,
                                              @PathVariable Long couponId) {
        validateAdminRole(userId);
        adminService.activateCoupon(couponId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 모든 사용자 조회
     */
    @GetMapping("/users")
    @LoginRequired
    public ResponseEntity<List<UserManagementResponse>> getAllUsers(@AuthUser Long userId) {
        validateAdminRole(userId);
        List<UserManagementResponse> response = adminService.getAllUsers();
        return ResponseEntity.ok(response);
    }
    
    /**
     * 사용자 역할 변경
     */
    @PutMapping("/users/{targetUserId}/role")
    @LoginRequired
    public ResponseEntity<UserManagementResponse> updateUserRole(@AuthUser Long userId,
                                                                @PathVariable Long targetUserId,
                                                                @RequestBody UpdateUserRoleRequest request) {
        validateAdminRole(userId);
        UserManagementResponse response = adminService.updateUserRole(targetUserId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 사용자 검색
     */
    @GetMapping("/users/search")
    @LoginRequired
    public ResponseEntity<List<UserManagementResponse>> searchUsers(@AuthUser Long userId,
                                                                   @RequestParam String keyword) {
        validateAdminRole(userId);
        List<UserManagementResponse> response = adminService.searchUsers(keyword);
        return ResponseEntity.ok(response);
    }
    
    private void validateAdminRole(Long userId) {
        UserResponse user = userService.findById(userId);
        if (user.role() != UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }
}