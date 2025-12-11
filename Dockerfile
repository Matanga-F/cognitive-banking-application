# =========================
# 🔹 MAVEN BUILD STAGE
# =========================
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /workspace

# Cache dependencies first
COPY pom.xml mvnw* ./
COPY .mvn .mvn
RUN mvn -B -DskipTests dependency:go-offline

# Copy full source
COPY src ./src

# Build JAR
RUN mvn -B -DskipTests package

# =========================
# 🔹 RUNTIME STAGE
# =========================
FROM eclipse-temurin:17-jdk-jammy

ARG JAR_FILE=/workspace/target/*.jar
COPY --from=build ${JAR_FILE} /app/app.jar

EXPOSE 8080
ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
