#!/bin/bash

# Continuous Integration Build Script for Trouvaille
# This script builds and tests both frontend and backend components

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Print banner
cat << "EOF"
 _____                            _ _ _      
|_   _|                          (_) | |     
  | |_ __ ___  _   ___   ____ _ _| | | ___ 
  | | '__/ _ \| | | \ \ / / _` | | | |/ _ \
  | | | | (_) | |_| |\ V / (_| | | | |  __/
  \_/_|  \___/ \__,_| \_/ \__,_|_|_|_|\___|

Continuous Integration Build
EOF

echo ""

# Check prerequisites
log_info "Checking prerequisites..."

# Check Java
if ! command -v java &> /dev/null; then
    log_error "Java is not installed or not in PATH"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    log_error "Java 21 or higher is required. Found Java $JAVA_VERSION"
    exit 1
fi
log_info "✓ Java $JAVA_VERSION found"

# Check Node.js
if ! command -v node &> /dev/null; then
    log_error "Node.js is not installed or not in PATH"
    exit 1
fi

NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
if [ "$NODE_VERSION" -lt 18 ]; then
    log_error "Node.js 18 or higher is required. Found Node.js $NODE_VERSION"
    exit 1
fi
log_info "✓ Node.js $(node -v) found"

# Check npm
if ! command -v npm &> /dev/null; then
    log_error "npm is not installed or not in PATH"
    exit 1
fi
log_info "✓ npm $(npm -v) found"

# Generate OpenAPI code
log_info "🔄 Generating OpenAPI code..."
if [ -f "./generate-openapi.sh" ]; then
    ./generate-openapi.sh
    if [ $? -ne 0 ]; then
        log_error "Failed to generate OpenAPI code"
        exit 1
    fi
    log_success "OpenAPI code generated"
else
    log_warning "generate-openapi.sh not found, skipping OpenAPI generation"
fi

# Build Backend
log_info "🏗️ Building backend..."
cd trouvaille-back

log_info "Running Maven clean compile..."
./mvnw clean compile

if [ $? -ne 0 ]; then
    log_error "Backend compilation failed"
    exit 1
fi

log_info "Running backend tests..."
./mvnw test

if [ $? -ne 0 ]; then
    log_error "Backend tests failed"
    exit 1
fi

log_info "Running code formatting check..."
./mvnw spotless:check

if [ $? -ne 0 ]; then
    log_error "Backend code formatting check failed"
    log_info "Run './mvnw spotless:apply' to fix formatting issues"
    exit 1
fi

log_success "Backend build completed successfully"
cd ..

# Build Frontend
log_info "🎨 Building frontend..."
cd trouvaille-front

log_info "Installing npm dependencies..."
npm ci

if [ $? -ne 0 ]; then
    log_error "npm install failed"
    exit 1
fi

log_info "Running TypeScript type checking..."
npm run type-check

if [ $? -ne 0 ]; then
    log_error "TypeScript type checking failed"
    exit 1
fi

log_info "Running ESLint..."
npm run lint

if [ $? -ne 0 ]; then
    log_error "ESLint check failed"
    exit 1
fi

log_info "Building frontend for production..."
npm run build

if [ $? -ne 0 ]; then
    log_error "Frontend build failed"
    exit 1
fi

log_success "Frontend build completed successfully"
cd ..

# Summary
log_success "🎉 All builds completed successfully!"
echo ""
log_info "Summary:"
log_info "  ✓ Backend compiled and tested"
log_info "  ✓ Frontend built and type-checked"
log_info "  ✓ Code formatting validated"
log_info "  ✓ Linting passed"
echo ""
log_info "Ready for deployment!"