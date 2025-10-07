package org.zb.ecommerce.domain.cart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zb.ecommerce.domain.cart.dto.AddToCartRequest;
import org.zb.ecommerce.domain.cart.dto.CartResponse;
import org.zb.ecommerce.domain.cart.dto.UpdateCartItemRequest;
import org.zb.ecommerce.domain.cart.entity.Cart;
import org.zb.ecommerce.domain.cart.entity.CartItem;
import org.zb.ecommerce.domain.cart.exception.CartNotFoundException;
import org.zb.ecommerce.domain.cart.repository.CartRepository;
import org.zb.ecommerce.domain.cart.repository.CartItemRepository;
import org.zb.ecommerce.domain.product.exception.ProductNotFoundException;
import org.zb.ecommerce.domain.product.repository.ProductRepository;
import org.zb.ecommerce.global.annotation.CustomTransactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    
    @CustomTransactional
    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        validateProductExists(request.productId());
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createAndSaveNewCart(userId));
        
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), request.productId());
        
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.increaseQuantity(request.quantity());
            cartItemRepository.save(item);
        } else {
            CartItem newItem = CartItem.builder()
                    .cartId(cart.getId())
                    .productId(request.productId())
                    .quantity(request.quantity())
                    .build();
            cartItemRepository.save(newItem);
        }
        
        return CartResponse.from(cart, cartItemRepository.findByCartId(cart.getId()));
    }
    
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("사용자의 장바구니를 찾을 수 없습니다."));
        
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        return CartResponse.from(cart, cartItems);
    }
    
    @CustomTransactional
    public CartResponse updateCartItem(Long userId, Long productId, UpdateCartItemRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("사용자의 장바구니를 찾을 수 없습니다."));
        
        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니에 해당 상품이 없습니다."));
        
        cartItem.updateQuantity(request.quantity());
        cartItemRepository.save(cartItem);
        
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        return CartResponse.from(cart, cartItems);
    }
    
    @CustomTransactional
    public void removeCartItem(Long userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("사용자의 장바구니를 찾을 수 없습니다."));
        
        cartItemRepository.deleteByCartIdAndProductId(cart.getId(), productId);
    }
    
    @CustomTransactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("사용자의 장바구니를 찾을 수 없습니다."));
        
        cartItemRepository.deleteByCartId(cart.getId());
    }
    
    @Transactional(readOnly = true)
    public boolean hasCart(Long userId) {
        return cartRepository.existsByUserId(userId);
    }
    
    private Cart createAndSaveNewCart(Long userId) {
        Cart cart = Cart.builder()
                .userId(userId)
                .build();
        return cartRepository.save(cart);
    }
    
    private void validateProductExists(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException();
        }
    }
}