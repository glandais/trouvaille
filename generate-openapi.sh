#!/bin/bash

# OpenAPI code generation script for Trouvaille application
# This script generates TypeScript client code and Java server interfaces from the OpenAPI contract

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

# Check if contract.yaml exists
if [ ! -f "contract.yaml" ]; then
    log_error "contract.yaml not found in root directory"
    exit 1
fi

echo ""
log_info "🔄 Generating OpenAPI client and server code from contract.yaml"
echo ""

# 1. Copy contract file to both projects
log_info "📋 Copying contract file to frontend and backend..."
cp contract.yaml trouvaille-front/ || {
    log_error "Failed to copy contract.yaml to trouvaille-front/"
    exit 1
}

cp contract.yaml trouvaille-back/src/main/resources/openapi/ || {
    log_error "Failed to copy contract.yaml to trouvaille-back/src/main/resources/openapi/"
    exit 1
}

# 2. Generate TypeScript client for frontend
log_info "🎨 Generating TypeScript client for frontend..."
cd trouvaille-front

# Check if npm is available
if ! command -v npm &> /dev/null; then
    log_error "npm is not installed or not in PATH"
    exit 1
fi

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
    log_warning "node_modules not found. Installing dependencies..."
    npm ci || {
        log_error "Failed to install frontend dependencies"
        exit 1
    }
fi

# Generate TypeScript API client
npm run generate:api || {
    log_error "Failed to generate TypeScript API client"
    exit 1
}

log_success "✅ TypeScript client generated successfully"

cd ..

# 3. Generate Java server interfaces for backend
log_info "☕ Generating Java server interfaces for backend..."
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

# Clean and generate code (compile triggers the generate-code phase)
$MVN_CMD clean compile || {
    log_error "Failed to generate Java server interfaces"
    exit 1
}

log_success "✅ Java server interfaces generated successfully"

cd ..

# 4. Verify generated files
log_info "🔍 Verifying generated files..."

# Check frontend generated files
if [ -d "trouvaille-front/src/api" ]; then
    FRONTEND_FILES=$(find trouvaille-front/src/api -name "*.ts" | wc -l)
    log_info "   Frontend: $FRONTEND_FILES TypeScript files generated"
else
    log_warning "   Frontend: API directory not found"
fi

# Check backend generated files
if [ -d "trouvaille-back/target/generated-sources" ]; then
    BACKEND_FILES=$(find trouvaille-back/target/generated-sources -name "*.java" | wc -l)
    log_info "   Backend: $BACKEND_FILES Java files generated"
else
    log_warning "   Backend: Generated sources directory not found"
fi

echo ""
log_success "🎉 OpenAPI code generation completed successfully!"
echo ""
log_info "Generated files:"
log_info "  📁 Frontend TypeScript client: trouvaille-front/src/api/"
log_info "  📁 Backend Java interfaces: trouvaille-back/target/generated-sources/"
echo ""
log_info "Next steps:"
log_info "  1. Review generated code for any breaking changes"
log_info "  2. Update implementations if needed"
log_info "  3. Run tests to ensure compatibility"
log_info "  4. Build the application: ./build.sh"
echo ""