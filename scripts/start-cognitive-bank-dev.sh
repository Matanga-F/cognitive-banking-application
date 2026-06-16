#!/bin/bash

# start-dev.sh - Start Cognitive Banking Application in Development Mode

set -e  # Exit on any error

echo "🔧 Starting Cognitive Banking Application in Development Mode..."
echo "================================================================"

# Check if we want to start infrastructure for dev mode
read -p "Do you want to start PostgreSQL and Redis for development? (y/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "🐳 Starting development infrastructure..."
    docker-compose up -d postgres redis

    echo "⏳ Waiting for databases to be ready..."
    sleep 8

    echo "✅ Development infrastructure ready!"
    echo "📊 PostgreSQL: localhost:5432"
    echo "🔴 Redis: localhost:6379"
else
    echo "ℹ️  Using H2 in-memory database and no Redis for development."
fi

# Build the application
echo "🔨 Building application..."
./mvnw clean compile

# Start the application with dev profile
echo "🎯 Starting Spring Boot application with development profile..."
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.jvmArguments="-Xmx512m -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005" &

# Save the PID
APP_PID=$!
echo $APP_PID > .app.pid

echo "✅ Application started with PID: $APP_PID"
echo "🌐 Application available at: http://localhost:8080/api/cognitive/bank"
echo "🗄️  H2 Console: http://localhost:8080/api/cognitive/bank/h2-console"
echo "📈 Actuator: http://localhost:8080/api/cognitive/bank/actuator/health"
echo "🐛 Debug port: 5005"

# Wait and check if application started
sleep 8
if ps -p $APP_PID > /dev/null; then
    echo "🎉 Development server is running!"
    echo "💡 Use './stop.sh' to stop the application"
    echo "📝 Auto-reload is enabled for development"
else
    echo "❌ Application failed to start. Check logs above."
    exit 1
fi