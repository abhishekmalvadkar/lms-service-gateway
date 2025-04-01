#!/bin/bash

# Load SDKMAN if not already loaded
if [ -z "$SDKMAN_DIR" ]; then
    export SDKMAN_DIR="$HOME/.sdkman"
fi

# Ensure SDKMAN is installed
if [ ! -d "$SDKMAN_DIR" ]; then
    echo "SDKMAN is not installed. Please install it first: https://sdkman.io/install"
    exit 1
fi

# Check if .sdkmanrc exists
if [ ! -f ".sdkmanrc" ]; then
    echo "Error: .sdkmanrc file not found in the current directory."
    exit 1
fi

# Load SDKMAN
source "$SDKMAN_DIR/bin/sdkman-init.sh"

# Use SDKMAN to install and switch to the required JDK version
echo "Installing JDK version specified in .sdkmanrc..."
sdk env install

echo "JDK setup complete. Current version:"
java -version
