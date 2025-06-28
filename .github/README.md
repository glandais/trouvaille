# GitHub Actions CI/CD Pipeline

This directory contains the GitHub Actions workflows for the Trouvaille project.

## Workflows

### 1. Continuous Integration (`ci.yml`)

**Triggers:**
- Push to `main` or `develop` branches
- Pull requests to `main` or `develop` branches

**What it does:**
- Sets up Java 21 and Node.js 20
- Caches Maven and npm dependencies
- Runs the `ci-build.sh` script which:
  - Generates OpenAPI code
  - Compiles and tests the backend
  - Checks code formatting (Spotless)
  - Builds and lints the frontend
  - Runs TypeScript type checking
- Uploads build artifacts and test results

### 2. Release (`release.yml`)

**Triggers:**
- Push of version tags (e.g., `v1.0.0`)
- Manual workflow dispatch

**What it does:**
- Runs the full CI build
- Builds Docker images for both frontend and backend
- Pushes images to GitHub Container Registry (ghcr.io)
- Creates release archives (source, backend JAR, frontend dist)
- Creates a GitHub release with artifacts

### 3. Code Quality (`code-quality.yml`)

**Triggers:**
- Push to `main` or `develop` branches
- Pull requests to `main` or `develop` branches
- Weekly scheduled run (Sundays at 02:00 UTC)

**What it does:**
- Security scanning with Trivy
- Frontend vulnerability checking with npm audit
- Dependency review for pull requests
- CodeQL code analysis

## Dependency Management

### Dependabot (`dependabot.yml`)

**Automated Updates:**
- Maven dependencies (Backend) - Weekly on Mondays
- npm dependencies (Frontend) - Weekly on Mondays
- Docker base images - Weekly on Tuesdays
- GitHub Actions - Weekly on Wednesdays

**Features:**
- Grouped updates by ecosystem (Quarkus, Vue.js, etc.)
- Automatic labeling and assignment
- Conventional commit messages
- Security-first prioritization

See [DEPENDABOT.md](DEPENDABOT.md) for detailed configuration.

## Build Scripts

### `ci-build.sh`

The main build script that:
1. Checks prerequisites (Java 21+, Node.js 18+)
2. Generates OpenAPI code
3. Builds and tests the backend
4. Builds and lints the frontend
5. Validates code formatting

### `build.sh`

Docker image build script that:
1. Builds backend with Maven (JVM or native)
2. Builds frontend with npm
3. Creates Docker images
4. Provides deployment information

## Usage

### Running Locally

```bash
# Run the CI build (compile, test, lint)
./ci-build.sh

# Build Docker images
./build.sh latest jvm
```

### Creating a Release

1. **Tag-based release:**
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

2. **Manual release:**
   - Go to Actions tab in GitHub
   - Select "Release" workflow
   - Click "Run workflow"
   - Enter the release tag

### Configuration

#### Required Secrets

- `GITHUB_TOKEN` - Automatically provided by GitHub

#### Optional Configuration

- Modify caching strategies in workflows
- Adjust security scan sensitivity
- Customize artifact retention periods
- Add environment-specific deployments

## Monitoring

### Build Status

Check build status on:
- GitHub Actions tab
- README badges (if added)
- Pull request checks

### Artifacts

- **CI builds:** Test results, build logs
- **Releases:** JAR files, frontend dist, Docker images
- **Security:** SARIF reports, vulnerability reports

## Troubleshooting

### Common Issues

1. **Build failures:**
   - Check Java/Node.js versions
   - Verify dependencies in `pom.xml` and `package.json`
   - Review build logs in Actions tab

2. **Security scan failures:**
   - Review vulnerability reports
   - Update dependencies
   - Add security exceptions if needed

3. **Release failures:**
   - Ensure proper tag format (`v1.0.0`)
   - Check Docker registry permissions
   - Verify release notes generation

### Local Testing

Before pushing, test locally:

```bash
# Test the CI build
./ci-build.sh

# Test Docker build
./build.sh test jvm

# Run specific checks
cd trouvaille-back && ./mvnw spotless:check
cd trouvaille-front && npm run lint
```