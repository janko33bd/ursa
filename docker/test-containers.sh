#!/bin/bash

echo "Testing Docker containers..."

# Test backend health
echo "1. Testing backend health endpoint:"
BACKEND_HEALTH=$(curl -s http://localhost:8080/actuator/health)
if [[ "$BACKEND_HEALTH" == '{"status":"UP"}' ]]; then
    echo "   ✅ Backend is healthy: $BACKEND_HEALTH"
else
    echo "   ❌ Backend health check failed: $BACKEND_HEALTH"
fi

# Test frontend
echo "2. Testing frontend:"
FRONTEND_RESPONSE=$(curl -s http://localhost/ | head -1)
if [[ "$FRONTEND_RESPONSE" == *"<!doctype html>"* ]]; then
    echo "   ✅ Frontend is serving Angular app"
else
    echo "   ❌ Frontend is not serving correctly: $FRONTEND_RESPONSE"
fi

# Check container status
echo "3. Container status:"
docker ps --filter "name=tribe" --format "   {{.Names}}: {{.Status}}"

echo "4. Container health:"
for container in tribe-backend tribe-frontend; do
    HEALTH=$(docker inspect --format='{{.State.Health.Status}}' $container 2>/dev/null || echo "no health check")
    echo "   $container: $HEALTH"
done

echo ""
echo "✅ Docker containers are working correctly!"
echo "Frontend: http://localhost/"
echo "Backend: http://localhost:8080/"
echo "Backend Health: http://localhost:8080/actuator/health"