@echo off
REM Enhanced E2E Loan Approval Test Runner for Windows
REM This script runs the complete loan approval process test with Docker containers

echo 🏦 Starting Complete Loan Approval Process E2E Test
echo ==================================================

REM Check if required tools are installed
echo [INFO] Checking required tools...

where docker >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Docker is not installed or not in PATH
    exit /b 1
)

where npm >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] npm is not installed or not in PATH
    exit /b 1
)

echo [SUCCESS] All required tools are available

REM Setup test environment
echo [INFO] Setting up test environment...
cd docker

if not exist "node_modules" (
    echo [INFO] Installing dependencies...
    call npm install
    if %errorlevel% neq 0 (
        echo [ERROR] Failed to install dependencies
        exit /b 1
    )
)

REM Install Playwright browsers
echo [INFO] Installing Playwright browsers...
call npx playwright install chromium firefox
cd ..

REM Cleanup any existing containers
echo [INFO] Cleaning up existing containers...
cd docker
docker compose -f docker-compose.e2e.yml down -v --remove-orphans >nul 2>nul

REM Build containers
echo [INFO] Building containers (this may take a few minutes)...
docker compose -f docker-compose.e2e.yml build --no-cache
if %errorlevel% neq 0 (
    echo [ERROR] Failed to build containers
    exit /b 1
)

REM Start containers
echo [INFO] Starting containers with proper startup order...
docker compose -f docker-compose.e2e.yml up -d
if %errorlevel% neq 0 (
    echo [ERROR] Failed to start containers
    exit /b 1
)

echo [INFO] Waiting for services to become healthy...

REM Wait for Zeebe (no health check, just wait)
echo [INFO] Waiting for Zeebe broker to start...
timeout /t 45 /nobreak >nul

REM Wait for services to be ready (simplified for Windows)
echo [INFO] Waiting for backend to be ready...
timeout /t 60 /nobreak >nul

echo [INFO] Waiting for frontend to be ready...
timeout /t 30 /nobreak >nul

REM Additional stabilization time
echo [INFO] Allowing additional time for services to stabilize...
timeout /t 15 /nobreak >nul

REM Show container status
echo [INFO] Container status:
docker compose -f docker-compose.e2e.yml ps

REM Test basic connectivity
echo [INFO] Testing basic connectivity...
curl -f http://localhost:8080/actuator/health >nul 2>nul
if %errorlevel% equ 0 (
    echo [SUCCESS] Backend health endpoint is responding
) else (
    echo [WARNING] Backend health endpoint may not be fully ready
)

curl -f http://localhost:80 >nul 2>nul
if %errorlevel% equ 0 (
    echo [SUCCESS] Frontend is responding
) else (
    echo [WARNING] Frontend may not be fully ready
)

echo [SUCCESS] Services appear to be ready for testing!

REM Run the E2E tests
cd docker
echo [INFO] Running E2E Loan Approval Tests...
echo [INFO] This will test the complete loan approval workflow:
echo [INFO]   - Real user authentication (testuser/demo123)
echo [INFO]   - Dashboard navigation
echo [INFO]   - Loan application submission
echo [INFO]   - BPMN process execution with Zeebe
echo [INFO]   - Result verification

REM Set environment and run tests
set PLAYWRIGHT_CONFIG_FILE=playwright.config.docker.js
call npx playwright test --config=playwright.config.docker.js --reporter=list

if %errorlevel% equ 0 (
    echo [SUCCESS] 🎉 All E2E tests passed!
    
    echo [INFO] Test artifacts created:
    dir docker\e2e-*.png 2>nul
    
    if exist "docker\test-results" (
        echo [INFO] Test results directory:
        dir docker\test-results
    )
    
    echo [SUCCESS] E2E Loan Approval Test completed successfully!
    echo [SUCCESS] Check the screenshots in docker\ directory to see the test execution
) else (
    echo [ERROR] ❌ E2E tests failed!
    
    echo [INFO] Container logs for debugging:
    echo === Backend Logs ===
    docker compose -f docker\docker-compose.e2e.yml logs --tail=20 tribe
    echo === Frontend Logs ===
    docker compose -f docker\docker-compose.e2e.yml logs --tail=20 frontend
    
    REM Cleanup on failure
    cd docker
    docker compose -f docker-compose.e2e.yml down -v --remove-orphans >nul 2>nul
    cd ..
    exit /b 1
)

REM Cleanup
echo [INFO] Cleaning up containers...
cd docker
docker compose -f docker-compose.e2e.yml down -v --remove-orphans >nul 2>nul
cd ..

echo [SUCCESS] Complete E2E Loan Approval Process test finished!
pause