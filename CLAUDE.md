# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# Marketplace - Plateforme de Petites Annonces Privée

## Objectif du Projet

Développement d'une plateforme web privée de petites annonces permettant aux utilisateurs de :
- **Vendre** ou **louer** des objets
- **Proposer** des biens/services ou **rechercher** des besoins spécifiques
- Gérer leurs annonces avec photos et géolocalisation
- Interagir dans un environnement communautaire fermé

## Architecture Technique

### Stack Technologique
- **Frontend** : Vue 3 + Vite + TypeScript
- **State Management** : Pinia
- **Styling** : Tailwind CSS
- **Backend** : Quarkus (Java 21)
- **Base de données** : MongoDB
- **Authentification** : JWT + OAuth2

### Code Architecture

#### Backend (trouvaille-back/)
- **Framework**: Quarkus with Panache MongoDB
- **API**: REST with Jackson serialization
- **Database**: MongoDB with Panache entities
- **Mapping**: MapStruct for entity-to-DTO conversion
- **Code Generation**: OpenAPI server interfaces from contract.yaml
- **Key Packages**:
  - `entity/`: MongoDB entities (AnnonceEntity, UserEntity, PhotoEntity, etc.)
  - `repository/`: Panache repositories for data access
  - `service/`: Business logic and mapping services
  - `resource/`: REST endpoint implementations (ApiResourceImpl)

#### Frontend (trouvaille-front/)
- **Framework**: Vue 3 with Composition API
- **Build**: Vite with TypeScript
- **Routing**: Vue Router 4
- **State**: Pinia stores
- **Code Generation**: TypeScript client from contract.yaml (to be implemented)

### Authentification et Sécurité

OAuth endpoints :
- Authorize URI https://chat.n-peloton.fr/oauth/authorize
- Token URI https://chat.n-peloton.fr/oauth/access_token
- User info URI https://chat.n-peloton.fr/api/v4/users/me

#### Configuration OIDC Backend
- **Extension**: `quarkus-oidc` pour la vérification des tokens JWT
- **Configuration**: `application.properties` avec auth-server-url pointant sur https://chat.n-peloton.fr
- **Sécurité des endpoints**:
  - `@PermitAll`: Endpoints publics (liste et consultation des annonces)
  - `@Authenticated`: Endpoints nécessitant une authentification (création, modification, suppression)
- **Service de sécurité**: `SecurityService` pour accéder aux informations de l'utilisateur authentifié
- **Contrôles d'accès**: Vérification de propriété pour la modification/suppression des annonces

### Contract-First Development
- **API Contract**: Defined in `contract.yaml` (OpenAPI 3.0.3)
- **Backend**: Generates server interfaces via quarkus-openapi-generator-server
- **Frontend**: Should generate TypeScript client (currently using contract.yaml copy)


## Fonctionnalités Principales

### 1. Gestion des Annonces
- **Types** : Vente ou Location
- **Nature** : Offre ou Demande
- **Propriétés** :
  - Titre et description (Markdown)
  - Prix (avec période pour locations : jour/semaine/mois)
  - Photos (max 10 avec miniatures)
  - Localisation avec coordonnées GPS
  - Statut (active/suspendue/expirée)

### 2. Recherche et Filtrage
- **Recherche textuelle** dans titre/description
- **Filtres** :
  - Type (vente/location) et nature (offre/demande)
  - Fourchette de prix (min/max)
  - Distance maximale depuis un point
- **Tri** :
  - Date de création/modification
  - Prix (croissant/décroissant)
  - Distance géographique
  - Alphabétique

### 3. Géolocalisation
- Saisie d'adresse avec autocomplétion
- Coordonnées GPS optionnelles
- Calcul de distance entre annonces
- Tri par proximité géographique

### 4. Gestion Utilisateurs
- Profils avec avatar
- Authentification sécurisée

### 5. Gestion des Photos
- Upload multiple (drag & drop)
- Génération automatique de miniatures
- Réorganisation par ordre d'affichage
- Suppression individuelle

## Spécifications Techniques

### API Endpoints Principaux

Contrat dans openapi.yaml à la racine.

```
GET    /api/v1/annonces                    # Liste avec filtres/tri/pagination
POST   /api/v1/annonces                    # Création d'annonce
GET    /api/v1/annonces/{id}               # Détail d'une annonce
PUT    /api/v1/annonces/{id}               # Modification
DELETE /api/v1/annonces/{id}               # Suppression
POST   /api/v1/photos                      # Upload photos
DELETE /api/v1/photos/{photoId}            # Suppression photo
```

### Modèles de Données Clés
- **Annonce** : type, nature, titre, description, prix, localisation, photos, utilisateur
- **Utilisateur** : pseudo, avatar
- **Photo** : URL, miniature, ordre d'affichage
- **Localisation** : coordonnées GPS

### Contraintes de Validation
- Titre : 5-100 caractères
- Description : max 2000 caractères (Markdown)
- Photos : max 10 par annonce
- Prix : décimal positif

## Objectifs d'Expérience Utilisateur

### Interface Intuitive
- Navigation claire entre offres/demandes
- Formulaires de création guidés
- Filtres visuels et ergonomiques

### Performance
- Chargement rapide des listes d'annonces
- Pagination optimisée
- Images optimisées (formats WebP, lazy loading)
- Recherche en temps réel avec debouncing

### Responsive Design
- Adaptation mobile-first
- Gestes tactiles pour la navigation photos
- Interface optimisée pour tous écrans

## Considérations de Sécurité

- Validation côté client ET serveur
- Sanitisation des contenus Markdown
- Authentification JWT

## Notes d'Implémentation

### Géolocalisation
- Utiliser une API de géocodage (OpenStreetMap)
- Calcul de distance : formule de Haversine
- Indexation spatiale en base pour les performances

### Photos
- Stockage : filesystem local
- Génération de miniatures : Java natif
- Formats supportés : JPEG, PNG, WebP

### Recherche
- Index full-text en base de données

## Development Commands

### Backend (Quarkus) - trouvaille-back/
```bash
# Build
cd trouvaille-back && mvn clean package

# Development mode (hot reload)
cd trouvaille-back && mvn quarkus:dev

# Run tests
cd trouvaille-back && mvn test

# Run integration tests
cd trouvaille-back && mvn verify

# Generate OpenAPI implementation from contract.yaml
cd trouvaille-back && mvn quarkus:generate-code
```

### Frontend (Vue 3) - trouvaille-front/
```bash
# Install dependencies
cd trouvaille-front && npm install

# Development server
cd trouvaille-front && npm run dev

# Build for production
cd trouvaille-front && npm run build

# Type checking
cd trouvaille-front && npm run type-check

# Lint and fix
cd trouvaille-front && npm run lint

# Format code
cd trouvaille-front && npm run format

# Preview production build
cd trouvaille-front && npm run preview
```

### Code Generation
- **Backend**: OpenAPI server interface generation via quarkus-openapi-generator-server from contract.yaml
- **Frontend**: TypeScript client generation from contract.yaml (needs setup)
