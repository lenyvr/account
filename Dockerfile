# Etapa 1: Compilación del proyecto
FROM gradle:8.14-jdk21-alpine AS build
WORKDIR /app

# 1.Copy Gradle base files for store dependencies in cache
COPY gradle gradle
COPY gradlew .
COPY settings.gradle.kts .
COPY build.gradle.kts .

# 2. Copy configuration files from each hexagonal module
COPY application/build.gradle.kts application/
COPY domain/build.gradle.kts domain/
COPY infrastructure/build.gradle.kts infrastructure/

# Download dependencies without compiling the code
RUN ./gradlew build -x test --no-daemon || true

# 3. Copy source code from all architechture
COPY application/src application/src
COPY domain/src domain/src
COPY infrastructure/src infrastructure/src

# 4. Compile the infrastructure module (this one gets application and domain)
RUN ./gradlew :infrastructure:bootJar -x test --no-daemon

# Stage 2: Final image Imagen final lightweight for execution
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 5. Copy generated JAR (Spring Boot by default add the version name)
COPY --from=build /app/infrastructure/build/libs/infrastructure-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]