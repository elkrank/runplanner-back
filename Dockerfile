# syntax=docker/dockerfile:1.7

FROM maven:3.9.10-eclipse-temurin-25 AS builder
WORKDIR /app

COPY pom.xml ./
COPY src ./src

RUN mvn -B -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=builder /app/target/*.jar /app/app.jar

EXPOSE 3000

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
