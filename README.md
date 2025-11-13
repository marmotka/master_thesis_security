Quarkus Security with JPA
========================

This guide demonstrates how your Quarkus application can use a database and JPA to store your user identities.

## Run quickstart in developer mode

Quarkus provides developer mode, in which you can try this example. Just try:

```bash
mvn quarkus:dev
```

[//]: # (todo)
DIMANA NOTES

Now the application will listen on `localhost:8080`.
In developer mode quarkus will also start its own postgres database.

## Run quickstart in JVM mode

## Run (Docker, prod profile)

1) Create keys (once):
   openssl genrsa -out secrets/privateKey.pem 2048
   openssl rsa -in secrets/privateKey.pem -pubout -out secrets/publicKey.pem

sql
Copy code

2) Build & start:
   docker compose build
   docker compose up -d

javascript
Copy code

3) Open http://localhost:8080

Notes:
- The app reads JWT keys from `/opt/keys` inside the container (mounted from `./secrets`).
- Dev mode (`mvn quarkus:dev`) uses demo keys on the classpath; prod uses the mounted keys.


1. Set ENV: export DB_HOST=localhost DB_PORT=5432 DB_NAME=tm-quarkus DB_USER=postgres DB_PASS=his_pass DB_TIMEZONE=Europe/Kyiv
2. Ensure Postgres running + keys/*.pem in src/main/resources.
3. Build: mvn clean package
4. Run: java -jar target/quarkus-run.jar
5. Access: http://server:8081
6. Disable embedded users if using real DB auth: Comment out quarkus.security.users.embedded.*