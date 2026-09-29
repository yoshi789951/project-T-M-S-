@echo off
setlocal

echo ==========================================
echo TaskFlow - Install project dependencies
echo ==========================================

where node >nul 2>&1 || (echo ERROR: Node.js is not installed. & exit /b 1)
where npm >nul 2>&1 || (echo ERROR: npm is not installed. & exit /b 1)
where java >nul 2>&1 || (echo ERROR: Java 21 is not installed. & exit /b 1)
where mvn >nul 2>&1 || (echo ERROR: Maven 3.9+ is not installed. & exit /b 1)

echo.
echo [1/3] Installing frontend libraries...
pushd frontend
call npm install || exit /b 1
popd

echo.
echo [2/3] Installing realtime libraries...
pushd realtime-service
call npm install || exit /b 1
popd

echo.
echo [3/3] Resolving backend Maven libraries and running tests...
pushd backend
call mvn clean test || exit /b 1
popd

echo.
echo All TaskFlow dependencies installed/tested successfully.
endlocal
