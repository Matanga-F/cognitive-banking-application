#!/bin/bash

# stop.sh - Stop Cognitive Banking Application

echo "🛑 Stopping Cognitive Banking Application..."
echo "============================================"

# Stop the Spring Boot application
if [ -f .app.pid ]; then
    APP_PID=$(cat .app.pid)
    if ps -p $APP_PID > /dev/null; then
        echo "⏹️  Stopping application (PID: $APP_PID)..."
        kill $APP_PID
        sleep 3

        # Force kill if still running
        if ps -p $APP_PID > /dev/null; then
            echo "⚠️  Application still running, forcing shutdown..."
            kill -9 $APP_PID
        fi

        echo "✅ Application stopped."
    else
        echo "ℹ️  Application was not running."
    fi
    rm -f .app.pid
else
    echo "ℹ️  No application PID file found."
fi

# Ask if user wants to stop infrastructure
read -p "Do you want to stop PostgreSQL and Redis containers? (y/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "🐳 Stopping infrastructure containers..."
    docker-compose down
    echo "✅ Infrastructure containers stopped."
else
    echo "ℹ️  Infrastructure containers are still running."
    echo "💡 You can stop them later with: docker-compose down"
fi

echo "🎯 All services have been stopped!"