#!/bin/bash

# Load SDKMAN and use Java version from .sdkmanrc
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk env

# Define the static service name
SERVICE_NAME="lms-service-gateway"
JAR_NAME="$SERVICE_NAME.jar"
LOG_FILE="$SERVICE_NAME"

# Build the microservice using Maven Wrapper
echo "Building $SERVICE_NAME..."
./mvnw clean package -DskipTests || { echo "Build failed"; exit 1; }

# Ensure the JAR file exists after the build
if [ ! -f "target/$JAR_NAME" ]; then
    echo "Error: JAR file 'target/$JAR_NAME' not found."
    exit 1
fi

# Delete the existing log file and create a new one
echo "🗑️ Deleting old log file..."
rm -f "$LOG_FILE"
touch "$LOG_FILE"

# Start the service with Spring profile set to local
echo "Starting $SERVICE_NAME with profile 'local'..."
nohup java -jar "target/$JAR_NAME" --spring.profiles.active=local > "$LOG_FILE.log" 2>&1 &
echo "$SERVICE_NAME started."
