# MySQL 설정 및 실행 가이드

### 1. Docker Compose로 MySQL 및 Redis 실행
```bash
# MySQL과 Redis 컨테이너 실행
docker-compose up -d

# 컨테이너 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs -f mysql
```

### 2. 애플리케이션 실행
```bash
# MySQL 프로파일로 애플리케이션 실행
./gradlew bootRun

# 또는 IDE에서 실행 시 VM Options 설정
-Dspring.profiles.active=mysql
```

### 3. 연결 확인
- **애플리케이션**: http://localhost:8080
- **MySQL**: localhost:3307
- **Redis**: localhost:6380

## 데이터베이스 정보

### MySQL 접속 정보
- **Host**: localhost
- **Port**: 3307
- **Database**: ecommerce
- **Username**: ecommerce_app
- **Password**: ecommerce_pass
- **Root Password**: root_password

### Redis 접속 정보
- **Host**: localhost
- **Port**: 6380

## 프로파일 설정

### 사용 가능한 프로파일
1. **mysql** (기본) - MySQL + Redis 환경
2. **h2** - H2 In-Memory 데이터베이스 (개발용)
3. **docker** - Docker 환경용
4. **test** - 테스트용 (TestContainers)

### 프로파일 변경 방법
```bash
# H2로 변경 (개발 테스트용)
java -jar app.jar --spring.profiles.active=h2

# Docker 환경으로 변경
java -jar app.jar --spring.profiles.active=docker
```

## 유용한 명령어

### Docker 관리
```bash
# 컨테이너 중지
docker-compose down

# 볼륨까지 완전 삭제
docker-compose down -v

# 컨테이너 재시작
docker-compose restart mysql

# MySQL 컨테이너 직접 접속
docker exec -it ecommerce_mysql mysql -u ecommerce_app -p ecommerce
```

### MySQL 직접 쿼리
```bash
# MySQL 컨테이너 내부 접속
docker exec -it ecommerce_mysql bash

# MySQL 접속
mysql -u ecommerce_app -p ecommerce

# 테이블 확인
SHOW TABLES;
SELECT * FROM users;
SELECT * FROM products;
```

## 초기 데이터

### 기본 생성 데이터
1. **관리자 계정**
   - Email: admin@ecommerce.com
   - Password: admin123

2. **샘플 상품** (5개)
   - 노트북, 마우스, 키보드, 모니터, 헤드셋

3. **샘플 쿠폰** (2개)
   - WELCOME10: 10% 할인
   - SAVE50K: 5만원 할인

## 문제 해결

### 포트 충돌 시
```bash
# 포트 사용 확인
netstat -an | findstr :3307
netstat -an | findstr :6380

# docker-compose.yml에서 포트 변경 후 재시작
docker-compose down && docker-compose up -d
```

### 데이터베이스 초기화
```bash
# 볼륨 삭제 후 재생성
docker-compose down -v
docker-compose up -d
```

### 권한 문제 시
```bash
# MySQL 컨테이너에서 권한 재설정
docker exec -it ecommerce_mysql mysql -u root -p
```
```sql
GRANT ALL PRIVILEGES ON ecommerce.* TO 'ecommerce_app'@'%';
FLUSH PRIVILEGES;
```

## 테스트 실행

테스트는 기존대로 TestContainers를 사용하므로 별도 MySQL 설정이 불필요.

```bash
# 모든 테스트 실행
./gradlew test

# 특정 테스트 실행
./gradlew test --tests "*UserControllerTest*"
```