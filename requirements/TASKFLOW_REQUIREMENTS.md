# TaskFlow Functional Requirements

## Authentication and authorization
- Signup and signin
- JWT generation, validation and expiration handling
- BCrypt password hashing
- Current-user endpoint (`/api/auth/me`)
- Database-backed active/suspended account checks
- USER / MANAGER / ADMIN authorization
- Public signup always creates USER; only ADMIN can promote roles

## Users
- List active assignable users for managers/admins
- Admin user list
- Change user role
- Activate/suspend user
- Track registration and last-login timestamps

## Projects
- List and view projects
- Create, update and delete projects
- Project status, description and accent color
- Creator ownership/audit fields

## Tasks
- Kanban project board and personal task list
- Create, update and delete tasks
- Assignee and creator tracking
- Priority: LOW, MEDIUM, HIGH, URGENT
- Status: TODO, IN_PROGRESS, REVIEW, DONE
- Due date, planned/actual start and end dates
- Estimated and actual hours
- Work notes/content
- Tags and task metadata
- Assigned USER may change own task status

## Comments and activity
- List/add task comments
- Comment author and timestamps
- Activity timeline for create/update/status/comment events
- Audit records retain useful history when parent records are removed where appropriate

## Planning
- Personal/project planning blocks
- Status, priority, start/end dates and progress percentage
- Plan checklist/items
- Optional task linkage for plan items
- Plan-item workflow status, planned dates and order

## Reminders
- General, task-linked and plan-linked reminders
- One-time / daily / weekly / monthly recurrence
- Pending, sent, dismissed and cancelled states
- Scheduled due-reminder processing
- Repeating reminders automatically advance to the next occurrence
- Delivery audit log for IN_APP / EMAIL / PUSH channels
- Realtime reminder-due events

## Dashboard
- Project count
- Total tasks
- Todo / In-progress / Review / Done counts
- Overdue count
- Recent tasks
- Planning preview
- Upcoming reminders

## Realtime
- Spring backend publishes mutation/reminder events to Redis
- Node realtime service subscribes to Redis
- Socket.IO authenticates the same JWT
- React receives `task-event` and refreshes relevant data

## Database
MySQL database: `task_manager`

Canonical application tables:
- `users`
- `projects`
- `tasks`
- `comments`
- `activity_logs`
- `tags`
- `task_meta`
- `task_tags`
- `plans`
- `plan_items`
- `reminders`
- `reminder_delivery_log`

Flyway additionally manages its own `flyway_schema_history` metadata table.

## Development and operations
- Swagger/OpenAPI
- Postman collection with JWT token capture
- Docker Compose for frontend, backend, MySQL, Redis and realtime service
- Actuator health endpoint
- Environment-variable configuration
- JWT/security integration tests
- Canonical MySQL Workbench import file: `database/task_manager_full.sql`
