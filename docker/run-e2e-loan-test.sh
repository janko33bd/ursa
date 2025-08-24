#!/bin/bash

# Enhanced E2E Loan Approval Test Runner with Real Authentication
# This script runs the complete loan approval process test with Docker containers

set -e  # Exit on any error

echo "🏦 Starting Complete Loan Approval Process E2E Test"
echo "=================================================="

# Color codes for better output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to cleanup containers
cleanup() {
    print_status "Cleaning up containers..."
    cd docker
    docker compose -f docker-compose.e2e.yml down -v --remove-orphans 2>/dev/null || true
    docker system prune -f 2>/dev/null || true
    cd ..
    print_success "Cleanup completed"
}

# Set trap to cleanup on script exit
trap cleanup EXIT

# Check if required tools are installed
print_status "Checking required tools..."

if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed or not in PATH"
    exit 1
fi

if ! command -v npm &> /dev/null; then
    print_error "npm is not installed or not in PATH"
    exit 1
fi

print_success "All required tools are available"

# Install Playwright if not already installed
print_status "Setting up test environment..."
cd docker

if [ ! -d "node_modules" ]; then
    print_status "Installing dependencies..."
    npm install
fi

# Ensure Playwright browsers are installed
print_status "Installing Playwright browsers..."
npx playwright install chromium firefox
cd ..

# Build and start containers
print_status "Building and starting Docker containers..."
cd docker
docker compose -f docker-compose.e2e.yml down -v --remove-orphans 2>/dev/null || true

# Build containers
print_status "Building containers (this may take a few minutes)..."
docker compose -f docker-compose.e2e.yml build --no-cache

# Start containers
print_status "Starting containers with proper startup order..."
docker compose -f docker-compose.e2e.yml up -d

# Wait for containers to be healthy
print_status "Waiting for services to become healthy..."

# Function to wait for service health
wait_for_service() {
    local service_name=$1
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if docker compose -f docker-compose.e2e.yml ps --format "table {{.Service}}\t{{.Status}}" | grep "$service_name" | grep -q "healthy"; then
            print_success "$service_name is healthy"
            return 0
        fi
        
        print_status "Waiting for $service_name to be healthy (attempt $attempt/$max_attempts)..."
        sleep 10
        attempt=$((attempt + 1))
    done
    
    print_error "$service_name failed to become healthy within timeout"
    docker compose -f docker-compose.e2e.yml logs $service_name
    return 1
}

# Wait for Zeebe (no health check, just wait)
print_status "Waiting for Zeebe broker to start..."
sleep 45

# Wait for backend to be healthy
wait_for_service "tribe"

# Wait for frontend to be healthy
wait_for_service "frontend"

# Additional wait to ensure all services are fully ready
print_status "Allowing additional time for services to stabilize..."
sleep 15

# Show container status
print_status "Container status:"
docker compose -f docker-compose.e2e.yml ps

# Test basic connectivity
print_status "Testing basic connectivity..."

# Test backend health
if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
    print_success "Backend health endpoint is responding"
else
    print_error "Backend health endpoint is not responding"
    docker compose -f docker-compose.e2e.yml logs tribe
    exit 1
fi

# Test frontend
if curl -f http://localhost:80 > /dev/null 2>&1; then
    print_success "Frontend is responding"
else
    print_error "Frontend is not responding"
    docker compose -f docker-compose.e2e.yml logs frontend
    exit 1
fi

# Test authentication endpoint specifically
print_status "Testing authentication endpoint..."
if curl -f -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"testuser","password":"demo123"}' > /dev/null 2>&1; then
    print_success "Authentication endpoint is working"
else
    print_warning "Authentication endpoint test failed (may be normal if it returns 4xx for curl)"
fi

print_success "All services are ready for testing!"

# Run the E2E tests
print_status "Running E2E Loan Approval Tests..."
print_status "This will test the complete loan approval workflow:"
print_status "  - Real user authentication (testuser/demo123)"
print_status "  - Dashboard navigation"
print_status "  - Loan application submission"
print_status "  - BPMN process execution with Zeebe"
print_status "  - Result verification"

cd ..
export PLAYWRIGHT_CONFIG_FILE="docker/playwright.config.e2e.js"

# Run tests with detailed output
if npx playwright test --config="docker/playwright.config.e2e.js" --reporter=list; then
    print_success "🎉 All E2E tests passed!"
    
    # Show test artifacts
    print_status "Test artifacts created:"
    ls -la docker/e2e-*.png 2>/dev/null || print_warning "No screenshots found"
    
    if [ -d "docker/test-results" ]; then
        print_status "Test results directory:"
        ls -la docker/test-results/
    fi
    
    print_success "E2E Loan Approval Test completed successfully!"
    print_success "Check the screenshots in docker/ directory to see the test execution"
    
else
    print_error "❌ E2E tests failed!"
    
    # Show container logs for debugging
    print_status "Container logs for debugging:"
    echo "=== Zeebe Logs ==="
    docker compose -f docker/docker-compose.e2e.yml logs --tail=50 zeebe
    echo "=== Backend Logs ==="
    docker compose -f docker/docker-compose.e2e.yml logs --tail=50 tribe
    echo "=== Frontend Logs ==="
    docker compose -f docker/docker-compose.e2e.yml logs --tail=50 frontend
    
    exit 1
fi

print_success "Complete E2E Loan Approval Process test finished!"