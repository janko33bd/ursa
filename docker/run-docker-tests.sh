#!/bin/bash

# Docker E2E Test Runner
# This script runs comprehensive end-to-end tests against Docker containers

set -e

echo "🧪 Docker E2E Test Runner"
echo "========================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
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

# Check if we're in the right directory
if [[ ! -f "docker-compose.test.yml" ]]; then
    print_error "docker-compose.test.yml not found. Please run this script from the docker directory."
    exit 1
fi

# Check if playwright is installed
if ! command -v npx &> /dev/null; then
    print_error "Node.js/npm not found. Please install Node.js to run Playwright tests."
    exit 1
fi

# Install Playwright if needed
print_status "Checking Playwright installation..."
if ! npx playwright --version &> /dev/null; then
    print_warning "Playwright not found. Installing..."
    npx playwright install
fi

# Ensure browsers are installed
print_status "Installing Playwright browsers..."
npx playwright install chromium

# Clean up any existing containers
print_status "Cleaning up existing containers..."
docker-compose -f docker-compose.test.yml down -v --remove-orphans || true

# Run the tests
print_status "Running Docker E2E tests..."
print_status "This will start Docker containers and run comprehensive tests..."

# Set environment variables for test configuration
export SHOW_CONTAINER_LOGS=${SHOW_CONTAINER_LOGS:-false}
export CI=${CI:-false}

# Run Playwright tests with Docker configuration
if npx playwright test --config=playwright.config.docker.js; then
    print_success "All tests passed! 🎉"
    
    # Show test results
    print_status "Test results available in:"
    echo "  📊 HTML Report: docker-test-results/index.html"
    echo "  📄 JSON Report: docker-test-results.json"
    
    # Open HTML report if not in CI
    if [[ "$CI" != "true" && -f "docker-test-results/index.html" ]]; then
        print_status "Opening test report..."
        if command -v xdg-open &> /dev/null; then
            xdg-open docker-test-results/index.html
        elif command -v open &> /dev/null; then
            open docker-test-results/index.html
        else
            echo "  📖 Open docker-test-results/index.html in your browser to view the report"
        fi
    fi
    
    print_success "Docker E2E tests completed successfully!"
    exit 0
else
    print_error "Some tests failed!"
    
    print_warning "Showing recent container logs for debugging:"
    echo "--- Frontend logs ---"
    docker logs tribe-frontend --tail 10 || echo "Could not get frontend logs"
    echo "--- Backend logs ---"
    docker logs tribe-backend --tail 10 || echo "Could not get backend logs"
    echo "--- Zeebe logs ---"
    docker logs zeebe-broker --tail 10 || echo "Could not get Zeebe logs"
    
    print_error "Check the test results for details:"
    echo "  📊 HTML Report: docker-test-results/index.html"
    echo "  📄 JSON Report: docker-test-results.json"
    
    exit 1
fi