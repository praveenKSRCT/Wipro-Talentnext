# Spring Boot 3 + Spring Security 6 + JWT — Role-Based Auth CRUD Demo

End-to-end reference project: **Spring Boot 3.3 · Spring Security 6 · JWT (JJWT 0.12) · Spring Data JPA · MySQL 8**,
built in a clean layered architecture (controller → service → repository → entity), with role-based
(`ROLE_USER` / `ROLE_ADMIN`) access control and full CRUD on a `Product` resource.

## 1. Architecture / Package Layout

```
com.training.security
├── SecurityJwtApplication.java     # entry point
├── config/
│   ├── SecurityConfig.java         # SecurityFilterChain, PasswordEncoder, AuthenticationManager
│   ├── JwtAuthEntryPoint.java      # 401 handler (unauthenticated)
│   ├── JwtAccessDeniedHandler.java # 403 handler (wrong role)
│   └── DataInitializer.java        # seeds roles + a default admin user on boot
├── security/
│   ├── JwtUtil.java                # generate / parse / validate JWT
│   ├── JwtAuthFilter.java          # OncePerRequestFilter — validates JWT per request
│   ├── UserPrincipal.java          # UserDetails adapter over the User entity
│   └── CustomUserDetailsService.java
├── entity/          # User, Role, ERole, Product  (JPA entities)
├── repository/       # UserRepository, RoleRepository, ProductRepository
├── dto/              # request/response payloads (never expose entities directly)
├── service/           # interfaces
│   └── impl/           # implementations
├── controller/        # AuthController, ProductController, UserController
└── exception/          # ResourceNotFoundException, BadRequestException, GlobalExceptionHandler
```

**Why layered this way:** controllers stay thin (HTTP concerns only), services hold business
rules and transactions, repositories are pure Spring Data JPA, and DTOs decouple the wire format
from the persistence model — a change to the `User` entity never leaks into the JSON contract.

## 2. Security Flow

1. `POST /api/auth/register` → creates a `User`, password hashed with **BCrypt**, assigned
   `ROLE_USER` by default (or `ROLE_ADMIN`/`ROLE_USER` if specified).
2. `POST /api/auth/login` → `AuthenticationManager` verifies credentials against
   `CustomUserDetailsService` + BCrypt, then `JwtUtil` issues a signed **HS256 JWT** containing
   the username (`sub`) and roles as a custom claim, with a 1-hour expiry.
3. Every subsequent request carries `Authorization: Bearer <token>`.
4. `JwtAuthFilter` (a `OncePerRequestFilter` registered **before**
   `UsernamePasswordAuthenticationFilter`) extracts the token, calls `JwtUtil.validateToken()`
   (checks signature + expiry), loads the user via `CustomUserDetailsService`, and populates the
   `SecurityContext` — this is the "JWT token validation" step.
5. `SecurityConfig` enforces **stateless** sessions (`SessionCreationPolicy.STATELESS` — no
   `HttpSession`, no CSRF token needed) and role rules both at the URL level
   (`authorizeHttpRequests`) and again at the method level (`@PreAuthorize`) for defense in depth.
6. Failures are handled cleanly: no/invalid/expired token → `JwtAuthEntryPoint` returns **401**;
   valid token but wrong role → `JwtAccessDeniedHandler` returns **403**. Both return JSON, not
   Spring Boot's default HTML error page.

## 3. Prerequisites

- JDK 17+
- Maven 3.9+
- MySQL 8.0 running locally (or reachable)

## 4. Database Setup

The app auto-creates the database (`createDatabaseIfNotExist=true` in the JDBC URL) and the
tables (`spring.jpa.hibernate.ddl-auto=update`) on first run — no manual DDL needed. Just make
sure a MySQL 8 instance is reachable and the credentials match `application.properties`.

If you'd rather create it manually:

```sql
CREATE DATABASE IF NOT EXISTS security_jwt_db CHARACTER SET utf8mb4;
```

Update `src/main/resources/application.properties` with your MySQL username/password:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/security_jwt_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
```

Also change `app.jwt.secret` to your own value before deploying anywhere real —
the shipped value is a demo key only.

## 5. Run

```bash
mvn clean install
mvn spring-boot:run
```

App starts on `http://localhost:8080`. On first boot, `DataInitializer` seeds:
- Roles: `ROLE_USER`, `ROLE_ADMIN`
- A default admin: **username `admin` / password `Admin@123`** (change immediately after first login)

## 6. API Reference & curl Walkthrough

### 6.1 Register a normal user (public)
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john","email":"john@example.com","password":"John@123"}'
```

### 6.2 Register an admin (public — pass roles explicitly)
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","email":"alice@example.com","password":"Alice@123","roles":["admin"]}'
```

### 6.3 Login (public) → returns JWT
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@123"}'
```
Response:
```json
{
  "tokenType": "Bearer",
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "id": 1,
  "username": "admin",
  "email": "admin@example.com",
  "roles": ["ROLE_ADMIN", "ROLE_USER"]
}
```
Save the token: `TOKEN="eyJhbGciOiJIUzI1NiJ9..."`

### 6.4 Product CRUD (JWT required)

| Method | Endpoint              | Required role     |
|--------|-----------------------|--------------------|
| POST   | `/api/products`       | `ROLE_ADMIN`       |
| GET    | `/api/products`       | `ROLE_USER`/`ADMIN`|
| GET    | `/api/products/{id}`  | `ROLE_USER`/`ADMIN`|
| PUT    | `/api/products/{id}`  | `ROLE_ADMIN`       |
| DELETE | `/api/products/{id}`  | `ROLE_ADMIN`       |

Create (admin only):
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Wireless Mouse","description":"Ergonomic 2.4G mouse","price":19.99,"quantity":150}'
```

List (any authenticated user):
```bash
curl -X GET http://localhost:8080/api/products -H "Authorization: Bearer $TOKEN"
```

Search:
```bash
curl -X GET "http://localhost:8080/api/products?keyword=mouse" -H "Authorization: Bearer $TOKEN"
```

Update (admin only):
```bash
curl -X PUT http://localhost:8080/api/products/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Wireless Mouse","description":"Updated desc","price":17.99,"quantity":120}'
```

Delete (admin only):
```bash
curl -X DELETE http://localhost:8080/api/products/1 -H "Authorization: Bearer $TOKEN"
```

### 6.5 Prove role-based auth actually works

Login as `john` (a plain `ROLE_USER`), then try to create a product with his token — expect **403**:
```bash
USER_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john","password":"John@123"}' | python3 -c "import sys,json;print(json.load(sys.stdin)['accessToken'])")

curl -i -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Should Fail","price":1,"quantity":1}'
# => 403 Forbidden, JSON body from JwtAccessDeniedHandler
```

Try any product endpoint with no token at all — expect **401**:
```bash
curl -i http://localhost:8080/api/products
# => 401 Unauthorized, JSON body from JwtAuthEntryPoint
```

### 6.6 User management (admin only)

| Method | Endpoint                     |
|--------|-------------------------------|
| GET    | `/api/users`                  |
| GET    | `/api/users/{id}`             |
| PATCH  | `/api/users/{id}/enable`      |
| PATCH  | `/api/users/{id}/disable`     |
| DELETE | `/api/users/{id}`             |

```bash
curl -X GET http://localhost:8080/api/users -H "Authorization: Bearer $TOKEN"
curl -X PATCH http://localhost:8080/api/users/2/disable -H "Authorization: Bearer $TOKEN"
```

## 7. Key Teaching Points (for the training session)

- **Spring Security 6 style**: no `WebSecurityConfigurerAdapter` (removed) — configuration is a
  `SecurityFilterChain` `@Bean` built with the lambda DSL (`http.authorizeHttpRequests(auth -> ...)`).
- **Stateless JWT**: `SessionCreationPolicy.STATELESS` + CSRF disabled, since there's no
  server-side session or cookie to forge against.
- **Two layers of role enforcement**: URL-pattern rules in `SecurityConfig` (coarse, fast-fail
  before the controller is even invoked) plus `@PreAuthorize` at the method level (explicit,
  self-documenting, survives refactors of the URL mapping).
- **Password hashing**: `BCryptPasswordEncoder` — never store or compare plaintext passwords.
- **Token validation** is centralized in `JwtUtil.validateToken()` and invoked once per request
  by `JwtAuthFilter`, so there's a single place to change signing algorithm, add token blacklist
  checks, etc.
- **DTOs everywhere**: request/response objects are separate from JPA entities so the API
  contract doesn't leak lazy-loading proxies or expose password hashes.
- **Global exception handling** via `@RestControllerAdvice` keeps controllers free of
  try/catch and gives consistent JSON error shapes across the app.

## 8. Extending This Project

- Add refresh-token rotation (issue a longer-lived refresh token at login, store its hash,
  expose `/api/auth/refresh`).
- Add token blacklisting/logout by tracking revoked JTIs (add a `jti` claim + a revoked-token table).
- Swap the in-code role check strings for a `@PreAuthorize("hasAuthority('PRODUCT_WRITE')")
  permission model if you outgrow simple USER/ADMIN roles.
- Add Swagger/OpenAPI (`springdoc-openapi-starter-webmvc-ui`) — the `/v3/api-docs/**` and
  `/swagger-ui/**` paths are already whitelisted in `SecurityConfig`.
