
-- 일반 사용자
INSERT IGNORE INTO users (email, password, name, phone, role) VALUES 
('user1@example.com', '$2a$10$8jKFnD.RQc9jKzM6gVt3R.sCvKr3YFp5x3wH1EHKxvL3ZqWh3aJjS', '홍길동', '010-1234-5678', 'GENERAL'),
('user2@example.com', '$2a$10$8jKFnD.RQc9jKzM6gVt3R.sCvKr3YFp5x3wH1EHKxvL3ZqWh3aJjS', '김철수', '010-2345-6789', 'GENERAL'),
('user3@example.com', '$2a$10$8jKFnD.RQc9jKzM6gVt3R.sCvKr3YFp5x3wH1EHKxvL3ZqWh3aJjS', '이영희', '010-3456-7890', 'GENERAL');

-- VIP 사용자
INSERT IGNORE INTO users (email, password, name, phone, role) VALUES 
('vip1@example.com', '$2a$10$8jKFnD.RQc9jKzM6gVt3R.sCvKr3YFp5x3wH1EHKxvL3ZqWh3aJjS', '박민수', '010-4567-8901', 'VIP'),
('vip2@example.com', '$2a$10$8jKFnD.RQc9jKzM6gVt3R.sCvKr3YFp5x3wH1EHKxvL3ZqWh3aJjS', '정수진', '010-5678-9012', 'VIP');

-- 관리자 사용자
INSERT IGNORE INTO users (email, password, name, phone, role) VALUES 
('admin@example.com', '$2a$10$8jKFnD.RQc9jKzM6gVt3R.sCvKr3YFp5x3wH1EHKxvL3ZqWh3aJjS', '관리자', '010-0000-0000', 'ADMIN');

-- =============================================
-- 2. 상품 카테고리별 데이터
-- =============================================

-- 전자제품
INSERT IGNORE INTO products (name, description, price, stock_quantity, category) VALUES
('MacBook Pro 14인치', 'M3 Pro 칩셋, 18GB 메모리, 512GB SSD', 2690000.00, 15, '전자제품'),
('iPhone 15 Pro', '128GB, 티타늄 블루', 1550000.00, 30, '전자제품'),
('iPad Air', '10.9인치, 256GB, Wi-Fi', 899000.00, 25, '전자제품'),
('AirPods Pro 2세대', '액티브 노이즈 캔슬링', 359000.00, 50, '전자제품'),
('Magic Keyboard', 'MacBook Pro 14인치용', 429000.00, 20, '전자제품'),
('삼성 갤럭시 S24 Ultra', '256GB, 티타늄 그레이', 1698000.00, 20, '전자제품'),
('LG 그램 17인치', 'Intel i7, 16GB RAM, 1TB SSD', 2100000.00, 12, '전자제품'),
('소니 WH-1000XM5', '무선 노이즈 캔슬링 헤드폰', 449000.00, 35, '전자제품');

-- 의류
INSERT IGNORE INTO products (name, description, price, stock_quantity, category) VALUES
('유니클로 히트텍 이너웨어', '남성용 긴팔, L사이즈', 19900.00, 100, '의류'),
('나이키 에어맥스 270', '운동화, 280mm', 169000.00, 40, '의류'),
('자라 울 코트', '여성용 롱 코트, M사이즈', 199000.00, 25, '의류'),
('리바이스 501 청바지', '남성용, 32x32', 128000.00, 60, '의류'),
('아디다스 후디', '기본 후드티, L사이즈', 79000.00, 80, '의류');

-- 도서
INSERT IGNORE INTO products (name, description, price, stock_quantity, category) VALUES
('클린 코드', '로버트 C. 마틴 저, 프로그래밍 필독서', 33000.00, 50, '도서'),
('스프링 부트와 AWS로 혼자 구현하는 웹 서비스', '이동욱 저', 27000.00, 30, '도서'),
('자바의 정석', '남궁성 저, 3판', 30000.00, 40, '도서'),
('토비의 스프링 3.1', '이일민 저', 45000.00, 20, '도서'),
('리팩토링', '마틴 파울러 저, 2판', 35000.00, 25, '도서');

-- 생활용품
INSERT IGNORE INTO products (name, description, price, stock_quantity, category) VALUES
('다이슨 V15 디텍트', '무선 청소기', 899000.00, 15, '생활용품'),
('샤오미 공기청정기 4', '미에어 4', 429000.00, 20, '생활용품'),
('브루비 정수기', '직수형 냉온정수기', 599000.00, 10, '생활용품'),
('템퍼 매트리스', '싱글 사이즈', 1200000.00, 8, '생활용품');

-- =============================================
-- 3. 쿠폰 데이터
-- =============================================

-- 신규 회원용 쿠폰
INSERT IGNORE INTO coupons (name, code, discount_type, discount_value, min_order_amount, max_discount_amount, valid_from, valid_to, max_usage_count) VALUES
('신규 가입 축하 쿠폰', 'WELCOME2024', 'PERCENTAGE', 10.00, 50000.00, 10000.00, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 1000),
('첫 구매 5천원 할인', 'FIRST5000', 'FIXED_AMOUNT', 5000.00, 30000.00, NULL, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 500);

-- VIP 전용 쿠폰
INSERT IGNORE INTO coupons (name, code, discount_type, discount_value, min_order_amount, max_discount_amount, valid_from, valid_to, max_usage_count) VALUES
('VIP 전용 20% 할인', 'VIP20OFF', 'PERCENTAGE', 20.00, 100000.00, 50000.00, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 200),
('VIP 고액 할인', 'VIPSPECIAL', 'FIXED_AMOUNT', 30000.00, 200000.00, NULL, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 100);

-- 시즌 쿠폰
INSERT IGNORE INTO coupons (name, code, discount_type, discount_value, min_order_amount, max_discount_amount, valid_from, valid_to, max_usage_count) VALUES
('여름 시즌 할인', 'SUMMER2024', 'PERCENTAGE', 15.00, 80000.00, 20000.00, '2024-06-01 00:00:00', '2024-08-31 23:59:59', 300),
('블랙프라이데이', 'BLACKFRIDAY', 'PERCENTAGE', 25.00, 150000.00, 100000.00, '2024-11-25 00:00:00', '2024-11-30 23:59:59', 1000);

-- 카테고리별 쿠폰
INSERT IGNORE INTO coupons (name, code, discount_type, discount_value, min_order_amount, max_discount_amount, valid_from, valid_to, max_usage_count) VALUES
('전자제품 특가', 'ELECTRONICS10', 'PERCENTAGE', 10.00, 100000.00, 30000.00, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 200),
('도서 할인 쿠폰', 'BOOK3000', 'FIXED_AMOUNT', 3000.00, 20000.00, NULL, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 400);

-- =============================================
-- 4. 사용자별 장바구니 초기화
-- =============================================

-- 각 사용자의 기본 장바구니 생성
INSERT IGNORE INTO carts (user_id) VALUES
(1), (2), (3), (4), (5), (6);

-- =============================================
-- 5. 테스트용 장바구니 아이템 (선택사항)
-- =============================================

-- 사용자 1의 장바구니
INSERT IGNORE INTO cart_items (cart_id, product_id, quantity) VALUES
(1, 1, 1),  -- MacBook Pro
(1, 4, 2);  -- AirPods Pro

-- 사용자 2의 장바구니  
INSERT IGNORE INTO cart_items (cart_id, product_id, quantity) VALUES
(2, 9, 2),  -- 히트텍
(2, 15, 1); -- 클린 코드

-- =============================================
-- 6. 사용자 쿠폰 할당
-- =============================================

-- 일반 사용자들에게 신규 가입 쿠폰 할당
INSERT IGNORE INTO user_coupons (user_id, coupon_id, status) VALUES
(1, 1, 'AVAILABLE'),
(2, 1, 'AVAILABLE'), 
(3, 1, 'AVAILABLE');

-- VIP 사용자들에게 VIP 쿠폰 할당
INSERT IGNORE INTO user_coupons (user_id, coupon_id, status) VALUES
(4, 3, 'AVAILABLE'),
(4, 4, 'AVAILABLE'),
(5, 3, 'AVAILABLE'),
(5, 4, 'AVAILABLE');

-- 일부 사용자에게 첫 구매 할인 쿠폰 할당
INSERT IGNORE INTO user_coupons (user_id, coupon_id, status) VALUES
(1, 2, 'AVAILABLE'),
(2, 2, 'AVAILABLE');

-- =============================================
-- 초기화 완료 메시지
-- =============================================
-- 이 시점에서 다음 데이터가 설정됩니다:
-- - 6명의 사용자 (일반 3명, VIP 2명, 관리자 1명)
-- - 22개의 다양한 카테고리 상품
-- - 8개의 다양한 할인 쿠폰
-- - 사용자별 장바구니 및 일부 장바구니 아이템
-- - 사용자별 쿠폰 할당

-- 비밀번호 정보: 모든 사용자의 비밀번호는 'password123' 입니다.
-- (BCrypt로 암호화된 값: $2a$10$8jKFnD.RQc9jKzM6gVt3R.sCvKr3YFp5x3wH1EHKxvL3ZqWh3aJjS)

SELECT 'Database initialization completed successfully!' as result;