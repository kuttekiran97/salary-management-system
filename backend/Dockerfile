# Build stage: compile with Maven
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
# Download dependencies first (cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B
COPY src src
RUN ./mvnw package -DskipTests -B

# Run stage: slim JRE image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create directory for SQLite database
RUN mkdir -p /data

COPY --from=build /app/target/*.jar app.jar

# Render sets PORT; SQLite file lives on the persistent disk at /data
ENV PORT=8080
ENV DB_URL=jdbc:sqlite:/data/salary.db?foreign_keys=true

EXPOSE 8080

# Run with seed enabled on first start; seeding is skipped if data already exists
ENTRYPOINT ["java", "-jar", "app.jar", "--app.seed.enabled=true"]
