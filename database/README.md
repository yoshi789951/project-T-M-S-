# TaskFlow MySQL — `task_manager`

The database is designed for MySQL 8.x with `utf8mb4` / `utf8mb4_unicode_ci`.

## Canonical standalone SQL

Only one manual import file is kept to avoid duplicate/repeated schema copies:

```text
database/task_manager_full.sql
```

It creates `task_manager`, drops/recreates the project tables in foreign-key-safe order, creates indexes/constraints and inserts reference tags.

## Tables

| Table | Purpose |
|---|---|
| `users` | Login identity, BCrypt password, role, active state, profile/audit fields |
| `projects` | Project workspaces |
| `tasks` | Assignments, status, priority, due/planned/actual time data, notes |
| `comments` | Task discussion linked to activity history |
| `activity_logs` | Task/project audit timeline |
| `tags` | Reusable task labels |
| `task_meta` | Extensible task key/value metadata |
| `task_tags` | Task ↔ tag many-to-many relationship |
| `plans` | Personal/project planning blocks |
| `plan_items` | Ordered checklist items inside plans |
| `reminders` | General/task/plan reminders |
| `reminder_delivery_log` | Reminder delivery/audit events |

## Recommended startup path

Create an empty database and let Spring Boot/Flyway apply migrations:

```text
backend/src/main/resources/db/migration/
├── V1__create_taskflow_schema.sql
└── V2__planning_reminders_activity_tags.sql
```

Hibernate runs with:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

so entities are checked against the migrated database rather than silently changing it.

## Manual MySQL Workbench path

Run:

```text
database/task_manager_full.sql
```

Then start Spring Boot normally. `spring.flyway.baseline-version=2` allows Flyway to baseline this already-complete non-empty schema instead of replaying V1/V2.

Do not both import the full SQL and manually run the individual V1/V2 scripts on the same clean database.

## Seed data

Flyway/full SQL inserts reference task tags:

- Frontend
- Backend
- Database
- JWT / Security
- Testing
- Urgent

The development ADMIN is created by `DataSeeder` with a BCrypt hash, not stored as a plain-text SQL password.
