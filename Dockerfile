# Stage 1 — Build
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle build --no-daemon

# Stage 2 — Run
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=builder /app/build/libs/telegram-bot.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]