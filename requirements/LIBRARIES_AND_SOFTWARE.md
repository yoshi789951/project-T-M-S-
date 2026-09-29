# TaskFlow Requirements, Libraries and Software

This file lists the direct libraries and runtime software used by the TaskFlow frontend, backend, realtime service and database layer.

## 1. Frontend — React/Vite

Declared in `frontend/package.json`.

| Library | Purpose |
|---|---|
| React 18 | Component-based frontend UI |
| React DOM | Renders React into the browser |
| React Router DOM | Landing/signin/signup/app route navigation and protected routes |
| Axios | REST API calls to Spring Boot and Bearer JWT interceptor |
| Zustand | Authentication/UI state management |
| Socket.IO Client | Live task/project/comment refresh events |
| Vite | Development server and production build |
| @vitejs/plugin-react | React support for Vite |

Install with:

```bash
cd frontend
npm install
npm run dev
```

## 2. Backend — Spring Boot/Java 21

Declared in `backend/pom.xml`.

| Maven dependency | Purpose |
|---|---|
| spring-boot-starter-web | REST controllers, JSON and HTTP server |
| spring-boot-starter-data-jpa | JPA/Hibernate repositories and ORM |
| spring-boot-starter-security | Authentication/authorization |
| spring-boot-starter-validation | Request DTO validation |
| spring-boot-starter-data-redis | Publishes realtime events to Redis |
| mysql-connector-j | JDBC connection from Spring Boot to MySQL |
| flyway-core | Versioned database migrations |
| flyway-mysql | MySQL support for Flyway |
| jjwt-api | JWT API |
| jjwt-impl | JWT implementation |
| jjwt-jackson | JWT JSON claims serialization |
| springdoc-openapi-starter-webmvc-ui | Swagger/OpenAPI documentation |
| spring-boot-starter-actuator | Health/diagnostic endpoint |
| spring-boot-devtools | Development restart support |
| spring-boot-starter-test | Backend tests |
| spring-security-test | Security test helpers |
| H2 | Isolated test database only |

Install/run with Maven:

```bash
cd backend
mvn clean test
mvn spring-boot:run
```

## 3. Realtime Service — Node.js

Declared in `realtime-service/package.json`.

| Library | Purpose |
|---|---|
| Socket.IO | WebSocket/realtime server |
| ioredis | Subscribes to Redis task events |
| jsonwebtoken | Verifies the JWT passed by the frontend during Socket.IO connection |

Install with:

```bash
cd realtime-service
npm install
npm start
```

## 4. Database Layer

TaskFlow uses **MySQL 8.x**.

Database integration is implemented through:

- MySQL Server
- MySQL Connector/J
- Spring Data JPA
- Hibernate ORM
- Flyway migrations

The frontend does **not** connect directly to MySQL. The correct architecture is:

```text
React Frontend
      ↓ Axios / JWT
Spring Boot REST API
      ↓ Spring Data JPA / Hibernate
MySQL Connector/J
      ↓
MySQL task_manager
```

Flyway scripts are stored in:

```text
backend/src/main/resources/db/migration/
  V1__create_taskflow_schema.sql
  V2__planning_reminders_activity_tags.sql
```

A manual SQL copy is available at:

```text
database/task_manager_full.sql
```

## 5. Infrastructure / Development Software

Recommended local software:

- Java JDK 21
- Maven 3.9+
- Node.js 20+ (Node 22 also supported by included realtime Docker image)
- npm 10+
- MySQL Server 8.x / MySQL Workbench
- Redis 7.x
- VS Code or Cursor
- Postman
- Docker Desktop + Docker Compose (easiest full-stack method)

## 6. Ports

| Service | Port |
|---|---:|
| Frontend | 5173 |
| Spring Boot backend | 8080 |
| Socket.IO realtime | 4000 |
| MySQL | 3306 |
| Redis | 6379 |

## 7. Authentication Libraries / Flow

```text
Signin form
   ↓
Axios POST /api/auth/signin
   ↓
Spring Security + BCrypt verifies password
   ↓
JJWT generates token
   ↓
React/Zustand saves token
   ↓
Axios sends Authorization: Bearer <token>
   ↓
JwtAuthenticationFilter
   ↓
CustomUserDetailsService loads current database user
   ↓
Role authorization: USER / MANAGER / ADMIN
```

## 8. Dependency Installation Note

The project intentionally does not include `node_modules`, Maven `.m2` caches, MySQL binaries or JDK binaries in the ZIP. They are machine-specific/generated dependencies. `npm install`, Maven, or Docker downloads the exact required libraries declared by the project.
