# TaskManager Spring – Secured Task Manager

A full-stack task management application built with **Spring Boot 3.x** + **Spring Security** + **JWT** featuring true hybrid authentication:

- Classic form-based login for browser users
- JWT-protected REST API (`/api/**`)
- Role-based access control (USER / ADMIN)

**Source Code:** 

https://github.com/marmotka/master_thesis_security/blob/spring-task-manager/README.md


## Features

- User registration & login (form + JWT)
- Protected web pages (`/tasks/**`, `/profile/**`, `/admin/**`)
- JWT-protected REST API (`/api/**`)
- File uploads
- Responsive frontend (Thymeleaf + Bootstrap)
- PostgreSQL + Spring Data JPA
- Docker + docker-compose ready

## Tech Stack

| Layer               | Technology                                      |
|---------------------|-------------------------------------------------|
| Framework           | Spring Boot 3.x                                 |
| Core                | Spring MVC                                      |
| Security            | Spring Security + JWT (stateless)               |
| Authentication      | Form Login + JWT Bearer Token                   |
| Persistence         | Spring Data JPA + Hibernate                     |
| Database            | PostgreSQL                                      |
| Build Tool          | Maven                                           |
| Templating          | Thymeleaf + Bootstrap                           |
| Containerization    | Docker + docker-compose                         |

## Project Structure
```
secured_task_manager_spring/
├─ src/main/resources/
│   └─ application.yml
├─ docker-compose.yml
├─ Dockerfile
└─ ...
```

## Local Development

### Requirements
- JDK 21
- Maven 3.9+
- Docker (recommended)

### Run

```bash
# Start PostgreSQL 
docker compose up db -d

# Run the application 
./mvnw spring-boot:run
```
Open → http://localhost:8080
Form login and API work immediately.

## Production / Server Deployment
### Requirements

Docker & docker-compose

### Run
```Bash # Clean start (deletes old DB volume if needed)
docker compose down -v

# Build and start everything
docker compose up --build
```

Application will be available at http://localhost:8080

The container automatically:

- Starts PostgreSQL and creates the `tm-spring` database
- Runs the Spring Boot app in production mode
- Uses the JWT secret from environment variable