#!/bin/bash

# Code formatting script for Trouvaille application
# This script formats both frontend and backend code using their respective tools

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

# Check if we should just check formatting (used by git hooks)
CHECK_ONLY=false
if [ "$1" = "--check" ] || [ "$1" = "-c" ]; then
    CHECK_ONLY=true
    log_info "🔍 Checking code formatting..."
else
    log_info "🎨 Formatting code..."
fi

echo ""

# Format frontend
log_info "📄 Frontend (TypeScript/Vue.js)..."
cd trouvaille-front

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
    log_warning "node_modules not found. Installing dependencies..."
    npm ci || {
        log_error "Failed to install frontend dependencies"
        exit 1
    }
fi

if [ "$CHECK_ONLY" = true ]; then
    # Check formatting without modifying files
    npm run format:check || {
        log_error "Frontend code formatting check failed"
        echo "Run './format.sh' to fix formatting issues"
    }
    npm run lint:check || {
        log_error "Frontend linting check failed"
        echo "Run 'npm run lint' in trouvaille-front/ to fix linting issues"
    }
    log_success "✅ Frontend formatting check passed"
else
    # Format and fix issues
    npm run format || {
        log_error "Failed to format frontend code"
    }
    npm run lint || {
        log_error "Failed to lint frontend code"
    }
    log_success "✅ Frontend code formatted successfully"
fi

cd ..

# Format backend
log_info "☕ Backend (Java)..."
cd trouvaille-back

# Check if Maven is available
if ! command -v mvn &> /dev/null && [ ! -f "./mvnw" ]; then
    log_error "Maven is not installed and mvnw wrapper not found"
    exit 1
fi

# Use Maven wrapper if available, otherwise use system Maven
MVN_CMD="mvn"
if [ -f "./mvnw" ]; then
    MVN_CMD="./mvnw"
fi

if [ "$CHECK_ONLY" = true ]; then
    # Check formatting without modifying files
    $MVN_CMD spotless:check || {
        log_error "Backend code formatting check failed"
        echo "Run './format.sh' to fix formatting issues"
        exit 1
    }
    log_success "✅ Backend formatting check passed"
else
    # Format code
    $MVN_CMD spotless:apply || {
        log_error "Failed to format backend code"
        exit 1
    }
    log_success "✅ Backend code formatted successfully"
fi

cd ..

echo ""
if [ "$CHECK_ONLY" = true ]; then
    log_success "🎉 All code formatting checks passed!"
    echo ""
    log_info "No formatting issues found in:"
    log_info "  📁 Frontend: TypeScript, Vue.js, JSON, Markdown, YAML"
    log_info "  📁 Backend: Java, POM files"
else
    log_success "🎉 Code formatting completed successfully!"
    echo ""
    log_info "Formatted files in:"
    log_info "  📁 Frontend: TypeScript, Vue.js, JSON, Markdown, YAML"
    log_info "  📁 Backend: Java, POM files"
    echo ""
    log_info "Next steps:"
    log_info "  1. Review the changes: git diff"
    log_info "  2. Stage the changes: git add ."
    log_info "  3. Commit the changes: git commit"
fi
echo ""