## Etapa de compilación
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /workspace

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

RUN set -eux; \
    sed -i 's/\r$//' mvnw; \
    chmod +x mvnw; \
    ./mvnw -B -DskipTests dependency:go-offline

COPY src src

RUN set -eux; \
    sed -i 's/\r$//' mvnw; \
    ./mvnw -B -DskipTests clean package

## Etapa de ejecución

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN useradd --create-home --shell /bin/bash spring && mkdir -p /app/uploads && chown -R spring:spring /app

COPY --from=builder /workspace/target/*.jar app.jar

USER spring

EXPOSE 8080

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

