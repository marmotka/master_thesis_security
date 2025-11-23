# TaskManager Spring – Secured Task Manager

A full-stack task management application built with **Spring Boot 3.x** + **Spring Security** + **JWT** featuring true hybrid authentication:

- Classic form-based login for browser users
- Stateless JWT Bearer Token API (`/api/**`) with **asymmetric RS256** signing
- Role-based access control (USER / ADMIN)

**Source Code:** 

https://github.com/marmotka/master_thesis_security/blob/spring-task-manager/README.md


## Features

- User registration & login (form + JWT)
- Protected web pages (`/tasks/**`, `/profile/**`, `/admin/**`)
- JWT-protected REST API (`/api/**`)
- CSV task import (including admin override `?owner=username`)
- Responsive frontend (Thymeleaf + Bootstrap)
- PostgreSQL + Spring Data JPA
- Docker + docker-compose ready

## Tech Stack

| Layer               | Technology                                      |
|---------------------|-------------------------------------------------|
| Framework           | Spring Boot 3.x                                 |
| Core                | Spring MVC                                      |
| Security            | Spring Security + JWT (RS256 asymmetric)        |
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
│   ├─ application.yml
│   └─ keys/
│       ├── privateKey.pem
│       └── publicKey.pem
├─ docker-compose.yml
├─ Dockerfile
└─ ...
```

**Generate once (if you don’t have them):**
```bash
# Generate private key
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out src/main/resources/keys/privateKey.pem

# Extract public key
openssl rsa -in src/main/resources/keys/privateKey.pem -pubout -out src/main/resources/keys/publicKey.pem
```

## Local Development

### Requirements
- JDK 21
- Maven 3.9+
- Docker (recommended)

### Run

```
# Start PostgreSQL 
docker compose up db -d

# Run the application 
./mvnw spring-boot:run

# Run the application in debug mode
./mvnw spring-boot:run -D'spring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"'
```
Open → http://localhost:8080
Form login and API work immediately.

## Production / Server Deployment
### Requirements

Docker & docker-compose

### Run
The same keys from `src/main/resources/keys/` are automatically packaged into the JAR

**Security note:**

The private key never leaves the container and is only readable by the application user.

```Bash 
# Build and start everything
docker compose up --build
```

Application will be available at http://localhost:8080

The container automatically:

- Starts PostgreSQL and creates the `tm-spring` database
- Runs the Spring Boot app in production mode
- Uses the embedded RSA keys for RS256 JWT signing

# CSV Test Files for Task Import API
In folder `src/main/resources/csv` you can find test files to test import functionality. 

| File                         | Purpose                                 |
|------------------------------|-----------------------------------------|
| import_success.csv           | 5 valid tasks → 100% success            |
| import_with_errors.csv       | Contains invalid dates & missing fields |
| wrong_other_format_file.xlsx | Wrong type of file                      |
| malformed_headers.csv        | Wrong column names → rejection          |
| wrong_png_named_as_csv.csv   | other file renamed as .csv              |

