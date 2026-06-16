#!/bin/bash

# start-prod.sh - Start Cognitive Banking Application in Production Mode

set -e  # Exit on any error

echo "🚀 Starting Cognitive Banking Application in Production Mode..."
echo "==============================================================="

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Load environment variables
if [ -f .env ]; then
    echo "📁 Loading environment variables from .env file"
    export $(cat .env | grep -v '^#' | xargs)
else
    echo "⚠️  No .env file found. Using default environment variables."
fi

# Start infrastructure
echo "🐳 Starting PostgreSQL and Redis containers..."
docker-compose up -d

# Wait for databases to be ready
echo "⏳ Waiting for databases to be ready..."
sleep 10

# Check if PostgreSQL is healthy
if [ "$(docker inspect -f '{{.State.Health.Status}}' cognitive-bank-db)" != "healthy" ]; then
    echo "❌ PostgreSQL is not healthy. Check logs with: docker-compose logs postgres"
    exit 1
fi

# Check if Redis is healthy
if [ "$(docker inspect -f '{{.State.Health.Status}}' cognitive-bank-redis)" != "healthy" ]; then
    echo "❌ Redis is not healthy. Check logs with: docker-compose logs redis"
    exit 1
fi

echo "✅ Infrastructure is ready!"
echo "📊 PostgreSQL: localhost:5432"
echo "🔴 Redis: localhost:6379"
echo "🖥️  pgAdmin: http://localhost:8081 (admin@cognitivebank.com / admin123)"

# Build the application (optional - remove if you want to run without building)
echo "🔨 Building application..."
./mvnw clean package -DskipTests

# Start the application
echo "🎯 Starting Spring Boot application with production profile..."
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod -Dspring-boot.run.jvmArguments="-Xmx512m" &

# Save the PID
APP_PID=$!
echo $APP_PID > .app.pid

echo "✅ Application started with PID: $APP_PID"
echo "🌐 Application will be available at: http://localhost:8080/api/cognitive/bank"
echo "📈 Actuator endpoints: http://localhost:8080/api/cognitive/bank/actuator/health"

# Wait a bit and check if application started successfully
sleep 10
if ps -p $APP_PID > /dev/null; then
    echo "🎉 Application is running successfully!"
    echo "💡 Use './stop.sh' to stop the application"
else
    echo "❌ Application failed to start. Check logs above."
    exit 1
fi