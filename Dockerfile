# Dockerfile - multi-stage for Spring Boot (Maven build)

# --- build stage ---
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /workspace
# copy pom and sources for dependency cache
COPY pom.xml mvnw* ./
COPY .mvn .mvn
# copy only necessary to run mvn dependency:go-offline faster
RUN mvn -B -DskipTests dependency:resolve

# copy full source
COPY src ./src
# package
RUN mvn -B -DskipTests package

# --- runtime stage ---
FROM eclipse-temurin:17-jdk-jammy
ARG JAR_FILE=/workspace/target/*.jar
COPY --from=build ${JAR_FILE} /app/app.jar

# if you are using a config file externalization pattern, mount it, else default
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /app/app.jar" ]
