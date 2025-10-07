@echo off
echo Starting Docker containers for E-commerce project...

REM Docker Compose 실행
docker-compose up -d

REM 컨테이너 상태 확인
echo.
echo Checking container status...
docker-compose ps

REM MySQL이 준비될 때까지 대기
echo.
echo Waiting for MySQL to be ready...
timeout /t 5 /nobreak > nul

REM MySQL 연결 테스트
docker exec ecommerce-mysql mysql -uecommerce -pecommerce1234 -e "SELECT 'MySQL is ready!' as status;" ecommerce_db 2>nul

if %errorlevel% equ 0 (
    echo MySQL is ready!
) else (
    echo MySQL is not ready yet. Please wait a moment and try again.
)

REM Redis 연결 테스트
docker exec ecommerce-redis redis-cli ping > nul 2>&1

if %errorlevel% equ 0 (
    echo Redis is ready!
) else (
    echo Redis is not ready yet.
)

echo.
echo Docker containers are running!
echo MySQL: localhost:3306 (user: ecommerce, password: ecommerce1234)
echo Redis: localhost:6379
pause
