# TaskFlow JWT Implementation Guide

This backend follows the same implementation flow as the provided ecommerce JWT reference, adapted to the TaskFlow React frontend.

## JWT flow

```text
React signup
  -> POST /api/auth/signup
  -> users table (BCrypt password, role USER)

React signin
  -> POST /api/auth/signin
  -> verify email + password
  -> JwtService.generateToken(...)
  -> return userId + name + email + role + message + token

React localStorage
  -> taskflow_token

Every protected REST request
  -> Authorization: Bearer <token>
  -> JwtAuthenticationFilter
  -> JwtService.extractEmail(...)
  -> CustomUserDetailsService
  -> users table
  -> JwtService.isTokenValid(...)
  -> Spring SecurityContext
  -> URL rules + @PreAuthorize + service ownership checks
```

## JWT/security files

- `backend/src/main/java/com/taskflow/security/JwtService.java`
- `backend/src/main/java/com/taskflow/security/CustomUserDetailsService.java`
- `backend/src/main/java/com/taskflow/security/JwtAuthenticationFilter.java`
- `backend/src/main/java/com/taskflow/config/SecurityConfig.java`
- `backend/src/main/java/com/taskflow/config/PasswordConfig.java`
- `backend/src/main/java/com/taskflow/service/AuthService.java`
- `backend/src/main/java/com/taskflow/controller/AuthController.java`
- `backend/src/main/java/com/taskflow/dto/AuthResponse.java`

## Role rules

| API / action | USER | MANAGER | ADMIN |
|---|---:|---:|---:|
| Signup / signin | public | public | public |
| View projects | yes | yes | yes |
| Create/update/delete projects | no | yes | yes |
| View task boards | yes | yes | yes |
| Create/update/delete tasks | no | yes | yes |
| Update assigned task status | yes | yes | yes |
| Update any task status | no | yes | yes |
| View assignable user directory | no | yes | yes |
| Admin user management | no | no | yes |

Public signup never accepts elevated privileges. New accounts are always `USER`.

## Frontend compatibility

The uploaded frontend expects:

- REST: `http://localhost:8080/api`
- Realtime: `http://localhost:4000`
- `auth.token`
- `auth.userId`
- `auth.name`
- `auth.email`
- `auth.role`

The backend returns all of these fields.

## Important test cases included

`JwtSecurityIntegrationTests.java` checks:

1. USER signup.
2. USER signin and JWT issuance.
3. `/api/auth/me` with bearer token.
4. USER blocked from ADMIN API.
5. USER blocked from project creation.
6. ADMIN signin.
7. ADMIN promotes USER to MANAGER.
8. MANAGER permission takes effect.
9. ADMIN suspends the account.
10. Suspended account's old JWT stops working.
