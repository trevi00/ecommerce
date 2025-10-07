-- MySQL용 초기 데이터 (UTF-8 인코딩)

-- 기본 사용자 데이터 삽입 (MySQL에서는 INSERT IGNORE 사용)
INSERT IGNORE INTO users (email, password, name, phone, role) VALUES 
('user1@example.com', '$2a$10$8jKFnD.RQc9jKzM6gVt3R.sCvKr3YFp5x3wH1EHKxvL3ZqWh3aJjS', '홍길동', '010-1234-5678', 'GENERAL'),
('user2@example.com', '$2a$10$8jKFnD.RQc9jKzM6gVt3R.sCvKr3YFp5x3wH1EHKxvL3ZqWh3aJjS', '김철수', '010-2345-6789', 'GENERAL'),
('user3@example.com', '$2a$10$8jKFnD.RQc9jKzM6gVt3R.sCvKr3YFp5x3wH1EHKxvL3ZqWh3aJjS', '이영희', '010-3456-7890', 'GENERAL');

-- 추가 상품 데이터
INSERT IGNORE INTO products (name, description, price, stock_quantity) VALUES 
('스마트폰', '최신 스마트폰', 800000.00, 20),
('태블릿', 'Android 태블릿', 400000.00, 15),
('이어폰', '무선 이어폰', 120000.00, 40),
('충전기', '고속 충전기', 35000.00, 60),
('케이스', '스마트폰 케이스', 25000.00, 80);