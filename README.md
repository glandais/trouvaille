# 🏺 Trouvaille - Private Marketplace

[![CI](https://github.com/glandais/trouvaille/actions/workflows/ci.yml/badge.svg)](https://github.com/glandais/trouvaille/actions/workflows/ci.yml)
[![Code Quality](https://github.com/glandais/trouvaille/actions/workflows/code-quality.yml/badge.svg)](https://github.com/glandais/trouvaille/actions/workflows/code-quality.yml)
[![Latest Release](https://img.shields.io/github/v/release/glandais/trouvaille)](https://github.com/glandais/trouvaille/releases)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Quarkus](https://img.shields.io/badge/Quarkus-3.24.3-blue.svg)](https://quarkus.io/)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.5-green.svg)](https://vuejs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.8-blue.svg)](https://www.typescriptlang.org/)
[![MongoDB](https://img.shields.io/badge/MongoDB-8.0-green.svg)](https://www.mongodb.com/)

A modern, private marketplace platform for classified ads allowing users to sell, rent, and exchange goods and services within a closed community.

## 📋 Table of Contents

- [🏺 Trouvaille - Private Marketplace](#-trouvaille---private-marketplace)
  - [📋 Table of Contents](#-table-of-contents)
  - [✨ Features](#-features)
  - [🏗️ Architecture](#️-architecture)
    - [Tech Stack](#tech-stack)
    - [Project Structure](#project-structure)
  - [🚀 Quick Start](#-quick-start)
    - [Prerequisites](#prerequisites)
    - [Environment Setup](#environment-setup)
    - [Development Mode](#development-mode)
    - [Production Deployment](#production-deployment)
  - [🛠️ Development](#️-development)
    - [Backend Development (Quarkus)](#backend-development-quarkus)
    - [Frontend Development (Vue 3)](#frontend-development-vue-3)
    - [API Contract Development](#api-contract-development)
  - [📡 API Documentation](#-api-documentation)
  - [🔐 Authentication](#-authentication)
  - [🗄️ Database](#️-database)
  - [📸 Photo Management](#-photo-management)
  - [🌍 Geolocation](#-geolocation)
  - [🐳 Docker](#-docker)
  - [🧪 Testing](#-testing)
  - [📝 Scripts](#-scripts)
  - [🚀 CI/CD](#-cicd)
  - [🤝 Contributing](#-contributing)
  - [📄 License](#-license)

## ✨ Features

### 🏪 Marketplace Core
- **Sell & Rent**: Create ads for selling or renting items
- **Offer & Demand**: Post offers or search for specific needs
- **Categories**: Organized listing system with flexible categorization
- **Photo Gallery**: Up to 10 photos per listing with automatic thumbnail generation
- **Status Management**: Active, suspended, and sold status tracking

### 🔍 Advanced Search & Filtering
- **Text Search**: Full-text search across titles and descriptions
- **Smart Filters**: Filter by type, nature, price range, and location
- **Geolocation**: Distance-based search up to 100km radius
- **Sorting Options**: Sort by date, price, title, or proximity
- **Real-time Results**: Debounced search with instant feedback

### 🗺️ Geospatial Features
- **Location Services**: Address autocomplete and GPS coordinates
- **Distance Calculation**: Haversine formula for accurate distance measurement
- **Map Integration**: Visual location display and selection
- **Proximity Search**: Find nearby listings automatically

### 🔐 Security & Authentication
- **OAuth2 Integration**: Secure authentication with external provider
- **JWT Tokens**: Stateless authentication with automatic expiration
- **Ownership Validation**: Users can only modify their own listings
- **CORS Protection**: Configured for secure cross-origin requests

### 📱 User Experience
- **Responsive Design**: Mobile-first approach with Tailwind CSS
- **Progressive Loading**: Pagination and lazy loading for performance
- **Real-time Updates**: Instant feedback on user actions
- **Accessibility**: WCAG compliant interface design

## 🏗️ Architecture

### Tech Stack

**Backend:**
- **Quarkus 3.24.3** - Supersonic Subatomic Java Framework
- **Java 21** - Latest LTS with modern language features
- **MongoDB 8.0** - Document database with geospatial capabilities
- **Panache** - Simplified data access layer
- **JWT** - JSON Web Tokens for authentication
- **MapStruct** - Type-safe bean mapping
- **OpenAPI 3.0.3** - API-first development

**Frontend:**
- **Vue 3** - Progressive JavaScript framework with Composition API
- **TypeScript 5.8** - Type-safe JavaScript development
- **Vite** - Lightning-fast build tool
- **Pinia** - Modern state management
- **Tailwind CSS** - Utility-first CSS framework
- **Axios** - HTTP client with interceptors
- **vue-i18n** - Internationalization

**Infrastructure:**
- **Docker** - Containerized deployment
- **Traefik** - Modern reverse proxy and load balancer
- **MongoDB** - Document database with replica sets
- **Nginx** - High-performance web server

### Project Structure

```
trouvaille/
├── contract.yaml                 # OpenAPI 3.0.3 specification
├── docker-compose.yml           # Development environment
├── data/                        # Persistent data
│   ├── keys/                   # JWT signing keys
│   ├── photos/                 # Uploaded images
│   └── mongodb/                # Database files
├── trouvaille-back/             # Backend (Quarkus)
│   ├── src/main/java/          # Java source code
│   │   └── io/github/glandais/trouvaille/
│   │       ├── entity/         # MongoDB entities
│   │       ├── repository/     # Data access layer
│   │       ├── service/        # Business logic
│   │       ├── resource/       # REST endpoints
│   │       └── config/         # Configuration classes
│   ├── src/main/resources/     # Configuration files
│   └── pom.xml                 # Maven dependencies
└── trouvaille-front/            # Frontend (Vue 3)
    ├── src/                    # TypeScript source code
    │   ├── components/         # Vue components
    │   ├── views/              # Page components
    │   ├── stores/             # Pinia stores
    │   ├── services/           # API services
    │   ├── api/                # Generated API client
    │   └── types/              # TypeScript definitions
    ├── public/                 # Static assets
    └── package.json            # Node.js dependencies
```

## 🚀 Quick Start

### Prerequisites

- **Docker & Docker Compose** (recommended)
- **Java 21+** (for backend development)
- **Node.js 18+** (for frontend development)
- **Maven 3.9+** (for backend builds)

### Environment Setup

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd trouvaille
   ```

2. **Create environment file:**
   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```

3. **Generate JWT keys:**
   ```bash
   ./generate-keys.sh
   ```

### Development Mode

**Option 1: Docker Compose (Recommended)**
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Access the application
open http://localhost:8090
```

**Option 2: Local Development**
```bash
# Terminal 1: Start MongoDB
docker-compose up -d mongodb

# Terminal 2: Start backend
cd trouvaille-back
mvn quarkus:dev

# Terminal 3: Start frontend
cd trouvaille-front
npm install
npm run dev
```

### Production Deployment

```bash
# Build and deploy
./build.sh
./deploy.sh

# Or manually
docker-compose up -d
```

## 🛠️ Development

### Backend Development (Quarkus)

**Start development server:**
```bash
cd trouvaille-back
mvn quarkus:dev
```

**Key Commands:**
```bash
# Clean build
mvn clean package

# Run tests
mvn test

# Run integration tests
mvn verify

# Generate code from OpenAPI
mvn quarkus:generate-code

# Code formatting
mvn spotless:apply
```

**Hot Reload:**
- Java code changes are automatically reloaded
- Configuration changes require restart
- Access Dev UI at `http://localhost:8080/q/dev/`

### Frontend Development (Vue 3)

**Start development server:**
```bash
cd trouvaille-front
npm run dev
```

**Key Commands:**
```bash
# Install dependencies
npm install

# Type checking
npm run type-check

# Linting and formatting
npm run lint
npm run format

# Build for production
npm run build

# Preview production build
npm run preview

# Generate API client
npm run generate:api
```

**Development Features:**
- Hot Module Replacement (HMR)
- TypeScript support with Vue 3 Composition API
- Tailwind CSS with JIT compilation
- ESLint and Prettier integration

### API Contract Development

The project follows **Contract-First Development**:

1. **Edit the contract:**
   ```bash
   # Edit the OpenAPI specification
   vim contract.yaml
   ```

2. **Generate backend interfaces:**
   ```bash
   cd trouvaille-back
   mvn quarkus:generate-code
   ```

3. **Generate frontend client:**
   ```bash
   cd trouvaille-front
   npm run generate:api
   ```

4. **Implement the interfaces:**
   - Backend: Implement generated interfaces in `resource/` package
   - Frontend: Use generated API client in services

## 📡 API Documentation

The API is documented using OpenAPI 3.0.3 specification in `contract.yaml`.

**Key Endpoints:**
- `GET /api/v1/annonces` - List ads with filtering and pagination
- `POST /api/v1/annonces` - Create new ad
- `GET /api/v1/annonces/{id}` - Get ad details
- `PUT /api/v1/annonces/{id}` - Update ad
- `DELETE /api/v1/annonces/{id}` - Delete ad
- `POST /api/v1/annonces/photos` - Upload photo
- `GET /api/v1/photos/{id}/full` - Get full-size photo
- `GET /api/v1/photos/{id}/thumb` - Get thumbnail

**Interactive Documentation:**
- Swagger UI: `http://localhost:8080/q/swagger-ui/`
- ReDoc: `http://localhost:8080/q/redoc/`

## 🔐 Authentication

**OAuth2 Flow:**
1. User clicks "Login" → Redirects to OAuth provider
2. User authenticates → Receives authorization code
3. Frontend exchanges code for JWT token via the backend
4. The backend returns a JWT token in an `auth_token` cookie
5. The frontend uses the cookie for subsequent API requests

**Configuration:**
```properties
# Backend (application.properties)
trouvaille.oauth.client-id=${OAUTH_CLIENT_ID}
trouvaille.oauth.client-secret=${OAUTH_CLIENT_SECRET}
quarkus.rest-client.oauth2.url=${OAUTH_BASE_URL}
```

## 🗄️ Database

**MongoDB Configuration:**
- Database: `trouvaille`
- Collections: `Annonce`, `User`, `Photo`
- **Automatic Schema Management**: Liquibase MongoDB automatically creates collections and indexes at startup
- Geospatial indexing for location-based queries
- Full-text search indexing with French language support

**Development Access:**
```bash
# Connect to MongoDB
docker-compose exec mongodb mongosh trouvaille

# View collections
db.getCollectionNames()

# Query ads
db.Annonce.find().limit(5)

# View existing indexes
db.Annonce.getIndexes()
```

**Automated Database Setup:**
The application automatically creates and manages the database schema using **Liquibase MongoDB**:

- **Collections**: `Annonce`, `User`, `Photo` are created automatically
- **Indexes**: Comprehensive indexing strategy applied at startup:
  - **Performance indexes**: `utilisateur`, `statut`, `type`, `nature`, `prix`
  - **Date indexes**: `dateCreation`, `dateModification` (descending)
  - **Geospatial index**: `coordinates` with 2dsphere for location queries
  - **Text search index**: `titre` and `description` with French language support and custom weights
  - **Unique constraints**: `externalId` for users

**Schema Migration:**
- Configuration: `quarkus.liquibase-mongodb.migrate-at-start=true`
- Migration file: `/src/main/resources/db/changeLog.xml`
- **No manual index creation needed** - everything is automated

**Index Details:**
```javascript
// Automatically created indexes include:

// Performance optimization
{ "utilisateur": 1, "statut": 1, "dateCreation": -1 }

// Geospatial queries  
{ "coordinates": "2dsphere" }

// Full-text search with French language
{ 
  "titre": "text", 
  "description": "text",
  weights: { titre: 10, description: 5 },
  default_language: "french"
}
```

## 📸 Photo Management

**Features:**
- Upload up to 10 photos per ad
- Automatic thumbnail generation (150x150px)
- Supported formats: JPEG, PNG, WebP
- Local filesystem storage with organized directory structure

**Storage Structure:**
```
data/photos/
└── 6/8/6/0/
    └── 68602c92c821e3906e47e06b/
        ├── full.jpg      # Original image
        └── thumb.jpg     # 150x150 thumbnail
```

**API Endpoints:**
- `POST /api/v1/annonces/photos` - Upload photo
- `GET /api/v1/photos/{id}/full` - Get full-size image
- `GET /api/v1/photos/{id}/thumb` - Get thumbnail
- `DELETE /api/v1/annonces/photos/{id}` - Delete photo

## 🌍 Geolocation

**Features:**
- Address autocomplete with coordinate resolution
- Distance-based filtering (1-100km radius)
- MongoDB geospatial queries with `$geoNear`
- Haversine distance calculations

**Usage:**
```javascript
// Find ads within 10km of coordinates
db.Annonce.aggregate([
  {
    $geoNear: {
      near: { type: "Point", coordinates: [2.3522, 48.8566] },
      distanceField: "distance",
      maxDistance: 10000, // 10km in meters
      spherical: true
    }
  }
])
```

## 🐳 Docker

**Development:**
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f [service-name]

# Stop all services
docker-compose down

# Rebuild services
docker-compose build --no-cache
```

**Production:**
```bash
# Deploy to production
docker-compose up -d

# Update images
docker-compose pull
docker-compose up -d
```

**Services:**
- **traefik**: Reverse proxy and load balancer (port 8090)
- **mongodb**: Database server (port 27017, localhost only)
- **backend**: Quarkus application (internal)
- **frontend**: Vue.js application (internal)

## 🧪 Testing

**Backend Testing:**
```bash
cd trouvaille-back

# Unit tests
mvn test

# Integration tests
mvn verify

# Test with coverage
mvn test jacoco:report
```

**Frontend Testing:**
```bash
cd trouvaille-front

# Type checking
npm run type-check

# Linting
npm run lint:check

# Format checking
npm run format:check
```

**Manual Testing:**
- Create test data using the API
- Test OAuth flow with valid credentials
- Verify geolocation features
- Test photo upload and display

## 📝 Scripts

**Utility Scripts:**
- `build.sh` - Build Docker images for production
- `deploy.sh` - Deploy to production environment
- `format.sh` - Format all code (backend + frontend)
- `generate-keys.sh` - Generate JWT signing keys
- `generate-openapi.sh` - Generate API code from contract
- `install-git-hooks.sh` - Install pre-commit hooks

**Usage:**
```bash
# Make scripts executable
chmod +x *.sh

# Format all code
./format.sh

# Build production images
./build.sh

# Deploy to production
./deploy.sh
```

## 🚀 CI/CD

This project uses **GitHub Actions** for CI/CD.

### Workflows

- **`ci.yml`**: Runs on every push and pull request to `main` and `develop`.
  - Builds and tests both backend and frontend.
  - Performs code quality checks.
- **`release.yml`**: Runs on new version tags (e.g., `v1.2.3`).
  - Creates a GitHub release.
  - Builds and pushes Docker images to `ghcr.io`.
- **`code-quality.yml`**: Runs weekly and on pull requests.
  - Performs security scans (Trivy, CodeQL, npm audit).

### Automated Dependency Management

**Dependabot** is configured to automatically create pull requests for dependency updates for:
- `maven` (backend)
- `npm` (frontend)
- `docker`
- `github-actions`

## 🤝 Contributing

1. **Fork the repository**
2. **Create a feature branch:**
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. **Make your changes:**
   - Follow the coding standards
   - Add tests for new features
   - Update documentation
4. **Format your code:**
   ```bash
   ./format.sh
   ```
5. **Commit your changes:**
   ```bash
   git commit -m "Add amazing feature"
   ```
6. **Push to the branch:**
   ```bash
   git push origin feature/amazing-feature
   ```
7. **Open a Pull Request**

**Coding Standards:**
- Backend: Google Java Style (enforced by Spotless)
- Frontend: ESLint + Prettier configuration
- Commit messages: Conventional Commits format
- Documentation: Update README and API docs

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**Built with ❤️ using modern technologies for a seamless marketplace experience.**