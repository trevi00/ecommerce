# 🚀 이커머스 프로젝트 초기화 가이드

## 📋 개요
이 가이드는 이커머스 프로젝트를 처음 실행할 때 필요한 데이터베이스 설정과 초기 데이터 구성 방법을 설명합니다.

## 🗄️ 데이터베이스 설정

### 1. Docker를 사용한 MySQL 설정 (권장)

```bash
# Docker Compose로 MySQL과 Redis 실행
docker-compose up -d mysql redis

# 컨테이너 상태 확인
docker ps
```

### 2. 수동 MySQL 설정

```sql
-- 데이터베이스 생성
CREATE DATABASE ecommerce CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 사용자 생성 및 권한 부여
CREATE USER 'ecommerce_app'@'%' IDENTIFIED BY 'ecommerce_pass';
GRANT ALL PRIVILEGES ON ecommerce.* TO 'ecommerce_app'@'%';
FLUSH PRIVILEGES;
```

## 🏗️ 스키마 및 데이터 초기화

### 프로파일별 초기화 방법

#### 1. MySQL 프로파일 (기본)
```bash
# application.yml의 기본 프로파일이 mysql로 설정되어 있음
./gradlew bootRun

# 또는 명시적으로 지정
./gradlew bootRun --args='--spring.profiles.active=mysql'
```

#### 2. H2 프로파일 (개발/테스트)
```bash
./gradlew bootRun --args='--spring.profiles.active=h2'
```

#### 3. Docker 프로파일
```bash
./gradlew bootRun --args='--spring.profiles.active=docker'
```

## 📊 초기 데이터 구성

### 자동 초기화 데이터

프로젝트 실행 시 다음 데이터가 자동으로 생성됩니다:

#### 👥 사용자 (6명)
- **일반 사용자 (3명)**: user1@example.com, user2@example.com, user3@example.com
- **VIP 사용자 (2명)**: vip1@example.com, vip2@example.com  
- **관리자 (1명)**: admin@example.com
- **공통 비밀번호**: `password123`

#### 📦 상품 (22개)
- **전자제품 (8개)**: MacBook Pro, iPhone 15 Pro, iPad Air 등
- **의류 (5개)**: 히트텍, 나이키 운동화, 자라 코트 등
- **도서 (5개)**: 클린 코드, 스프링 부트 책 등
- **생활용품 (4개)**: 다이슨 청소기, 샤오미 공기청정기 등

#### 🎟️ 쿠폰 (8개)
- **신규 회원용**: WELCOME2024, FIRST5000
- **VIP 전용**: VIP20OFF, VIPSPECIAL
- **시즌 쿠폰**: SUMMER2024, BLACKFRIDAY
- **카테고리별**: ELECTRONICS10, BOOK3000

#### 🛒 기타
- 사용자별 장바구니 자동 생성
- 테스트용 장바구니 아이템 일부 추가
- 사용자별 쿠폰 할당

## 🧪 테스트 실행

### 전체 테스트
```bash
./gradlew test
```

### 특정 테스트 클래스
```bash
# Repository 테스트
./gradlew test --tests "*RepositoryTest"

# Service 테스트
./gradlew test --tests "*ServiceTest"

# Controller 테스트
./gradlew test --tests "*ControllerTest"
```

### TestContainers 테스트
```bash
# TestContainers를 사용한 통합 테스트
./gradlew test --tests "*DataJdbcTest"
```

## 🔧 트러블슈팅

### MySQL 연결 오류
```bash
# MySQL 컨테이너 상태 확인
docker logs ecommerce_mysql

# 포트 충돌 확인
netstat -an | grep 3307
```

### Redis 연결 오류
```bash
# Redis 컨테이너 상태 확인
docker logs ecommerce_redis

# Redis 연결 테스트
redis-cli -h localhost -p 6380 ping
```

### JWT 토큰 오류
JWT 시크릿 키가 256비트(32바이트) 이상인지 확인하세요:
```yaml
jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-here}
```

## 🌐 API 테스트

### Postman 컬렉션 예제

#### 회원가입
```json
POST http://localhost:8080/api/users/signup
Content-Type: application/json

{
    "email": "newuser@example.com",
    "password": "password123",
    "name": "새로운사용자",
    "phone": "010-9999-9999"
}
```

#### 로그인
```json
POST http://localhost:8080/api/users/login
Content-Type: application/json

{
    "email": "user1@example.com",
    "password": "password123"
}
```

#### 상품 조회
```bash
GET http://localhost:8080/api/products
```

## 📈 모니터링

### 애플리케이션 상태 확인
```bash
# Health Check
curl http://localhost:8080/actuator/health

# 애플리케이션 정보
curl http://localhost:8080/actuator/info
```

### 데이터베이스 상태 확인
```sql
-- 테이블별 데이터 수 확인
SELECT 'users' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT 'products', COUNT(*) FROM products
UNION ALL
SELECT 'coupons', COUNT(*) FROM coupons
UNION ALL
SELECT 'carts', COUNT(*) FROM carts;
```

## 📝 추가 정보

### 프로파일별 특징
- **mysql**: 운영 환경 유사, 영구 데이터 저장
- **h2**: 빠른 개발, 메모리 데이터베이스
- **test**: 테스트 전용, TestContainers 사용
- **docker**: 컨테이너 환경, 마이크로서비스 구성

### 보안 고려사항
- 기본 비밀번호는 개발환경용입니다
- 운영환경에서는 강력한 비밀번호로 변경하세요
- JWT 시크릿 키를 환경변수로 설정하세요
- HTTPS 사용을 권장합니다

### 성능 최적화
- 적절한 인덱스가 schema.sql에 정의되어 있습니다
- 커넥션 풀 설정이 프로파일별로 최적화되어 있습니다
- Redis 캐싱이 구성되어 있습니다

---

🎉 **초기화가 완료되었습니다!** 이제 이커머스 프로젝트를 사용할 준비가 되었습니다.