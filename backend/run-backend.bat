@echo off
where mvn >nul 2>nul
if errorlevel 1 (
  echo Maven was not found. Install Maven 3.9+ and add it to PATH.
  pause
  exit /b 1
)
mvn spring-boot:run
