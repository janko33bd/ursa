# Loan Application System

A full-stack loan application system with automated approval workflow. Built with Spring Boot, Angular, and Camunda 8 Zeebe.

## Quick Start

### Prerequisites

- Docker
- Node.js 18+ (for local development)  
- Java 17+ (for local development)

### Run with Docker (Recommended)

1. Set JWT secret:
```bash
export JWT_SECRET=$(openssl rand -base64 32)
```

2. Run the application:
```bash
cd docker
docker-compose up --build
```

3. Access the application:
- Frontend: http://localhost
- Backend API: http://localhost:8080

### Demo Credentials

- **Username:** testuser | **Password:** password123
- **Username:** admin | **Password:** password123  
- **Username:** officer | **Password:** password123

## Local Development

### Backend (Spring Boot)

```bash
cd tribe
export JWT_SECRET=$(openssl rand -base64 32)
./gradlew bootRun
```

### Frontend (Angular)

```bash
cd frontend
npm install
npm start
```

## Testing

Run E2E tests:
```bash
cd docker
export JWT_SECRET=$(openssl rand -base64 32)
./run-e2e-loan-test.sh
```

## Project Structure

- `tribe/` - Spring Boot backend with JWT auth and Camunda workflow
- `frontend/` - Angular frontend with login and loan application form
- `docker/` - Docker configuration and E2E tests

## Technology Stack

- **Backend:** Spring Boot, Spring Security, JWT, Camunda 8 Zeebe
- **Frontend:** Angular, TypeScript
- **Database:** H2 (in-memory)
- **Containerization:** Docker, Docker Compose
- **Testing:** JUnit, Playwright