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

case "$ACTION" in
    "build")
        log_info "Building images..."
        ./build.sh latest
        ;;
    "up"|"start")
        log_info "Starting services..."
        
        # Check if .env file exists
        if [ ! -f ".env" ]; then
            log_warning ".env file not found. Creating from template..."
            cp .env.example .env
            log_warning "Please edit .env file with your OAuth credentials before running again"
            exit 1
        fi
        
        # Create necessary directories based
        mkdir -p data/{photos,mongodb,keys}
        
        log_info "Running docker compose up -d"

        # Start services
        docker compose up -d
        
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

Usage: $0 [ACTION]

Actions:
  build                         Build Docker images
  up|start                      Start all services
  down|stop                     Stop all services  
  restart                       Restart all services
  logs                          Show logs from all services
  status                        Show status of all services
  clean                         Remove all containers, images and volumes
  help                          Show this help message

Examples:
  $0 build                          # Build images
  $0 up                             # Start
  $0 logs                           # View logs
  $0 clean                          # Clean everything

Access in production mode:
  - Application: http://localhost (Traefik only)
  - All other services: Internal network only

EOF
        ;;
esac