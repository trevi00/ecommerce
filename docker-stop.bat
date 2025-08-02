@echo off
echo Stopping Docker containers...

REM Docker Compose 중지
docker-compose down

echo Docker containers stopped.
pause
