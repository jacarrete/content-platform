# ---------- Build stage ----------
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /app

# Copy Gradle wrapper and configs
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY gradle/libs.versions.toml gradle/libs.versions.toml
COPY src src

# Make wrapper executable
RUN chmod +x gradlew

# Optional: still safe to unset JAVA_HOME
ENV JAVA_HOME=
RUN ./gradlew clean bootJar --no-daemon --no-parallel

# ---------- Runtime stage ----------
FROM eclipse-temurin:25-jre

WORKDIR /app

# Copy the built JAR
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
