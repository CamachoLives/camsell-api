# syntax=docker/dockerfile:1

# ---- etapa de build ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Capa de dependencias: se reutiliza mientras el pom no cambie.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- etapa de runtime ----
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S camsell && adduser -S camsell -G camsell
COPY --from=build /build/target/camsell-api-*.jar app.jar
RUN chown -R camsell:camsell /app
USER camsell

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC"
ENV CAMSELL_PROFILE=prod
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q UP || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
