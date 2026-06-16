# ============================================
# STAGE 1: BUILD
# ============================================
FROM maven:3.9.5-eclipse-temurin-17 AS builder

WORKDIR /build

COPY pom.xml .
RUN mvn dependency:resolve -B

COPY src ./src
RUN mvn clean package -DskipTests -B


# ============================================
# STAGE 2: RUNTIME
# ============================================
FROM eclipse-temurin:17-jre-jammy

LABEL maintainer="cognitive-banking-team@bank.com"
LABEL version="1.0.0"
LABEL description="Cognitive Banking Application"

# Install tools
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    curl \
    ca-certificates \
    netcat-openbsd && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Create user and directories
RUN groupadd -r appgroup && \
    useradd -r -g appgroup appuser && \
    mkdir -p /app/logs /app/temp && \
    chown -R appuser:appgroup /app

WORKDIR /app

# Copy JAR from builder
COPY --from=builder --chown=appuser:appgroup /build/target/*.jar app.jar

# ============================================
# CREATE ENTRYPOINT SCRIPT DIRECTLY (no COPY)
# ============================================
RUN printf '#!/bin/sh\n\
set -e\n\
\n\
echo "Starting Cognitive Banking Application..."\n\
\n\
# JVM options\n\
JAVA_OPTS="${JAVA_OPTS:--Xmx512m -Xms256m}"\n\
\n\
# Spring profile\n\
if [ -n "$SPRING_PROFILES_ACTIVE" ]; then\n\
    echo "Using profile: $SPRING_PROFILES_ACTIVE"\n\
    JAVA_OPTS="$JAVA_OPTS -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE"\n\
fi\n\
\n\
# Wait for database if configured\n\
if [ -n "$DB_HOST" ] && [ -n "$DB_PORT" ]; then\n\
    echo "Waiting for database at $DB_HOST:$DB_PORT..."\n\
    while ! nc -z $DB_HOST $DB_PORT; do\n\
        sleep 1\n\
    done\n\
    echo "Database is ready!"\n\
fi\n\
\n\
echo "Starting application..."\n\
exec java $JAVA_OPTS -jar /app/app.jar\n\
' > /app/docker-entrypoint.sh && \
chmod +x /app/docker-entrypoint.sh && \
sed -i 's/\r$//' /app/docker-entrypoint.sh

# Expose port
EXPOSE 8080

# Healthcheck
HEALTHCHECK \
  --interval=30s \
  --timeout=10s \
  --start-period=60s \
  --retries=3 \
  CMD curl -f http://localhost:8080/api/cognitive/bank/actuator/health/liveness || exit 1

# JVM defaults
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseContainerSupport -XX:+UseG1GC -XX:+UseStringDeduplication -Duser.timezone=UTC"

USER appuser

ENTRYPOINT ["/app/docker-entrypoint.sh"]