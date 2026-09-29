# TaskFlow — Full Stack Task Management System

TaskFlow is a React/Vite + Spring Boot + MySQL task-management project with JWT security, USER/MANAGER/ADMIN authorization, Kanban projects/tasks, comments, activity history, tags, planning checklists, reminders, Redis events and Socket.IO realtime refresh.

## Project structure

```text
TaskFlow_Perfect_Project/
├── frontend/                 React 18 + Vite UI
├── backend/                  Spring Boot 3 / Java 21 REST API
├── realtime-service/         Socket.IO + Redis subscriber
├── database/
│   ├── task_manager_full.sql canonical MySQL Workbench import
│   ├── DATABASE_DESIGN.md
│   └── README.md
├── postman/                  Postman collection
├── requirements/             Library + functional requirements
├── docker-compose.yml
├── START_HERE_WINDOWS.txt
└── JWT_IMPLEMENTATION_GUIDE.md
```

## Main features

- JWT signup/signin and `/api/auth/me`
- BCrypt password hashing
- USER / MANAGER / ADMIN roles
- Project create/read/update/delete
- Kanban task workflow: TODO → IN_PROGRESS → REVIEW → DONE
- Task assignment, priorities, due dates, planned dates, hours, notes and tags
- Task comments and activity timeline
- Personal task list
- Planning blocks with progress and checklist/plan items
- Task/plan/general reminders with repeat settings and delivery audit records
- Admin role and account activation controls
- MySQL persistence with Flyway migrations
- Redis event publication + Socket.IO realtime UI refresh
- Swagger/OpenAPI and Postman collection
- Dark/light UI

## Database

Database name: `task_manager`

Canonical tables:

```text
users
projects
tasks
comments
activity_logs
tags
task_meta
task_tags
plans
plan_items
reminders
reminder_delivery_log
```

The project intentionally has **one standalone database import file**:

```text
database/task_manager_full.sql
```

For normal application startup, use an empty `task_manager` database and let Flyway execute:

```text
V1__create_taskflow_schema.sql
V2__planning_reminders_activity_tags.sql
```

If `task_manager_full.sql` was imported first, Flyway is configured with baseline version `2`, so V1/V2 are not replayed against the already-complete schema.

## Fastest run — Docker

Requirements: Docker Desktop with Docker Compose.

From the project root:

```bash
docker compose up --build
```

Open:

- Frontend: `http://localhost:5173`
- Backend API: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui.html`
- Realtime service: `http://localhost:4000`
- MySQL: `localhost:3306`
- Redis: `localhost:6379`

Development administrator:

```text
Email:    admin@taskflow.local
Password: Admin123!
```

Change the seeded credentials and JWT secret before any real deployment.

## Local run without Docker

Requirements:

- Java 21
- Maven 3.9+
- Node.js 20+
- MySQL 8+
- Redis 7+ for realtime events

Create an empty database:

```sql
CREATE DATABASE IF NOT EXISTS task_manager
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

Backend:

```bash
cd backend
mvn clean test
mvn spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

Realtime service:

```bash
cd realtime-service
npm install
npm start
```

## Frontend libraries

Pinned in `frontend/package.json`:

- React 18.3.1
- React DOM 18.3.1
- React Router DOM 6.28.0
- Axios 1.7.7
- Zustand 5.0.1
- Socket.IO Client 4.8.1
- Vite 5.4.10

## Backend libraries

Declared in `backend/pom.xml`:

- Spring Boot Web
- Spring Data JPA
- Spring Security
- Spring Validation
- Spring Data Redis
- MySQL Connector/J
- Flyway Core + Flyway MySQL
- JJWT 0.12.6
- Springdoc OpenAPI
- Actuator
- JUnit/Spring Boot Test
- Spring Security Test
- H2 test database

## Key API groups

```text
/api/auth
/api/projects
/api/tasks
/api/tags
/api/plans
/api/plans/{planId}/items
/api/reminders
/api/dashboard
/api/users
/api/admin/users
```

Task activity:

```text
GET /api/tasks/{taskId}/activity
```

## Role behavior

- USER: authenticate, view workspaces, manage assigned task status, comments, personal plans/reminders.
- MANAGER: USER abilities plus project/task creation, editing, deletion and assignment.
- ADMIN: MANAGER abilities plus role and account administration.

Public signup always creates `USER`. MANAGER/ADMIN elevation requires an authenticated ADMIN.

## Security notes

- APIs are stateless and use `Authorization: Bearer <JWT>`.
- Passwords are BCrypt hashes.
- Suspended users are rejected even when presenting an older JWT because user details are reloaded from MySQL.
- Change `JWT_SECRET`, seed credentials and CORS origins outside development.
- Use HTTPS and external secret management for production.

## Verification commands

Backend tests:

```bash
cd backend
mvn test
```

Frontend build:

```bash
cd frontend
npm install
npm run build
```

Realtime syntax:

```bash
node --check realtime-service/server.js
```

This package was source-audited for duplicated schema files, API/UI mismatches and orphaned database structures. The runtime used to prepare this archive has no external package-network access, so dependency downloads and a complete Maven/npm build must be executed on a machine with internet access or through Docker during `docker compose up --build`.
