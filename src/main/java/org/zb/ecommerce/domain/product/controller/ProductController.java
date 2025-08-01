package org.zb.ecommerce.domain.product.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zb.ecommerce.domain.product.dto.ProductResponse;
import org.zb.ecommerce.domain.product.service.ProductService;

import java.util.List;

/**
 * 상품 관련 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    
    /**
     * 상품 상세 조회
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long productId) {
        ProductResponse response = productService.getProduct(productId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 전체 상품 조회
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> responses = productService.getAllProducts();
        return ResponseEntity.ok(responses);
    }
    
    /**
     * 카테고리별 상품 조회
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(@PathVariable String category) {
        List<ProductResponse> responses = productService.getProductsByCategory(category);
        return ResponseEntity.ok(responses);
    }
    
    /**
     * 상품 검색
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam String keyword) {
        List<ProductResponse> responses = productService.searchProducts(keyword);
        return ResponseEntity.ok(responses);
    }
}
