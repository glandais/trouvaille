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
BACKEND_IMAGE="ghcr.io/glandais/trouvaille-backend"
FRONTEND_IMAGE="ghcr.io/glandais/trouvaille-frontend"
TAG="${1:-latest}"

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

# Build backend
log_info "Building backend image..."
cd trouvaille-back

log_info "Compiling Quarkus JVM image with Maven..."
export QUARKUS_CONTAINER_IMAGE_IMAGE="${BACKEND_IMAGE}:${TAG}"
./mvnw clean package -DskipTests -Dquarkus.container-image.build=true

if [ $? -ne 0 ]; then
    log_error "Failed to build JAR"
    exit 1
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
docker images | grep -E "(trouvaille-backend|trouvaille-frontend)" | head -10

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
log_info "  ./deploy.sh up"
echo ""
log_info "To access the application:"
log_info "  Frontend: http://localhost"
log_info "  Backend API: http://localhost/api"
log_info "  Traefik Dashboard: http://localhost:8080"
echo ""
