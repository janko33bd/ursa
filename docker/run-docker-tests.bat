@echo off
setlocal enabledelayedexpansion

echo 🧪 Docker E2E Test Runner
echo =========================

REM Check if we're in the right directory
if not exist "docker-compose.test.yml" (
    echo [ERROR] docker-compose.test.yml not found. Please run this script from the docker directory.
    exit /b 1
)

REM Check if npm is available
where npm >nul 2>nul
if !errorlevel! neq 0 (
    echo [ERROR] Node.js/npm not found. Please install Node.js to run Playwright tests.
    exit /b 1
)

echo [INFO] Checking Playwright installation...
npx playwright --version >nul 2>nul
if !errorlevel! neq 0 (
    echo [WARNING] Playwright not found. Installing...
    npx playwright install
)

echo [INFO] Installing Playwright browsers...
npx playwright install chromium

echo [INFO] Cleaning up existing containers...
docker-compose -f docker-compose.test.yml down -v --remove-orphans

echo [INFO] Running Docker E2E tests...
echo [INFO] This will start Docker containers and run comprehensive tests...

REM Set environment variables
if not defined SHOW_CONTAINER_LOGS set SHOW_CONTAINER_LOGS=false
if not defined CI set CI=false

REM Run Playwright tests
npx playwright test --config=playwright.config.docker.js

if !errorlevel! equ 0 (
    echo [SUCCESS] All tests passed! 🎉
    echo [INFO] Test results available in:
    echo   📊 HTML Report: docker-test-results/index.html
    echo   📄 JSON Report: docker-test-results.json
    
    REM Open HTML report if available
    if exist "docker-test-results\index.html" (
        echo [INFO] Opening test report...
        start "" "docker-test-results\index.html"
    )
    
    echo [SUCCESS] Docker E2E tests completed successfully!
    exit /b 0
) else (
    echo [ERROR] Some tests failed!
    echo [WARNING] Showing recent container logs for debugging:
    
    echo --- Frontend logs ---
    docker logs tribe-frontend --tail 10 2>nul || echo Could not get frontend logs
    
    echo --- Backend logs ---
    docker logs tribe-backend --tail 10 2>nul || echo Could not get backend logs
    
    echo --- Zeebe logs ---
    docker logs zeebe-broker --tail 10 2>nul || echo Could not get Zeebe logs
    
    echo [ERROR] Check the test results for details:
    echo   📊 HTML Report: docker-test-results/index.html
    echo   📄 JSON Report: docker-test-results.json
    
    exit /b 1
)