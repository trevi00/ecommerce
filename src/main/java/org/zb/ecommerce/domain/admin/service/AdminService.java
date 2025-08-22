package org.zb.ecommerce.domain.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zb.ecommerce.domain.admin.dto.*;
import org.zb.ecommerce.domain.admin.repository.CouponRepository;
import org.zb.ecommerce.domain.admin.repository.UserCouponRepository;
import org.zb.ecommerce.domain.coupon.entity.Coupon;
import org.zb.ecommerce.domain.coupon.entity.CouponStatus;
import org.zb.ecommerce.domain.coupon.entity.UserCoupon;
import org.zb.ecommerce.domain.order.entity.OrderStatus;
import org.zb.ecommerce.domain.order.repository.OrderRepository;
import org.zb.ecommerce.domain.user.entity.User;
import org.zb.ecommerce.domain.user.exception.UserNotFoundException;
import org.zb.ecommerce.domain.user.repository.UserRepository;
import org.zb.ecommerce.global.annotation.CustomTransactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {
    
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    
    @CustomTransactional
    public CouponResponse createCoupon(CreateCouponRequest request) {
        validateCouponCodeUnique(request.code());
        
        Coupon coupon = Coupon.builder()
                .name(request.name())
                .code(request.code())
                .discountType(request.discountType())
                .discountValue(request.discountValue())
                .minOrderAmount(request.minOrderAmount())
                .maxDiscountAmount(request.maxDiscountAmount())
                .validFrom(request.validFrom())
                .validTo(request.validTo())
                .maxUsageCount(request.maxUsageCount())
                .build();
        
        Coupon savedCoupon = couponRepository.save(coupon);
        return CouponResponse.from(savedCoupon);
    }
    
    @Transactional(readOnly = true)
    public List<CouponResponse> getAllCoupons() {
        List<Coupon> coupons = (List<Coupon>) couponRepository.findAll();
        return coupons.stream()
                .map(CouponResponse::from)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<CouponResponse> getActiveCoupons() {
        List<Coupon> coupons = couponRepository.findByIsActiveTrue();
        return coupons.stream()
                .map(CouponResponse::from)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<CouponResponse> searchCoupons(String name) {
        List<Coupon> coupons = couponRepository.findByNameContainingIgnoreCase(name);
        return coupons.stream()
                .map(CouponResponse::from)
                .collect(Collectors.toList());
    }
    
    @CustomTransactional
    public void issueCouponToUser(IssueCouponToUserRequest request) {
        validateUserExists(request.userId());
        Coupon coupon = validateCouponExists(request.couponId());
        
        if (userCouponRepository.existsByUserIdAndCouponId(request.userId(), request.couponId())) {
            throw new IllegalArgumentException("이미 해당 쿠폰을 보유하고 있습니다.");
        }
        
        if (!coupon.canUse()) {
            throw new IllegalArgumentException("발급할 수 없는 쿠폰입니다.");
        }
        
        UserCoupon userCoupon = UserCoupon.builder()
                .userId(request.userId())
                .couponId(request.couponId())
                .status(CouponStatus.AVAILABLE)
                .issuedAt(LocalDateTime.now())
                .build();
        
        userCouponRepository.save(userCoupon);
    }
    
    @CustomTransactional
    public void deactivateCoupon(Long couponId) {
        Coupon coupon = validateCouponExists(couponId);
        coupon.deactivate();
        couponRepository.save(coupon);
    }
    
    @CustomTransactional
    public void activateCoupon(Long couponId) {
        Coupon coupon = validateCouponExists(couponId);
        coupon.activate();
        couponRepository.save(coupon);
    }
    
    @Transactional(readOnly = true)
    public List<UserManagementResponse> getAllUsers() {
        List<User> users = (List<User>) userRepository.findAll();
        return users.stream()
                .map(UserManagementResponse::from)
                .collect(Collectors.toList());
    }
    
    @CustomTransactional
    public UserManagementResponse updateUserRole(Long userId, UpdateUserRoleRequest request) {
        User user = validateUserExists(userId);
        user.updateRole(request.role());
        User savedUser = userRepository.save(user);
        return UserManagementResponse.from(savedUser);
    }
    
    @Transactional(readOnly = true)
    public List<UserManagementResponse> searchUsers(String keyword) {
        List<User> users = userRepository.findByEmailContainingOrNameContaining(keyword, keyword);
        return users.stream()
                .map(UserManagementResponse::from)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboardData() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        
        long totalUsers = userRepository.count();
        long totalCoupons = couponRepository.count();
        long activeCoupons = couponRepository.findByIsActiveTrue().size();
        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING).size();
        
        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalCoupons(totalCoupons)
                .activeCoupons(activeCoupons)
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .build();
    }
    
    private void validateCouponCodeUnique(String code) {
        if (couponRepository.existsByCode(code)) {
            throw new IllegalArgumentException("이미 존재하는 쿠폰 코드입니다.");
        }
    }
    
    private Coupon validateCouponExists(Long couponId) {
        return couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));
    }
    
    private User validateUserExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
    }
}