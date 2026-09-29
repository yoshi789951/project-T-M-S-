# Task Manager Database Design

## Planning block
`plans` stores a user's overall plan. `plan_items` stores individual steps. A plan can optionally belong to a project, and a plan item can optionally reference an existing task.

## Reminder block
`reminders` can be general or connected to a task or plan. Supported repeat values are `NONE`, `DAILY`, `WEEKLY`, and `MONTHLY`. A scheduled backend worker processes due reminders. One-time reminders move to `SENT`; recurring reminders are advanced to their next date. Each in-app delivery is recorded in `reminder_delivery_log`, leaving room for future email/push channels.

## Required workflow values

- User roles: `USER`, `MANAGER`, `ADMIN`
- Task status: `TODO`, `IN_PROGRESS`, `REVIEW`, `DONE`
- Priority: `LOW`, `MEDIUM`, `HIGH`, `URGENT`
- Plan status: `DRAFT`, `ACTIVE`, `ON_HOLD`, `COMPLETED`, `CANCELLED`
- Reminder status: `PENDING`, `SENT`, `DISMISSED`, `CANCELLED`
- Repeat type: `NONE`, `DAILY`, `WEEKLY`, `MONTHLY`

The Java application uses enums for these values, while the database stores them as readable VARCHAR values.
