package org.zb.ecommerce.domain.product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zb.ecommerce.domain.product.dto.ProductResponse;
import org.zb.ecommerce.domain.product.entity.Product;
import org.zb.ecommerce.domain.product.exception.ProductNotFoundException;
import org.zb.ecommerce.domain.product.repository.ProductRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 상품 비즈니스 로직을 처리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    
    private final ProductRepository productRepository;
    
    /**
     * 상품 상세 조회
     */
    public ProductResponse getProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        
        return ProductResponse.from(product);
    }
    
    /**
     * 전체 상품 조회
     */
    public List<ProductResponse> getAllProducts() {
        return ((List<Product>) productRepository.findAll()).stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 카테고리별 상품 조회
     */
    public List<ProductResponse> getProductsByCategory(String category) {
        return productRepository.findByCategory(category).stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 상품명으로 검색
     */
    public List<ProductResponse> searchProducts(String keyword) {
        return productRepository.findByNameContaining(keyword).stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 재고 확인 (내부 사용)
     */
    public Product getProductEntity(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }
    
    /**
     * 여러 상품 조회 (내부 사용)
     */
    public List<Product> getProductEntities(List<Long> productIds) {
        List<Product> products = productRepository.findByIdIn(productIds);
        if (products.size() != productIds.size()) {
            throw new ProductNotFoundException();
        }
        return products;
    }
}
