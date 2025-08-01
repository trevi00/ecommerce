-- 테스트용 사용자 데이터
INSERT INTO users (email, password, name, phone, role) VALUES
('user1@example.com', '$2a$10$8jKFnD.RQc9jKzM6gVt3R.sCvKr3YFp5x3wH1EHKxvL3ZqWh3aJjS', '홍길동', '010-1234-5678', 'GENERAL'),
('user2@example.com', '$2a$10$8jKFnD.RQc9jKzM6gVt3R.sCvKr3YFp5x3wH1EHKxvL3ZqWh3aJjS', '김철수', '010-2345-6789', 'VIP'),
('user3@example.com', '$2a$10$8jKFnD.RQc9jKzM6gVt3R.sCvKr3YFp5x3wH1EHKxvL3ZqWh3aJjS', '이영희', '010-3456-7890', 'GENERAL');

-- 테스트용 상품 데이터
INSERT INTO products (name, description, price, stock_quantity, category) VALUES
('노트북', '고성능 게이밍 노트북', 1500000.00, 10, '전자제품'),
('키보드', '기계식 키보드', 150000.00, 50, '전자제품'),
('마우스', '무선 게이밍 마우스', 80000.00, 100, '전자제품'),
('모니터', '27인치 4K 모니터', 450000.00, 20, '전자제품'),
('책상', '높이 조절 가능한 책상', 300000.00, 15, '가구'),
('의자', '인체공학적 사무용 의자', 250000.00, 25, '가구');

-- 테스트용 쿠폰 데이터
INSERT INTO coupons (name, code, discount_type, discount_value, min_order_amount, max_discount_amount, valid_from, valid_to, max_usage_count) VALUES
('신규 가입 쿠폰', 'WELCOME2024', 'PERCENTAGE', 10.00, 50000.00, 10000.00, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 1000),
('VIP 전용 쿠폰', 'VIP20OFF', 'PERCENTAGE', 20.00, 100000.00, 50000.00, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 500),
('정액 할인 쿠폰', 'SAVE10000', 'FIXED_AMOUNT', 10000.00, 50000.00, NULL, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 200);

-- 테스트용 장바구니 데이터
INSERT INTO carts (user_id) VALUES
(1),
(2),
(3);

-- 테스트용 장바구니 아이템 데이터
INSERT INTO cart_items (cart_id, product_id, quantity) VALUES
(1, 1, 1),
(1, 2, 2),
(2, 3, 1),
(2, 4, 1);

-- 테스트용 사용자 쿠폰 데이터
INSERT INTO user_coupons (user_id, coupon_id, status) VALUES
(1, 1, 'AVAILABLE'),
(2, 2, 'AVAILABLE'),
(2, 3, 'AVAILABLE');
