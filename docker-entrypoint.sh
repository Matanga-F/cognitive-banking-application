#!/bin/sh
set -e

# Set memory options if not set
if [ -z "$JAVA_OPTS" ]; then
    JAVA_OPTS="-Xmx512m -Xms256m"
fi

# Spring profile
if [ -z "$SPRING_PROFILES_ACTIVE" ]; then
    SPRING_PROFILES_ACTIVE=default
fi

# Start the app
exec java $JAVA_OPTS -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -jar /app.jar