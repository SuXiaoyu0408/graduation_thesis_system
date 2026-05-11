# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

UMLmyself is a Spring Boot-based graduation thesis management system with multi-role authentication, JWT-based authorization, and Redis-backed verification codes. The system includes static HTML frontend pages using Tailwind CSS and vanilla JavaScript.

### Prerequisites
- Java 21 (set in `pom.xml`)
- MySQL 5.7+ (or compatible)
- Maven 3.6+ (or use included `mvnw` wrapper)
- Optional: Redis for SMS verification codes

## Common Development Tasks

### Building and Running
- **Run the application**: `./mvnw spring-boot:run` (or `mvn spring-boot:run`)
- **Build the JAR**: `./mvnw clean package`
- **Run tests**: `./mvnw test`
- **Run a specific test class**: `./mvnw test -Dtest=ClassName`
- **Run with Spring Boot DevTools hot reload**: Already enabled; modify code and the application will auto-restart.

### Database Setup
1. MySQL database required: `graduation_thesis_system_myself_b` (configured in `application.properties`)
2. SQL scripts available in `src/main/resources/static/db/` (but note some may have been deleted per git status)
3. JPA DDL-auto is set to `validate`; ensure tables exist before running.

### Frontend Development
- Static HTML pages in `src/main/resources/static/`
- JavaScript files in `src/main/resources/static/js/`
- Uses CDN for Tailwind CSS; no build step required.
- Access login page at `http://localhost:8080/login_page.html`

## Architecture

### Layer Structure
- **Entities** (`entity/`): JPA-mapped classes (User, Role, College, Major, ThesisProcess, etc.)
- **Repositories** (`repository/`): Spring Data JPA interfaces for data access
- **Services** (`service/`, `service/impl/`): Business logic with interface/implementation pattern
- **Controllers** (`controller/`): REST endpoints returning `ApiResponse<T>` wrapper
- **DTOs** (`dto/`): Data transfer objects for request/response payloads
- **Configuration** (`config/`): CORS, OpenAPI/Swagger, web converters
- **Utilities** (`util/`): JwtUtil, AdminUtil
- **Common** (`common/`): ApiResponse, BusinessException, GlobalExceptionHandler, RequireRole annotation
- **Aspect** (`aspect/`): RoleCheckAspect for authorization

### Authentication & Authorization
- **Login flow**: Two-step process: (1) POST `/login` returns available roles, (2) POST `/login/confirm-role` returns JWT token.
- **JWT tokens**: Contain `userId` and `roleId`. Secret key configurable via `jwt.secret`.
- **Role-based access**: Annotate controllers/methods with `@RequireRole` (supports `requireAdmin` flag or role code array).
- **AOP enforcement**: `RoleCheckAspect` intercepts annotated methods, validates token, and checks role permissions against database.
- **Token cleaning**: `JwtUtil.cleanToken()` removes whitespace and invalid characters from raw tokens.

### API Conventions
- **Uniform response**: All controllers return `ApiResponse<T>` with `code`, `message`, `data`.
- **Error handling**: `GlobalExceptionHandler` catches `BusinessException` and returns error responses.
- **Validation**: Uses `@Validated` on request DTOs with Spring Boot Starter Validation.
- **Documentation**: OpenAPI/Swagger available at `/swagger-ui.html` with JWT bearer security scheme.

### External Services
- **MySQL**: Primary datastore (configured in `spring.datasource.*`)
- **Redis**: Stores SMS verification codes with 5-minute TTL (optional; if not needed, comment out Redis config)
- **JWT**: HS256 signed tokens with configurable expiration (default 7 days)

## Configuration Notes

### `application.properties`
- Database connection points to `172.20.10.4:3306`; adjust `spring.datasource.url` for local development.
- JWT secret: `jwt.secret` (default provided but should be changed in production).
- Redis: Disabled by default (no password); set `spring.data.redis.host` if needed.
- Swagger: Paths `/api-docs` and `/swagger-ui.html`.

### CORS
- `CorsConfig` allows all origins, methods, headers, and credentials (adjust for production).

## Development Tips

- **Lombok**: All entity and DTO classes use `@Data`; ensure IDE has Lombok plugin installed.
- **Spring Boot DevTools**: Automatic restart on classpath changes; disable if causing issues.
- **Testing**: Only a basic `@SpringBootTest` exists; add unit tests for services and integration tests for controllers.
- **Database changes**: Modify entities and ensure migrations are handled (currently `validate` mode).
- **Role management**: Roles stored in `role` table; user-role mapping in `user_role`.
- **Frontend-backend communication**: JavaScript uses `fetch()` with `Authorization: Bearer <token>` header.
- **File uploads**: The `FileService` saves files to the `uploads/` directory (relative to application working directory). Ensure directory exists and has write permissions.
- **Test data**: `test_data_insert.sql` in root contains sample data for testing. Run after database schema creation.

## Important Paths

- Main application: `src/main/java/com/sxy/umlmyself/UmLmyselfApplication.java`
- API examples: `src/main/java/com/sxy/umlmyself/controller/AdminController.java`
- JWT utility: `src/main/java/com/sxy/umlmyself/util/JwtUtil.java`
- Role check aspect: `src/main/java/com/sxy/umlmyself/aspect/RoleCheckAspect.java`
- Static frontend: `src/main/resources/static/login_page.html`

## Related Documentation

- `markdown/PROJECT_DOCUMENTATION.md`: Comprehensive project documentation
- `markdown/API接口文档.md`: API documentation (Chinese)
- `markdown/后端代码详细说明文档.md`: Backend code details (Chinese)
- `markdown/PASSWORD_API_DOCUMENTATION.md`: Password/verification API docs