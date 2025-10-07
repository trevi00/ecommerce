package org.zb.ecommerce.domain.cart.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zb.ecommerce.domain.cart.dto.AddToCartRequest;
import org.zb.ecommerce.domain.cart.dto.CartResponse;
import org.zb.ecommerce.domain.cart.dto.UpdateCartItemRequest;
import org.zb.ecommerce.domain.cart.service.CartService;
import org.zb.ecommerce.global.auth.AuthUser;
import org.zb.ecommerce.global.auth.LoginRequired;

/**
 * 장바구니 관련 API Controller
 */
@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {
    
    private final CartService cartService;
    
    /**
     * 장바구니에 상품 추가
     */
    @PostMapping("/items")
    @LoginRequired
    public ResponseEntity<CartResponse> addToCart(@AuthUser Long userId,
                                                 @RequestBody AddToCartRequest request) {
        CartResponse response = cartService.addToCart(userId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 장바구니 조회
     */
    @GetMapping
    @LoginRequired
    public ResponseEntity<CartResponse> getCart(@AuthUser Long userId) {
        CartResponse response = cartService.getCart(userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 장바구니 상품 수량 변경
     */
    @PutMapping("/items/{productId}")
    @LoginRequired
    public ResponseEntity<CartResponse> updateCartItem(@AuthUser Long userId,
                                                      @PathVariable Long productId,
                                                      @RequestBody UpdateCartItemRequest request) {
        CartResponse response = cartService.updateCartItem(userId, productId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 장바구니에서 상품 제거
     */
    @DeleteMapping("/items/{productId}")
    @LoginRequired
    public ResponseEntity<Void> removeCartItem(@AuthUser Long userId,
                                              @PathVariable Long productId) {
        cartService.removeCartItem(userId, productId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 장바구니 비우기
     */
    @DeleteMapping
    @LoginRequired
    public ResponseEntity<Void> clearCart(@AuthUser Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 장바구니 존재 여부 확인
     */
    @GetMapping("/exists")
    @LoginRequired
    public ResponseEntity<Boolean> hasCart(@AuthUser Long userId) {
        boolean hasCart = cartService.hasCart(userId);
        return ResponseEntity.ok(hasCart);
    }
}