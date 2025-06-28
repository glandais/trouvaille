# Security Policy

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| main    | :white_check_mark: |
| develop | :white_check_mark: |

## Reporting a Vulnerability

If you discover a security vulnerability, please follow these steps:

1. **Do not open a public issue** - Security vulnerabilities should be reported privately
2. **Email the maintainer** at the email address listed in the repository
3. **Include details** - Provide as much information as possible about the vulnerability
4. **Wait for confirmation** - We will acknowledge receipt within 48 hours

## Security Measures

### Automated Security Scanning

- **Dependabot** - Automatic dependency updates with security prioritization
- **CodeQL** - Static code analysis for security vulnerabilities
- **Trivy** - Container image vulnerability scanning
- **npm audit** - Frontend dependency vulnerability checking

### Dependency Management

- Weekly automated dependency updates
- Security updates are prioritized and can be merged immediately
- All dependencies are scanned for known vulnerabilities
- Dependency review is required for all pull requests

### CI/CD Security

- All builds run in isolated environments
- No secrets are exposed in build logs
- Container images are scanned before deployment
- Release artifacts are signed and verified

### Code Review

- All changes require code review
- Security-related changes require additional review
- Automated security checks must pass before merge

## Security Best Practices

### For Contributors

1. **Keep dependencies up to date** - Accept Dependabot PRs promptly
2. **Review security scan results** - Check for vulnerabilities in CI
3. **Use secure coding practices** - Follow OWASP guidelines
4. **Validate user input** - Sanitize all external inputs
5. **Handle secrets properly** - Never commit secrets to the repository

### For Deployment

1. **Use official base images** - Prefer official Docker images
2. **Keep systems updated** - Apply security patches regularly
3. **Monitor for vulnerabilities** - Set up security alerts
4. **Use encrypted connections** - HTTPS/TLS for all communications
5. **Implement access controls** - Principle of least privilege

## Vulnerability Response

### Severity Levels

- **Critical** - Immediate response required (within 24 hours)
- **High** - Response within 48 hours
- **Medium** - Response within 1 week
- **Low** - Response within 1 month

### Response Process

1. **Assessment** - Evaluate the impact and severity
2. **Fix development** - Create and test a security fix
3. **Testing** - Verify the fix resolves the vulnerability
4. **Release** - Deploy the fix as quickly as possible
5. **Communication** - Notify users of the security update

## Security Tools

### GitHub Security Features

- **Dependency graph** - Track project dependencies
- **Dependabot alerts** - Get notified of vulnerable dependencies
- **Security advisories** - Publish security information
- **Code scanning** - Automated vulnerability detection

### Third-party Tools

- **Trivy** - Container and filesystem vulnerability scanner
- **CodeQL** - Semantic code analysis
- **OWASP ZAP** - Web application security testing (if applicable)

## Contact

For security-related questions or concerns, please contact:

- **Maintainer**: Guillaume Landais
- **Response time**: Within 48 hours
- **PGP Key**: Available upon request

## Updates

This security policy is reviewed and updated quarterly to ensure it remains current with best practices and project needs.