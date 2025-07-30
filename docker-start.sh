#!/bin/bash

echo "Starting Docker containers for E-commerce project..."

# Docker Compose 실행
docker-compose up -d

# 컨테이너 상태 확인
echo ""
echo "Checking container status..."
docker-compose ps

# MySQL이 준비될 때까지 대기
echo ""
echo "Waiting for MySQL to be ready..."
sleep 5

# MySQL 연결 테스트
docker exec ecommerce-mysql mysql -uecommerce -pecommerce1234 -e "SELECT 'MySQL is ready!' as status;" ecommerce_db 2>/dev/null

if [ $? -eq 0 ]; then
    echo "✅ MySQL is ready!"
else
    echo "❌ MySQL is not ready yet. Please wait a moment and try again."
fi

# Redis 연결 테스트
docker exec ecommerce-redis redis-cli ping > /dev/null 2>&1

if [ $? -eq 0 ]; then
    echo "✅ Redis is ready!"
else
    echo "❌ Redis is not ready yet."
fi

echo ""
echo "Docker containers are running!"
echo "MySQL: localhost:3306 (user: ecommerce, password: ecommerce1234)"
echo "Redis: localhost:6379"
