#!/bin/bash

# Deployment script for Trouvaille application
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

# Configuration
ACTION="${1:-up}"
BUILD_TYPE="${2:-native}"
ENVIRONMENT="${3:-production}"  # production, development

case "$ACTION" in
    "build")
        log_info "Building images..."
        ./build.sh latest $BUILD_TYPE
        ;;
    "up"|"start")
        log_info "Starting services in $ENVIRONMENT mode..."
        
        # Check if .env file exists
        if [ ! -f ".env" ]; then
            log_warning ".env file not found. Creating from template..."
            cp .env.example .env
            log_warning "Please edit .env file with your OAuth credentials before running again"
            exit 1
        fi
        
        # Create necessary directories based
        mkdir -p data/{photos,mongodb,keys}
        
        # Select compose files based on environment
        COMPOSE_FILES="-f docker-compose.yml"
        if [ "$ENVIRONMENT" = "production" ]; then
            COMPOSE_FILES="$COMPOSE_FILES -f docker-compose.prod.yml"
            log_info "Using production configuration with enhanced security"
        fi
        
        log_info "Running docker compose $COMPOSE_FILES up -d"

        # Start services
        docker compose $COMPOSE_FILES up -d
        
        log_success "Services started!"
        log_info "Waiting for services to be ready..."
        sleep 10
        
        # Health checks
        log_info "Checking service health..."
        
        # Check if containers are running
        if docker compose ps | grep -q "Up"; then
            log_success "Containers are running"
        else
            log_error "Some containers failed to start"
            docker compose logs
            exit 1
        fi
        
        HTTP_PORT=$(grep "^HTTP_PORT=" .env 2>/dev/null | cut -d'=' -f2 || echo "8090")

        log_success "Deployment completed!"
        echo ""
        log_info "Access points:"
        log_info "  Application: http://localhost:${HTTP_PORT}"
        log_info "  MongoDB: localhost:27017"
        if [ "$ENVIRONMENT" = "production" ]; then
            log_info "  Only accessible through Traefik on port ${HTTP_PORT}"
            log_info "  Configure your Nginx to proxy to http://localhost:${HTTP_PORT}"
        else
            log_info "  Traefik Dashboard: http://localhost:28080"
        fi
        
        echo ""
        log_info "To view logs: ./deploy.sh logs"
        log_info "To stop: ./deploy.sh stop"
        ;;
    "down"|"stop")
        log_info "Stopping services..."
        docker compose down
        log_success "Services stopped"
        ;;
    "restart")
        log_info "Restarting services..."
        docker compose down
        docker compose up -d
        log_success "Services restarted"
        ;;
    "logs")
        docker compose logs -f
        ;;
    "status")
        docker compose ps
        ;;
    "clean")
        log_warning "This will remove all containers, images, and volumes!"
        read -p "Are you sure? (y/N): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            docker compose down -v --rmi all
            docker system prune -f
            log_success "Cleanup completed"
        else
            log_info "Cleanup cancelled"
        fi
        ;;
    "help"|*)
        cat << EOF
Trouvaille Deployment Script

Usage: $0 [ACTION] [BUILD_TYPE] [ENVIRONMENT]

Actions:
  build [native|jvm]            Build Docker images
  up|start [native|jvm] [env]   Start all services
  down|stop                     Stop all services  
  restart                       Restart all services
  logs                          Show logs from all services
  status                        Show status of all services
  clean                         Remove all containers, images and volumes
  help                          Show this help message

Build Types (for build action):
  native            Build Quarkus native image (default, slower build, faster runtime)
  jvm               Build Quarkus JVM image (faster build, slower runtime)

Environments:
  production        Secure configuration, no exposed ports (default)
  development       Development tools, localhost-only port exposure

Examples:
  $0 build native                   # Build native images
  $0 up native production           # Start in production mode
  $0 up jvm development             # Start in development mode with debug tools
  $0 logs                           # View logs
  $0 clean                          # Clean everything

Access in development mode:
  - Application: http://localhost
  - Backend API: http://localhost:8081/api
  - Frontend: http://localhost:8082
  - Mongo Express: http://localhost:8083
  - Traefik Dashboard: http://localhost:8080

Access in production mode:
  - Application: http://localhost (Traefik only)
  - All other services: Internal network only

EOF
        ;;
esac