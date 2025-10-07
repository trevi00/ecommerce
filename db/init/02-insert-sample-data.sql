-- 샘플 데이터 삽입 스크립트

-- 관리자 사용자 생성 (비밀번호: admin123)
INSERT INTO users (email, password, name, phone, role) VALUES
('admin@ecommerce.com', '$2a$10$N8gKwW7zYwQpj2BVR8RlIeShPXFyZnDLLMDgF.7YhWKJqfIgxjKvW', '관리자', '010-0000-0000', 'ADMIN');

-- 일반 사용자 생성 (비밀번호: user123)
INSERT INTO users (email, password, name, phone, role) VALUES
('user1@example.com', '$2a$10$N8gKwW7zYwQpj2BVR8RlIeShPXFyZnDLLMDgF.7YhWKJqfIgxjKvW', '김철수', '010-1234-5678', 'GENERAL'),
('user2@example.com', '$2a$10$N8gKwW7zYwQpj2BVR8RlIeShPXFyZnDLLMDgF.7YhWKJqfIgxjKvW', '이영희', '010-9876-5432', 'GENERAL'),
('user3@example.com', '$2a$10$N8gKwW7zYwQpj2BVR8RlIeShPXFyZnDLLMDgF.7YhWKJqfIgxjKvW', '박민수', '010-1111-2222', 'GENERAL');

-- 카테고리별 상품 데이터
-- 전자제품
INSERT INTO products (name, description, price, stock_quantity) VALUES
('MacBook Pro 16인치', '애플 맥북프로 16인치 M3 Max 칩 탑재', 4890000.00, 15),
('iPad Air', '10.9인치 iPad Air WiFi 256GB', 929000.00, 25),
('iPhone 15 Pro', '아이폰 15 Pro 128GB 내추럴 티타늄', 1550000.00, 30),
('AirPods Pro 2세대', '액티브 노이즈 캔슬링 무선 이어폰', 359000.00, 50),
('Apple Watch Series 9', '45mm GPS + Cellular 모델', 649000.00, 20);

-- 의류
INSERT INTO products (name, description, price, stock_quantity) VALUES
('나이키 에어맥스 270', '남성용 러닝화 블랙/화이트', 159000.00, 40),
('아디다스 울트라부스트 22', '여성용 러닝화 화이트/그레이', 189000.00, 35),
('유니클로 히트텍 내복 세트', '극세모 발열 내복 상하 세트', 29900.00, 100),
('자라 울 코트', '여성용 울 블렌드 롱 코트 네이비', 129000.00, 15),
('리바이스 501 청바지', '오리지널 스트레이트 핏 데님', 89000.00, 60);

-- 생활용품
INSERT INTO products (name, description, price, stock_quantity) VALUES
('다이슨 V15 무선청소기', '레이저 먼지 감지 기능 탑재', 899000.00, 12),
('필립스 에어프라이어', '6.2L 대용량 오일없는 튀김기', 298000.00, 20),
('샤오미 공기청정기 4', '99.97% 미세먼지 제거', 199000.00, 25),
('아이로봇 룸바 i7+', '자동 먼지 배출 로봇청소기', 999000.00, 8),
('브리타 정수기', '5단계 필터링 직수형 정수기', 159000.00, 30);

-- 도서
INSERT INTO products (name, description, price, stock_quantity) VALUES
('Clean Code', '로버트 마틴의 클린 코드', 32000.00, 50),
('이펙티브 자바', '조슈아 블로크의 자바 프로그래밍', 36000.00, 40),
('스프링 부트 실전 가이드', '실무에서 바로 쓰는 스프링 부트', 28000.00, 45),
('알고리즘 문제 해결 전략', '프로그래밍 대회에서 배우는 알고리즘', 45000.00, 30),
('데이터베이스 개론', '관계형 데이터베이스의 이론과 실무', 35000.00, 25);

-- 쿠폰 데이터
INSERT INTO coupons (name, discount_type, discount_value, min_order_amount, status, valid_from, valid_until) VALUES
('신규가입 축하 쿠폰', 'FIXED', 10000.00, 50000.00, 'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
('10% 할인 쿠폰', 'PERCENTAGE', 10.00, 100000.00, 'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY)),
('무료배송 쿠폰', 'FIXED', 3000.00, 30000.00, 'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 14 DAY)),
('VIP 회원 특가', 'PERCENTAGE', 15.00, 200000.00, 'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 60 DAY)),
('봄맞이 특별 할인', 'FIXED', 20000.00, 150000.00, 'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 21 DAY));

-- 사용자별 쿠폰 발급
INSERT INTO user_coupons (user_id, coupon_id, is_used) VALUES
(2, 1, FALSE),  -- user1에게 신규가입 축하 쿠폰
(2, 3, FALSE),  -- user1에게 무료배송 쿠폰
(3, 1, FALSE),  -- user2에게 신규가입 축하 쿠폰
(3, 2, FALSE),  -- user2에게 10% 할인 쿠폰
(4, 1, FALSE),  -- user3에게 신규가입 축하 쿠폰
(4, 4, FALSE);  -- user3에게 VIP 회원 특가

-- 장바구니 생성
INSERT INTO carts (user_id) VALUES (2), (3), (4);

-- 장바구니에 상품 추가
INSERT INTO cart_items (cart_id, product_id, quantity) VALUES
-- user1의 장바구니
(1, 1, 1),  -- MacBook Pro
(1, 4, 2),  -- AirPods Pro
-- user2의 장바구니
(2, 6, 1),  -- 나이키 에어맥스
(2, 8, 1),  -- 유니클로 히트텍
(2, 16, 2), -- Clean Code
-- user3의 장바구니
(3, 11, 1), -- 다이슨 청소기
(3, 13, 1); -- 샤오미 공기청정기