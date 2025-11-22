# TaskManager Quarkus – Secured Task Manager

A full-stack task management application built with **Quarkus 3.x** featuring true hybrid authentication:

- Classic form-based login for browser users  
- JWT-protected REST API (`/api/**`) for mobile/SPA clients  
- Role-based access control (USER / ADMIN)

Perfect for browser users and third-party clients simultaneously.

**Source Code:** [http://gitlab.hss.fmi.uni-sofia.bg/dimanas/secured_task_manager.git](https://github.com/marmotka/master_thesis_security/blob/quarkus-task-manager/README.md)


## Features

- User registration & form-based login
- Protected web pages (`/tasks/*`, `/profile/*`, `/admin/*`)
- JWT-protected REST API (`/api/*`)
- File uploads
- Responsive HTML/CSS/JS frontend
- PostgreSQL + Hibernate ORM + Panache
- Docker + docker-compose ready

## Tech Stack

| Layer               | Technology                                    |
|---------------------|-----------------------------------------------|
| Framework           | Quarkus 3.x (fast-jar)                        |
| Reactive/Core       | Vert.x                                        |
| Security            | SmallRye JWT (RS256) + Form Authentication    |
| Persistence         | Hibernate ORM with Panache                    |
| Database            | PostgreSQL                                    |
| Build Tool          | Maven                                         |
| Templating          | Qute                                          |
| Containerization    | Docker + docker-compose                       |
| JWT Keys            | RSA 2048-bit (PKCS#8)                         |

## Project Structure (Important!)
```
textsecured_task_manager/
├─ src/main/resources/keys/     ← only for local development
├─ secrets/                     ← NEVER commit to Git! (created manually)
├─ docker-compose.yml
└─ ...
```

## Generate JWT Key Pair (Required Once)

Run these commands in the project root:

```bash
## Create secrets directory
mkdir -p secrets

## Generate 2048-bit RSA private key (PKCS#8 – the format Quarkus expects)
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out secrets/privateKey.pem

## Extract the matching public key
openssl rsa -pubout -in secrets/privateKey.pem -out secrets/publicKey.pem

## Also copy public key for local dev (optional but convenient)
cp secrets/publicKey.pem src/main/resources/keys/
```

**Important:** `secrets/privateKey.pem` must **never** be committed to Git!

## Local Development (Dev Mode)

### Requirements
- JDK 21
- Maven 3.9+
- Docker + docker-compose (optional but recommended)

### Run

```
bash
# Start PostgreSQL (optional – Quarkus can start it automatically in dev)
docker compose up db -d

# Start the app with hot-reload
./mvnw quarkus:dev

```

Open → http://localhost:8080
Form login and JWT API work immediately.

## Production / Server Deployment
### Requirements

Docker & docker-compose

### Run
```
Bash 
 Make sure secrets/ contains both publicKey.pem and privateKey.pem
# (generated in the step above)

docker compose up --build
```
The application starts in production mode and is available at http://localhost:8080
The container automatically:

- Connects to PostgreSQL
- Mounts the secrets/ folder
- Uses the keys at runtime (private key never leaves the host)
