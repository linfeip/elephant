# syntax=docker/dockerfile:1

############################################
# Build stage: compile Spring Boot with JDK 24
############################################
FROM dhub.kubesre.xyz/maven:3.9.11 AS build
WORKDIR /workspace

# Use Aliyun Maven mirror for faster dependency downloads inside China
COPY maven-settings.xml /usr/share/maven/conf/settings.xml

# Pre-fetch dependencies for better layer caching
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -s /usr/share/maven/conf/settings.xml -B -DskipTests dependency:go-offline

# Copy sources and build
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 bash -lc 'mvn -s /usr/share/maven/conf/settings.xml -B -DskipTests package && JAR=$(ls -1 target/*.jar | head -n 1) && mv "$JAR" target/app.jar'

############################################
# Runtime stage: run on JRE
############################################
FROM dhub.kubesre.xyz/ubuntu/jre:21-24.04_stable
WORKDIR /app

# Copy the normalized application jar
COPY --from=build /workspace/target/app.jar /app/app.jar

EXPOSE 8080

# Tweakable JVM options via JAVA_TOOL_OPTIONS (picked up automatically by JVM)
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -Dfile.encoding=UTF-8"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

