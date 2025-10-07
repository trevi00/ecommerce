package org.zb.ecommerce.domain.cart.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.zb.ecommerce.config.BaseIntegrationTest;
import org.zb.ecommerce.domain.cart.entity.Cart;
import org.zb.ecommerce.domain.cart.entity.CartItem;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CartRepositoryTest extends BaseIntegrationTest {
    
    @Autowired
    private CartRepository cartRepository;
    
    @Test
    @DisplayName("사용자 ID로 장바구니 조회 성공")
    void findByUserId_Success() {
        // given
        Long userId = 1L;
        Cart cart = Cart.builder()
                .userId(userId)
                .build();
        Cart savedCart = cartRepository.save(cart);
        
        // when
        Optional<Cart> result = cartRepository.findByUserId(userId);
        
        // then
        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(userId);
        assertThat(result.get().getId()).isEqualTo(savedCart.getId());
    }
    
    @Test
    @DisplayName("존재하지 않는 사용자 ID로 장바구니 조회 시 빈 결과 반환")
    void findByUserId_NotFound() {
        // given
        Long nonExistentUserId = 999L;
        
        // when
        Optional<Cart> result = cartRepository.findByUserId(nonExistentUserId);
        
        // then
        assertThat(result).isEmpty();
    }
    
    @Test
    @DisplayName("사용자의 장바구니 존재 여부 확인 - 존재하는 경우")
    void existsByUserId_True() {
        // given
        Long userId = 1L;
        Cart cart = Cart.builder()
                .userId(userId)
                .build();
        cartRepository.save(cart);
        
        // when
        boolean exists = cartRepository.existsByUserId(userId);
        
        // then
        assertThat(exists).isTrue();
    }
    
    @Test
    @DisplayName("사용자의 장바구니 존재 여부 확인 - 존재하지 않는 경우")
    void existsByUserId_False() {
        // given
        Long nonExistentUserId = 999L;
        
        // when
        boolean exists = cartRepository.existsByUserId(nonExistentUserId);
        
        // then
        assertThat(exists).isFalse();
    }
    
    @Test
    @DisplayName("사용자별 장바구니 생성 및 조회")  
    void save_Cart_Success() {
        // given
        Long userId1 = 1L;
        Long userId2 = 2L;
        Cart cart1 = Cart.builder().userId(userId1).build();
        Cart cart2 = Cart.builder().userId(userId2).build();
        
        // when
        cartRepository.save(cart1);
        cartRepository.save(cart2);
        
        // then
        Optional<Cart> foundCart1 = cartRepository.findByUserId(userId1);
        Optional<Cart> foundCart2 = cartRepository.findByUserId(userId2);
        
        assertThat(foundCart1).isPresent();
        assertThat(foundCart1.get().getUserId()).isEqualTo(userId1);
        
        assertThat(foundCart2).isPresent();
        assertThat(foundCart2.get().getUserId()).isEqualTo(userId2);
    }
    
    @Test
    @DisplayName("장바구니 삭제 확인")
    void delete_Cart_Success() {
        // given
        Long userId = 1L;
        Cart cart = Cart.builder().userId(userId).build();
        Cart savedCart = cartRepository.save(cart);
        
        // when
        cartRepository.delete(savedCart);
        
        // then
        Optional<Cart> deletedCart = cartRepository.findByUserId(userId);
        assertThat(deletedCart).isEmpty();
        assertThat(cartRepository.existsByUserId(userId)).isFalse();
    }
}