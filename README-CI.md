# CI/CD Pipeline Setup

This document describes the continuous integration and deployment setup for the Trouvaille project.

## Overview

The project includes two build scripts and comprehensive GitHub Actions workflows for automated testing, building, and deployment.

## Build Scripts

### 1. `ci-build.sh` - Continuous Integration Build

A fast build script focused on compilation, testing, and code quality checks.

**Features:**
- ✅ Prerequisites validation (Java 21+, Node.js 18+)
- 🔄 OpenAPI code generation
- ☕ Backend compilation and testing
- 🎨 Frontend building and linting
- 📋 Code formatting validation
- ⚡ Optimized for CI/CD environments

**Usage:**
```bash
./ci-build.sh
```

**Duration:** ~2-3 minutes (without Docker overhead)

### 2. `build.sh` - Docker Image Builder

Comprehensive build script for creating production Docker images.

**Features:**
- 🐳 Docker image creation
- 🏗️ Native or JVM builds
- 📦 Multi-stage builds
- 💾 Build caching
- 📊 Size reporting

**Usage:**
```bash
./build.sh [tag] [jvm|native]
# Examples:
./build.sh latest jvm
./build.sh v1.0.0 native
```

## GitHub Actions Workflows

### 1. Continuous Integration (`.github/workflows/ci.yml`)

**Triggers:**
- Push to `main` or `develop`
- Pull requests to `main` or `develop`

**Matrix Strategy:**
- Java: 21
- Node.js: 20

**Steps:**
1. Checkout code
2. Setup Java and Node.js
3. Cache dependencies (Maven & npm)
4. Run `ci-build.sh`
5. Upload artifacts (JARs, test results, frontend dist)

**Artifacts:**
- Backend test results (`target/surefire-reports/`)
- Frontend build (`dist/`)
- Backend JAR files (`target/*.jar`)

### 2. Release (`.github/workflows/release.yml`)

**Triggers:**
- Version tags (`v*`)
- Manual workflow dispatch

**Steps:**
1. Full CI build
2. Docker image creation
3. Push to GitHub Container Registry
4. Create release archives
5. Generate GitHub release

**Docker Images:**
- `ghcr.io/[owner]/trouvaille-backend:tag`
- `ghcr.io/[owner]/trouvaille-frontend:tag`

**Release Artifacts:**
- `trouvaille-backend-[version].tar.gz`
- `trouvaille-frontend-[version].tar.gz`
- `trouvaille-source-[version].tar.gz`

### 3. Code Quality (`.github/workflows/code-quality.yml`)

**Triggers:**
- Push to `main` or `develop`
- Pull requests to `main` or `develop`
- Weekly schedule (Sundays 02:00 UTC)

**Security Features:**
- Trivy vulnerability scanning
- npm audit for frontend
- Dependency review for PRs
- CodeQL analysis

## Local Development

### Quick Start

```bash
# Install dependencies
cd trouvaille-front && npm install
cd ../trouvaille-back

# Run CI build
./ci-build.sh

# Build Docker images (optional)
./build.sh test jvm
```

### Testing Individual Components

```bash
# Backend only
cd trouvaille-back
./mvnw clean test
./mvnw spotless:check

# Frontend only
cd trouvaille-front
npm ci
npm run type-check
npm run lint
npm run build
```

## Deployment

### Development/Staging

```bash
# Build and start with Docker Compose
./build.sh latest jvm
docker compose up -d
```

### Production

1. **Create a release:**
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

2. **Deploy from registry:**
   ```bash
   # Pull images
   docker pull ghcr.io/[owner]/trouvaille-backend:v1.0.0
   docker pull ghcr.io/[owner]/trouvaille-frontend:v1.0.0
   
   # Update docker-compose.prod.yml and deploy
   docker compose -f docker-compose.prod.yml up -d
   ```

## Monitoring and Maintenance

### Build Status

Monitor builds at:
- GitHub Actions tab
- Pull request checks
- Release pages

### Performance Metrics

**CI Build Times:**
- Backend compilation: ~30-60s
- Frontend build: ~20-40s
- Tests: ~10-30s
- Total: ~2-3 minutes

**Docker Build Times:**
- JVM build: ~3-5 minutes
- Native build: ~10-15 minutes

### Cache Optimization

The workflows use aggressive caching:
- Maven dependencies (`~/.m2`)
- Node.js modules (`node_modules`)
- Docker layer caching
- OpenAPI generator cache

### Security

**Automated Scanning:**
- Trivy for container vulnerabilities
- npm audit for JavaScript dependencies
- CodeQL for code analysis
- Dependency review for PRs

**Dependency Management:**
- Dependabot for automated dependency updates
- Weekly schedules: Maven (Mon), npm (Mon), Docker (Tue), Actions (Wed)
- Grouped updates by ecosystem for better compatibility
- Security-prioritized updates with immediate notifications

**Manual Reviews:**
- Security scan results in GitHub Security tab
- Dependabot PR reviews and approvals
- Weekly security reports and vulnerability assessments

## Troubleshooting

### Common Issues

1. **Build timeouts:**
   - Check memory usage (native builds need 4GB+)
   - Verify cache efficiency
   - Consider reducing test scope

2. **Dependency conflicts:**
   - Clear caches (`rm -rf ~/.m2`, `rm -rf node_modules`)
   - Update lock files
   - Check for version conflicts

3. **Docker registry issues:**
   - Verify GitHub token permissions
   - Check image size limits
   - Ensure proper tagging

### Debug Commands

```bash
# Local CI simulation
./ci-build.sh

# Verbose Maven build
cd trouvaille-back && ./mvnw clean compile -X

# Frontend debugging
cd trouvaille-front && npm run build -- --mode development

# Docker build debugging
./build.sh debug jvm
```

## Configuration

### Environment Variables

**GitHub Actions:**
- `GITHUB_TOKEN` - Automatically provided
- Custom secrets can be added in repository settings

**Local Development:**
- See `.env.example` files in each module
- Use docker-compose overrides for local config

### Customization

**Build Timeouts:**
- Adjust in workflow files (`timeout-minutes`)
- Configure in build scripts

**Cache Policies:**
- Modify cache keys in workflows
- Adjust retention periods

**Security Scans:**
- Configure sensitivity levels
- Add custom rules
- Set up notifications

## Best Practices

1. **Branch Protection:**
   - Require CI checks to pass
   - Require code reviews
   - Enforce linear history

2. **Release Process:**
   - Use semantic versioning
   - Tag releases consistently
   - Maintain changelog

3. **Performance:**
   - Monitor build times
   - Optimize dependencies
   - Use parallel jobs when possible

4. **Security:**
   - Regular dependency updates
   - Security scan reviews
   - Access control management

---

For questions or issues, see the troubleshooting section or check the GitHub Actions logs.