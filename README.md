# ISP Field Employee Management System

A full-stack workforce management system designed for Internet Service Providers (ISPs) to manage field employees, work activities, salaries, advances, and related operational data from a centralized web application.

The system is designed with field usage in mind, including support for **offline-capable workflows** so employees can continue working when internet connectivity is unreliable.

## Overview

Managing ISP field employees through spreadsheets, paper records, or disconnected systems can make it difficult to track employee information, work activities, salary records, and advances.

This project provides a centralized system for managing those operations through a web-based interface.

### Key goals

* Manage field employee information
* Track field workforce operations
* Manage salaries and advances
* Support mobile-friendly field usage
* Provide authentication and authorization
* Support offline-capable workflows
* Maintain persistent records using PostgreSQL
* Provide real-time communication where required
* Generate operational documents and reports

## Features

* Employee management
* Field workforce management
* Salary management
* Advance/payment tracking
* Authentication and authorization
* JWT-based security
* Google OAuth2 integration
* Email support
* Real-time communication using WebSockets
* Offline-capable frontend functionality
* Persistent local browser storage
* PostgreSQL database
* PDF generation
* Excel document generation
* REST APIs
* Health and application monitoring through Spring Boot Actuator
* Docker-based development environment
* Automated frontend and backend testing

## Architecture

The application is organized into three main components:

```text
┌──────────────────────────────┐
│        React Frontend        │
│   React + TypeScript + Vite  │
└──────────────┬───────────────┘
               │
               │ REST / WebSocket
               ▼
┌──────────────────────────────┐
│      Spring Boot Backend     │
│ Java 21 + Spring Boot        │
│ Security + JPA + WebSocket   │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│          PostgreSQL          │
│          Database            │
└──────────────────────────────┘
```

The project can also be run using Docker Compose:

```text
Frontend
   │
   ▼
Backend
   │
   ▼
PostgreSQL
```

## Tech Stack

### Backend

* Java 21
* Spring Boot 3.4.4
* Spring Web
* Spring Security
* Spring Data JPA
* Spring Validation
* Spring WebSocket
* Spring Mail
* Spring OAuth2 Client
* PostgreSQL
* JWT
* Apache PDFBox
* Apache POI
* Spring Boot Actuator
* Maven

### Frontend

* React 19
* TypeScript
* Vite
* React Router
* Axios
* Tailwind CSS
* IndexedDB
* STOMP.js
* SockJS
* Vitest
* React Testing Library

### Infrastructure

* Docker
* Docker Compose
* PostgreSQL 16
* GitHub Actions

## Project Structure

```text
isp-field-employee-management/
│
├── .github/
│   └── workflows/
│
├── allinone-backend/
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
│
├── allinone-frontend/
│   ├── src/
│   ├── package.json
│   └── Dockerfile
│
├── docker-compose.yml
├── .gitignore
└── LICENSE
```

## Getting Started

### Prerequisites

For local development, install:

* Java 21
* Maven
* Node.js
* npm
* PostgreSQL

Alternatively, Docker and Docker Compose can be used to run the complete stack.

### Clone the repository

```bash
git clone https://github.com/finleysimula67/isp-field-employee-management.git

cd isp-field-employee-management
```

## Running with Docker Compose

Create an environment file:

```bash
cp .env.example .env
```

Configure the required environment variables.

Then start the application:

```bash
docker compose up --build
```

The default services are:

| Service    |   Port |
| ---------- | -----: |
| Frontend   |   `80` |
| Backend    | `8080` |
| PostgreSQL | `5432` |

The Docker configuration uses PostgreSQL with persistent volumes for database data and application uploads.

## Running Backend Locally

Navigate to the backend:

```bash
cd allinone-backend
```

Run the application:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
mvnw.cmd spring-boot:run
```

The backend runs on:

```text
http://localhost:8080
```

## Running Frontend Locally

Navigate to the frontend:

```bash
cd allinone-frontend
```

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

The frontend will normally be available at:

```text
http://localhost:5173
```

## Building

### Frontend

```bash
npm run build
```

### Backend

```bash
./mvnw clean package
```

For the production profile:

```bash
./mvnw clean package -Pprod
```

The production Maven profile builds the frontend and copies the generated frontend assets into the backend's static resources.

## Testing

### Frontend

Run the frontend test suite:

```bash
npm test
```

Run tests in watch mode:

```bash
npm run test:watch
```

### Backend

Run backend tests:

```bash
./mvnw test
```

## Configuration

The application uses environment variables for configuration.

Important configuration areas include:

* PostgreSQL connection
* JWT secret
* Default application password
* Allowed email addresses
* Email credentials
* Google OAuth2 credentials
* Frontend URL
* CORS configuration
* OAuth2 redirect URLs

**Do not commit production credentials, JWT secrets, OAuth credentials, or database passwords to the repository.**

## Offline Support

Field employees may work in areas where mobile connectivity is unreliable.

The frontend therefore includes browser-side persistence capabilities using IndexedDB. This allows selected application data and workflows to remain available locally and provides a foundation for offline-first field operations.

Offline synchronization behavior should be treated carefully when modifying the application because conflicting local and server-side changes can result in data inconsistencies.

## Security

The backend uses Spring Security and JWT-based authentication.

Additional authentication capabilities include OAuth2 client integration.

Security-sensitive configuration should always be supplied through environment variables rather than committed to source control.

## Deployment

The repository includes Docker Compose configuration for running:

```text
PostgreSQL
     │
     ▼
Spring Boot Backend
     │
     ▼
Frontend
```

The application can be deployed to a server capable of running Docker containers, or the frontend/backend components can be deployed independently depending on the deployment requirements.

## Production Considerations

Before deploying to production:

* Change all default passwords
* Generate a strong JWT secret
* Configure production CORS origins
* Configure secure OAuth2 redirect URLs
* Configure production email credentials
* Use HTTPS
* Protect PostgreSQL from public exposure
* Configure database backups
* Configure persistent storage for uploads
* Review authentication and authorization rules
* Remove development credentials and defaults

## License

This project is licensed under the MIT License.

See [LICENSE](LICENSE) for details.

## Project

**ISP Field Employee Management System**

Built to simplify workforce management for Internet Service Providers operating with field employees and real-world connectivity constraints.
