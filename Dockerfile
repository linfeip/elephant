# syntax=docker/dockerfile:1

############################################
# Build stage: compile Spring Boot with JDK 24
############################################
FROM maven:3.9.8-eclipse-temurin-24 AS build
WORKDIR /workspace

# Pre-fetch dependencies for better layer caching
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests dependency:go-offline

# Copy sources and build
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests package

############################################
# Runtime stage: run on Java 24 JRE
############################################
FROM eclipse-temurin:24-jre
WORKDIR /app

# Copy the built jar (assumes only one application jar produced)
COPY --from=build /workspace/target/*.jar /app/

# Normalize jar name to app.jar for stable entrypoint
RUN set -eux; JAR=$(ls -1 /app/*.jar | head -n 1); mv "$JAR" /app/app.jar

EXPOSE 8080

# Tweakable JVM options
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -Dfile.encoding=UTF-8"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

