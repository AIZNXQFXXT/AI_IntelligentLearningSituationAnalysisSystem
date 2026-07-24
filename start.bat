@echo off
setlocal enabledelayedexpansion

cd /d "%~dp0"

echo [1/4] Checking environment...

where mvn >nul 2>nul
if errorlevel 1 (
    echo.
    echo [ERROR] Maven not found in PATH.
    echo Please install Maven and add it to system PATH.
    pause
    exit /b 1
)

java -version >nul 2>nul
if errorlevel 1 (
    echo.
    echo [ERROR] Java not found in PATH.
    echo Please install JDK 17+ and add it to system PATH.
    pause
    exit /b 1
)

echo [OK] Environment OK

echo.
echo [2/4] Installing common-module...
echo      Running: mvn install -DskipTests -pl common-module -am
echo      (This may take a while on first run - downloading dependencies)
echo.
call mvn install -DskipTests -pl common-module -am
if errorlevel 1 (
    echo.
    echo [ERROR] common-module compilation failed.
    echo Check the error messages above.
    pause
    exit /b 1
)
echo [OK] common-module installed

echo.
echo [3/4] Starting backend in background...
start /b cmd /c "mvn spring-boot:run -pl backend-module -Dmaven.test.skip=true > backend.log 2>&1"
echo [OK] Backend is starting (logs: backend.log)

echo.
echo [4/4] Waiting for backend to be ready...
for /l %%i in (1,1,60) do (
    curl -s http://localhost:8080/api/health >nul 2>nul
    if not errorlevel 1 (
        echo [OK] Backend is ready
        goto :backend_ready
    )
    echo      Waiting... (%%i/60)
    timeout /t 2 /nobreak >nul
)
:backend_ready

echo.
echo [5/4] Starting JavaFX client...
echo      Client will auto-retry connection while backend starts up.
echo.
call mvn javafx:run -pl client-module

echo.
echo === Cleaning up ===
echo Stopping backend process on port 8080...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080') do (
    if not "%%a"=="0" (
        taskkill /f /pid %%a >nul 2>nul && echo [OK] Backend stopped
    )
)
echo Done.
pause