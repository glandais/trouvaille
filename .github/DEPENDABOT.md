# Dependabot Configuration

This document explains the automated dependency management setup using GitHub Dependabot.

## Overview

Dependabot automatically monitors and updates dependencies across multiple package ecosystems:

- 📦 **Maven** (Backend Java dependencies)
- 📦 **npm** (Frontend JavaScript dependencies)
- 🐳 **Docker** (Container base images)
- ⚙️ **GitHub Actions** (Workflow dependencies)

## Schedule

| Ecosystem | Day | Time (Europe/Paris) | Max PRs |
|-----------|-----|---------------------|---------|
| Maven | Monday | 09:00 | 5 |
| npm | Monday | 09:30 | 10 |
| Docker (Root) | Tuesday | 09:00 | 3 |
| Docker (Backend) | Tuesday | 09:15 | 3 |
| Docker (Frontend) | Tuesday | 09:30 | 3 |
| GitHub Actions | Wednesday | 09:00 | 5 |

## Dependency Groups

### Maven Dependencies (Backend)

**Quarkus Group:**
- All `io.quarkus*` and `io.quarkus.platform*` packages
- Minor and patch updates only
- Ensures Quarkus components stay in sync

**MapStruct Group:**
- All `org.mapstruct*` packages
- Minor and patch updates only

**Lombok Group:**
- All `org.projectlombok*` packages
- Minor and patch updates only

**Testing Group:**
- JUnit, REST Assured, Testcontainers
- Minor and patch updates only

### npm Dependencies (Frontend)

**Vue.js Group:**
- `vue*` and `@vue/*` packages
- Minor and patch updates only
- Keeps Vue ecosystem in sync

**Vite Group:**
- `vite*` and `@vitejs/*` packages
- Minor and patch updates only

**TypeScript Group:**
- `typescript*` and `@types/*` packages
- Minor and patch updates only

**ESLint Group:**
- All ESLint-related packages
- Minor and patch updates only

**Tailwind CSS Group:**
- `tailwindcss*` and `@tailwindcss/*` packages
- Minor and patch updates only

**Build Tools Group:**
- Prettier, PostCSS, Autoprefixer, OpenAPI Generator
- Minor and patch updates only

### GitHub Actions Groups

**Setup Actions:**
- `actions/checkout`, `actions/setup-*`, `actions/cache`, etc.
- Minor and patch updates only

**Docker Actions:**
- Docker-related actions
- Minor and patch updates only

**Security Actions:**
- CodeQL, Trivy, dependency review actions
- Minor and patch updates only

## Pull Request Management

### Automatic Labels

All Dependabot PRs are automatically labeled:
- `dependencies` (all PRs)
- Package ecosystem (`maven`, `npm`, `docker`, `github-actions`)
- Component (`backend`, `frontend`, `infrastructure`, `ci-cd`)

### Assignment and Review

- **Assignee:** `glandais`
- **Reviewer:** `glandais`
- **Auto-merge:** Disabled (manual review required)

### Commit Messages

Conventional commit format with scope:
- `deps(maven): bump quarkus from 3.24.0 to 3.24.1`
- `deps(npm): bump vue from 3.4.0 to 3.4.1`
- `deps(docker): bump node from 20-alpine to 21-alpine`
- `deps(actions): bump actions/checkout from v3 to v4`

## Monitoring and Review Process

### Daily Checks

1. **Review new PRs** - Check for breaking changes
2. **Run CI/CD** - Ensure all tests pass
3. **Security scan** - Review vulnerability reports
4. **Manual testing** - Test critical updates locally

### Weekly Tasks

1. **Merge approved PRs** - Group related updates
2. **Review grouped updates** - Check compatibility
3. **Update documentation** - Note significant changes
4. **Release planning** - Schedule dependency updates

### Monthly Review

1. **Audit ignored updates** - Review major version updates
2. **Security assessment** - Check for known vulnerabilities
3. **Performance impact** - Measure build time changes
4. **Configuration tuning** - Adjust groups and schedules

## Managing Updates

### Approving Updates

```bash
# Review the PR locally
git fetch origin
git checkout dependabot/maven/io.quarkus.platform-quarkus-bom-3.24.1

# Run full build
./build.sh latest jvm

# Test specific functionality
cd trouvaille-back && ./mvnw test
cd trouvaille-front && npm test

# Merge if tests pass
gh pr merge --squash
```

### Handling Conflicts

1. **Rebase conflicts:**
   ```bash
   # Let Dependabot recreate the PR
   gh pr comment --body "@dependabot rebase"
   ```

2. **Manual resolution:**
   ```bash
   # Checkout and resolve manually
   git checkout dependabot/branch-name
   # Fix conflicts, commit, push
   ```

### Ignoring Updates

Add to `.github/dependabot.yml`:

```yaml
- package-ecosystem: "maven"
  directory: "/trouvaille-back"
  ignore:
    # Ignore major version updates for specific packages
    - dependency-name: "io.quarkus.platform:quarkus-bom"
      update-types: ["version-update:semver-major"]
    # Ignore specific versions
    - dependency-name: "org.mapstruct:mapstruct"
      versions: ["1.6.0"]
```

## Security Considerations

### Vulnerability Scanning

- All updates trigger security scans
- Critical vulnerabilities get priority
- Security updates bypass version constraints

### Review Requirements

- **Critical/High:** Immediate review and merge
- **Medium:** Review within 48 hours
- **Low:** Review during weekly cycle
- **Major versions:** Manual review required

### Branch Protection

- Dependabot PRs require CI checks to pass
- Security scans must complete successfully
- Manual approval required for sensitive changes

## Troubleshooting

### Common Issues

1. **Build failures after updates:**
   ```bash
   # Check for breaking changes in changelogs
   # Run specific tests for affected components
   # Consider reverting if critical
   ```

2. **Conflicting dependencies:**
   ```bash
   # Check dependency trees
   mvn dependency:tree
   npm ls --depth=0
   
   # Resolve version conflicts manually
   ```

3. **Performance degradation:**
   ```bash
   # Run performance benchmarks
   # Compare build times before/after
   # Monitor runtime performance
   ```

### Debug Commands

```bash
# Check Dependabot status
gh api repos/:owner/:repo/dependabot/alerts

# View dependency graph
gh api repos/:owner/:repo/dependency-graph/dependencies

# Check security vulnerabilities
gh api repos/:owner/:repo/vulnerability-alerts
```

## Configuration Tuning

### Frequency Adjustment

For active development periods:
```yaml
schedule:
  interval: "daily"  # More frequent updates
```

For stable periods:
```yaml
schedule:
  interval: "monthly"  # Less frequent updates
```

### PR Limits

Adjust based on team capacity:
```yaml
open-pull-requests-limit: 3  # Conservative
open-pull-requests-limit: 10 # Aggressive
```

### Group Customization

Add new groups as dependencies grow:
```yaml
groups:
  spring-boot:
    patterns:
      - "org.springframework.boot*"
      - "org.springframework*"
```

## Metrics and Reporting

### Weekly Reports

- Number of PRs created/merged
- Security vulnerabilities addressed
- Build time impact
- Failed updates requiring attention

### Monthly Dashboard

- Dependency freshness score
- Security posture improvement
- Update adoption rate
- Manual intervention frequency

---

For questions about Dependabot configuration, see the [GitHub Dependabot documentation](https://docs.github.com/en/code-security/dependabot).