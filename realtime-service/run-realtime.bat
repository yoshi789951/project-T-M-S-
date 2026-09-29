@echo off
where npm >nul 2>nul
if errorlevel 1 (
  echo npm was not found. Install Node.js 20+.
  pause
  exit /b 1
)
if not exist node_modules npm install
npm start
