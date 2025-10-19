# 1. # Use JDK 21 for build stage
FROM eclipse-temurin:21-jdk-jammy AS build

# 2. Copy Maven wrapper and pom.xml for dependency caching
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# 3. Download dependencies (for caching)
RUN ./mvnw dependency:go-offline -B

# 4. Copy source code and build the application
COPY src src
RUN ./mvnw clean package -DskipTests

# 5. Create the runtime image (smaller)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the fat JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose app port
EXPOSE 8080

# Run app
ENTRYPOINT ["java", "-jar", "app.jar"]
