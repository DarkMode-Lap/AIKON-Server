# ---- Build Stage ----
FROM gradle:9.4.1-jdk21 AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# 의존성만 먼저 캐싱
RUN ./gradlew dependencies --no-daemon --parallel -q || true

COPY src src

RUN ./gradlew bootJar --no-daemon --parallel -x test

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S aikon && adduser -S aikon -G aikon

COPY --from=builder /app/build/libs/*.jar app.jar

RUN chown aikon:aikon app.jar

USER aikon

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
