# ---- Build Stage ----
FROM gradle:9.4.1-jdk21 AS builder

WORKDIR /app

COPY gradlew .
RUN chmod +x gradlew
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# 의존성만 먼저 캐싱
RUN ./gradlew dependencies --no-daemon --parallel -q

COPY src src

RUN ./gradlew bootJar --no-daemon --parallel -x test && \
    find build/libs -name "*.jar" ! -name "*-plain.jar" -exec mv {} build/libs/app.jar \;

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S aikon && adduser -S aikon -G aikon

COPY --chown=aikon:aikon --from=builder /app/build/libs/app.jar app.jar

USER aikon

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
