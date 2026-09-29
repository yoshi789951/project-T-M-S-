@echo off
setlocal

echo This local mode expects MySQL on port 3306 and Redis on port 6379.
echo Database: task_manager

echo Starting Spring Boot backend...
start "TaskFlow Backend" cmd /k "cd /d %~dp0backend && mvn spring-boot:run"

echo Starting realtime service...
start "TaskFlow Realtime" cmd /k "cd /d %~dp0realtime-service && npm start"

echo Starting React frontend...
start "TaskFlow Frontend" cmd /k "cd /d %~dp0frontend && npm run dev"

echo.
echo Frontend: http://localhost:5173
echo Backend:  http://localhost:8080
echo Swagger:  http://localhost:8080/swagger-ui.html
echo Realtime: http://localhost:4000
endlocal
