package org.zb.ecommerce.domain.product.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Product Entity 단위 테스트
 */
class ProductTest {
    
    @Nested
    @DisplayName("Product 생성")
    class ProductCreation {
        
        @Test
        @DisplayName("정상적인 Product 생성")
        void createProduct_Success() {
            // given
            String name = "노트북";
            String description = "고성능 노트북";
            BigDecimal price = new BigDecimal("1500000");
            Integer stockQuantity = 10;
            String category = "전자제품";
            
            // when
            Product product = Product.builder()
                    .name(name)
                    .description(description)
                    .price(price)
                    .stockQuantity(stockQuantity)
                    .category(category)
                    .build();
            
            // then
            assertThat(product.getName()).isEqualTo(name);
            assertThat(product.getDescription()).isEqualTo(description);
            assertThat(product.getPrice()).isEqualByComparingTo(price);
            assertThat(product.getStockQuantity()).isEqualTo(stockQuantity);
            assertThat(product.getCategory()).isEqualTo(category);
        }
        
        @Test
        @DisplayName("가격이 0일 때 예외 발생")
        void createProduct_WithZeroPrice_ThrowsException() {
            // when & then
            assertThatThrownBy(() -> Product.builder()
                    .name("노트북")
                    .price(BigDecimal.ZERO)
                    .stockQuantity(10)
                    .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("가격은 0보다 커야 합니다.");
        }
        
        @Test
        @DisplayName("재고가 음수일 때 예외 발생")
        void createProduct_WithNegativeStock_ThrowsException() {
            // when & then
            assertThatThrownBy(() -> Product.builder()
                    .name("노트북")
                    .price(new BigDecimal("1500000"))
                    .stockQuantity(-1)
                    .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("재고 수량은 0 이상이어야 합니다.");
        }
    }
    
    @Nested
    @DisplayName("재고 관리")
    class StockManagement {
        
        @Test
        @DisplayName("재고 감소 성공")
        void decreaseStock_Success() {
            // given
            Product product = Product.builder()
                    .name("노트북")
                    .price(new BigDecimal("1500000"))
                    .stockQuantity(10)
                    .build();
            
            // when
            product.decreaseStock(3);
            
            // then
            assertThat(product.getStockQuantity()).isEqualTo(7);
        }
        
        @Test
        @DisplayName("재고 부족 시 예외 발생")
        void decreaseStock_InsufficientStock_ThrowsException() {
            // given
            Product product = Product.builder()
                    .name("노트북")
                    .price(new BigDecimal("1500000"))
                    .stockQuantity(5)
                    .build();
            
            // when & then
            assertThatThrownBy(() -> product.decreaseStock(10))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("재고가 부족합니다. 현재 재고: 5");
        }
        
        @Test
        @DisplayName("재고 증가 성공")
        void increaseStock_Success() {
            // given
            Product product = Product.builder()
                    .name("노트북")
                    .price(new BigDecimal("1500000"))
                    .stockQuantity(10)
                    .build();
            
            // when
            product.increaseStock(5);
            
            // then
            assertThat(product.getStockQuantity()).isEqualTo(15);
        }
        
        @Test
        @DisplayName("재고 확인")
        void isAvailable_Success() {
            // given
            Product product = Product.builder()
                    .name("노트북")
                    .price(new BigDecimal("1500000"))
                    .stockQuantity(10)
                    .build();
            
            // when & then
            assertThat(product.isAvailable(5)).isTrue();
            assertThat(product.isAvailable(10)).isTrue();
            assertThat(product.isAvailable(11)).isFalse();
        }
    }
    
    @Nested
    @DisplayName("상품 정보 수정")
    class ProductUpdate {
        
        @Test
        @DisplayName("가격 수정 성공")
        void updatePrice_Success() {
            // given
            Product product = Product.builder()
                    .name("노트북")
                    .price(new BigDecimal("1500000"))
                    .stockQuantity(10)
                    .build();
            
            BigDecimal newPrice = new BigDecimal("1400000");
            
            // when
            product.updatePrice(newPrice);
            
            // then
            assertThat(product.getPrice()).isEqualByComparingTo(newPrice);
        }
        
        @Test
        @DisplayName("상품 정보 수정 성공")
        void updateInfo_Success() {
            // given
            Product product = Product.builder()
                    .name("노트북")
                    .price(new BigDecimal("1500000"))
                    .stockQuantity(10)
                    .category("전자제품")
                    .build();
            
            // when
            product.updateInfo("게이밍 노트북", "고사양 게이밍 노트북", "게이밍");
            
            // then
            assertThat(product.getName()).isEqualTo("게이밍 노트북");
            assertThat(product.getDescription()).isEqualTo("고사양 게이밍 노트북");
            assertThat(product.getCategory()).isEqualTo("게이밍");
        }
    }
}
