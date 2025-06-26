#!/bin/bash

# Git hooks installation script for Trouvaille application
# This script installs pre-commit hooks to ensure code quality

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

echo ""
log_info "🪝 Installing Git hooks for Trouvaille project..."
echo ""

# Check if we're in a git repository
if [ ! -d ".git" ]; then
    log_error "Not in a Git repository root directory"
    log_error "Please run this script from the root of the Trouvaille project"
    exit 1
fi

# Create hooks directory if it doesn't exist
mkdir -p .git/hooks

# Create pre-commit hook
log_info "📝 Creating pre-commit hook..."
cat > .git/hooks/pre-commit << 'EOF'
#!/bin/bash

# Trouvaille pre-commit hook
# This hook runs code formatting checks before allowing commits

echo "🔍 Running pre-commit checks..."

# Check if format.sh exists
if [ ! -f "./format.sh" ]; then
    echo "❌ format.sh script not found"
    exit 1
fi

# Run formatting check
./format.sh --check

# Check the exit code
if [ $? -ne 0 ]; then
    echo ""
    echo "❌ Pre-commit hook failed!"
    echo ""
    echo "🎨 To fix formatting issues, run:"
    echo "   ./format.sh"
    echo ""
    echo "📝 Then stage and commit your changes:"
    echo "   git add ."
    echo "   git commit"
    echo ""
    exit 1
fi

echo "✅ Pre-commit checks passed!"
EOF

# Make the pre-commit hook executable
chmod +x .git/hooks/pre-commit

# Create commit-msg hook for commit message validation
log_info "📝 Creating commit-msg hook..."
cat > .git/hooks/commit-msg << 'EOF'
#!/bin/bash

# Trouvaille commit-msg hook
# This hook validates commit message format

commit_regex='^(feat|fix|docs|style|refactor|test|chore|perf|ci|build|revert)(\(.+\))?: .{1,50}'

error_msg="❌ Invalid commit message format!

📋 Commit message should follow the Conventional Commits format:
   <type>[optional scope]: <description>

📝 Types: feat, fix, docs, style, refactor, test, chore, perf, ci, build, revert

✅ Examples:
   feat: add user authentication
   fix(api): resolve cors issue
   docs: update installation guide
   style: format code with prettier
   
💡 Current message: $(cat $1)"

if ! grep -qE "$commit_regex" "$1"; then
    echo "$error_msg"
    exit 1
fi
EOF

# Make the commit-msg hook executable
chmod +x .git/hooks/commit-msg

# Create pre-push hook for additional checks
log_info "📝 Creating pre-push hook..."
cat > .git/hooks/pre-push << 'EOF'
#!/bin/bash

# Trouvaille pre-push hook
# This hook runs additional checks before pushing to remote

echo "🚀 Running pre-push checks..."

# Check if there are any unstaged changes
if ! git diff-index --quiet HEAD --; then
    echo "⚠️  Warning: You have unstaged changes"
    echo "   Consider staging them with: git add ."
fi

# Run a quick build check for backend (if mvnw exists)
if [ -f "trouvaille-back/mvnw" ] || command -v mvn &> /dev/null; then
    echo "🔨 Quick build check for backend..."
    cd trouvaille-back
    
    MVN_CMD="mvn"
    if [ -f "./mvnw" ]; then
        MVN_CMD="./mvnw"
    fi
    
    # Just compile without running tests (quick check)
    if ! $MVN_CMD compile -q; then
        echo "❌ Backend compilation failed!"
        echo "   Fix compilation errors before pushing"
        exit 1
    fi
    
    cd ..
    echo "✅ Backend compilation check passed"
fi

# Run TypeScript check for frontend (if package.json exists)
if [ -f "trouvaille-front/package.json" ]; then
    echo "🔨 TypeScript check for frontend..."
    cd trouvaille-front
    
    if [ -d "node_modules" ]; then
        if ! npm run type-check; then
            echo "❌ Frontend TypeScript check failed!"
            echo "   Fix TypeScript errors before pushing"
            exit 1
        fi
        echo "✅ Frontend TypeScript check passed"
    else
        echo "⚠️  Frontend dependencies not installed, skipping TypeScript check"
    fi
    
    cd ..
fi

echo "✅ Pre-push checks completed successfully!"
EOF

# Make the pre-push hook executable
chmod +x .git/hooks/pre-push

# Test if the hooks are working
log_info "🧪 Testing hooks installation..."

if [ -x ".git/hooks/pre-commit" ]; then
    log_success "✅ pre-commit hook installed and executable"
else
    log_error "❌ pre-commit hook installation failed"
    exit 1
fi

if [ -x ".git/hooks/commit-msg" ]; then
    log_success "✅ commit-msg hook installed and executable"
else
    log_error "❌ commit-msg hook installation failed"
    exit 1
fi

if [ -x ".git/hooks/pre-push" ]; then
    log_success "✅ pre-push hook installed and executable"
else
    log_error "❌ pre-push hook installation failed"
    exit 1
fi

echo ""
log_success "🎉 Git hooks installed successfully!"
echo ""
log_info "📋 Installed hooks:"
log_info "  🪝 pre-commit: Checks code formatting before commits"
log_info "  🪝 commit-msg: Validates commit message format (Conventional Commits)"
log_info "  🪝 pre-push: Runs compilation checks before pushing"
echo ""
log_info "💡 Usage:"
log_info "  • Hooks run automatically during git operations"
log_info "  • Format code manually: ./format.sh"
log_info "  • Check formatting: ./format.sh --check"
log_info "  • Bypass hooks (not recommended): git commit --no-verify"
echo ""
log_info "📝 Commit message format:"
log_info "  <type>[optional scope]: <description>"
log_info "  Example: feat(auth): add user login functionality"
echo ""