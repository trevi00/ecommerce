# E-commerce Project

Spring Boot 기반의 이커머스 프로젝트입니다.

## 기술 스택

- Java 21
- Spring Boot 3.5.4
- Spring Data JDBC
- MySQL 8.0
- Redis 7
- Docker & Docker Compose

## 프로젝트 구조

```
src/
├── main/
│   ├── java/
│   │   └── org/zb/ecommerce/
│   │       ├── domain/
│   │       │   ├── common/      # 공통 Entity
│   │       │   ├── user/        # 사용자 도메인
│   │       │   ├── product/     # 상품 도메인
│   │       │   ├── order/       # 주문 도메인
│   │       │   ├── payment/     # 결제 도메인
│   │       │   ├── cart/        # 장바구니 도메인
│   │       │   └── coupon/      # 쿠폰 도메인
│   │       └── EcommerceApplication.java
│   └── resources/
│       ├── application.yml      # 애플리케이션 설정
│       ├── schema.sql          # DB 스키마
│       └── data.sql            # 테스트 데이터
└── test/
    └── java/
        └── org/zb/ecommerce/   # 테스트 코드
```

## 개발 환경 설정

### 1. Docker 환경 실행

#### Linux/Mac
```bash
chmod +x docker-start.sh docker-stop.sh
./docker-start.sh
```

#### Windows
```cmd
docker-start.bat
```

### 2. Docker 환경 중지

#### Linux/Mac
```bash
./docker-stop.sh
```

#### Windows
```cmd
docker-stop.bat
```

### 3. 데이터베이스 접속 정보

- **MySQL**
  - Host: localhost:3306
  - Database: ecommerce_db
  - Username: ecommerce
  - Password: ecommerce1234

- **Redis**
  - Host: localhost:6379

## 개발 우선순위

1. **Order** (주문)
   - 상품 상세
   - 주문 생성/변경/취소
   - 쿠폰 적용
   - 장바구니에서 주문

2. **User** (사용자)
   - 회원가입/로그인/탈퇴
   - 쿠폰 다운로드/사용
   - 결제

3. **Product** (상품)
   - 상품 CRUD

4. **Payments** (결제)
   - 결제/취소
   - 결제 내역 조회

5. **Cart** (장바구니)
   - 장바구니 추가/조회
   - 상품 변경/삭제

6. **Admin** (관리자)
   - 쿠폰 관리
   - 사용자 등급 관리

## 주요 기능

- DDD(Domain-Driven Design) 적용
- TDD(Test-Driven Development) 적용
- Spring Data JDBC 사용
- Redis 캐싱 (TTL 5분)
- Git Flow 브랜치 전략

## 실행 방법

1. Docker 환경 실행
2. Gradle 빌드
   ```bash
   ./gradlew clean build
   ```
3. 애플리케이션 실행
   ```bash
   ./gradlew bootRun
   ```

## API 문서

(추후 Swagger 또는 REST Docs 추가 예정)

## 테스트

```bash
./gradlew test
```
