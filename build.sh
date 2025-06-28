#!/bin/bash

# Build script for Trouvaille application
# This script builds Docker images for both frontend and backend

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BACKEND_IMAGE="ghcr.io/glandais/trouvaille-backend/backend"
FRONTEND_IMAGE="ghcr.io/glandais/trouvaille-frontend"
TAG="${1:-latest}"
BUILD_TYPE="${2:-native}" # native or jvm

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

cleanup() {
    log_info "Cleaning up temporary files..."
    # Remove any temporary files if needed
}

# Trap to cleanup on exit
trap cleanup EXIT

# Print banner
cat << "EOF"
 _____                            _ _ _      
|_   _|                          (_) | |     
  | |_ __ ___  _   ___   ____ _ _| | | ___ 
  | | '__/ _ \| | | \ \ / / _` | | | |/ _ \
  | | | | (_) | |_| |\ V / (_| | | | |  __/
  \_/_|  \___/ \__,_| \_/ \__,_|_|_|_|\___|

Building Docker images...
EOF

echo ""

# Check if Docker is available
if ! command -v docker &> /dev/null; then
    log_error "Docker is not installed or not in PATH"
    exit 1
fi

# Check if Docker is running
if ! docker info &> /dev/null; then
    log_error "Docker daemon is not running"
    exit 1
fi

log_info "Using tag: $TAG"
log_info "Backend build type: $BUILD_TYPE"

# Generate OpenAPI code for both frontend and backend
log_info "🔄 Generating OpenAPI code..."
./generate-openapi.sh

if [ $? -ne 0 ]; then
    log_error "Failed to generate OpenAPI code"
    exit 1
fi

# Build backend
log_info "Building backend image..."
cd trouvaille-back

if [ "$BUILD_TYPE" = "native" ]; then
    log_info "Building Quarkus native image (this may take several minutes)..."
    
    # Check if we have enough memory for native build
    AVAILABLE_MEMORY=$(free -m | awk 'NR==2{printf "%.0f", $7}')
    if [ "$AVAILABLE_MEMORY" -lt 4096 ]; then
        log_warning "Available memory is ${AVAILABLE_MEMORY}MB. Native build requires at least 4GB."
        log_warning "Consider using 'jvm' build type: ./build.sh latest jvm"
    fi
    
    # Build native executable first
    log_info "Compiling native executable with Maven..."
    ./mvnw clean package -Dnative -DskipTests
    
    if [ $? -ne 0 ]; then
        log_error "Failed to build native executable"
        exit 1
    fi
    
    # Build Docker image using our adapted native Dockerfile
    log_info "Building native Docker image..."
    docker build -f Dockerfile.native-trouvaille -t ${BACKEND_IMAGE}:${TAG} .
else
    log_info "Building Quarkus JVM image..."
    
    # Build JAR first
    log_info "Compiling JAR with Maven..."
    ./mvnw clean package -DskipTests
    
    if [ $? -ne 0 ]; then
        log_error "Failed to build JAR"
        exit 1
    fi
    
    # Build Docker image using our adapted JVM Dockerfile
    log_info "Building JVM Docker image..."
    docker build -f Dockerfile.jvm-trouvaille -t ${BACKEND_IMAGE}:${TAG} .
fi

if [ $? -eq 0 ]; then
    log_success "Backend image built successfully: ${BACKEND_IMAGE}:${TAG}"
else
    log_error "Failed to build backend image"
    exit 1
fi

cd ..

# Build frontend
log_info "Building frontend image..."
cd trouvaille-front

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
    log_warning "node_modules not found. Run 'npm install' first for better build caching."
fi

docker build -t ${FRONTEND_IMAGE}:${TAG} .

if [ $? -eq 0 ]; then
    log_success "Frontend image built successfully: ${FRONTEND_IMAGE}:${TAG}"
else
    log_error "Failed to build frontend image"
    exit 1
fi

cd ..

# Display built images
log_info "Built images:"
docker images | grep -E "(trouvaille/backend|trouvaille/frontend)" | head -10

# Check total size
BACKEND_SIZE=$(docker images ${BACKEND_IMAGE}:${TAG} --format "table {{.Size}}" | tail -1)
FRONTEND_SIZE=$(docker images ${FRONTEND_IMAGE}:${TAG} --format "table {{.Size}}" | tail -1)

log_success "Build completed successfully!"
echo ""
log_info "Image sizes:"
log_info "  Backend:  $BACKEND_SIZE"
log_info "  Frontend: $FRONTEND_SIZE"
echo ""
log_info "To start the application:"
log_info "  docker compose up -d"
echo ""
log_info "To access the application:"
log_info "  Frontend: http://localhost"
log_info "  Backend API: http://localhost/api"
log_info "  Traefik Dashboard: http://localhost:8080"
echo ""

# Optional: Run basic health checks
if command -v curl &> /dev/null; then
    log_info "To verify the deployment after docker compose up:"
    log_info "  curl http://localhost/health"
    log_info "  curl http://localhost/api/health"
fi