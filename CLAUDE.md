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
- **Frontend** : Vue 3 + Vite
- **State Management** : Pinia
- **Styling** : Tailwind CSS
- **Backend** : Quarkus
- **Base de données** : MongoDB
- **Authentification** : JWT

### Authentification

OAuth endpoints :
- Authorize URI https://chat.n-peloton.fr/oauth/authorize
- Token URI https://chat.n-peloton.fr/oauth/access_token
- User info URI https://chat.n-peloton.fr/api/v4/users/me

### Frontend

Sous dossier trouvaille-front

Standard Vue 3 / Vite / Pinia / Tailwind CSS

Génération du client Typescript depuis le fichier contract.yaml à la racine de trouvaille-front

### Backend

Sous dossier trouvaille-back

Standard Quarkus (Panache MongoDB, Quarkus REST, MapStruct)

Génération de l'interface à implémentation avec quarkus-openapi-generator-server


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

## Commands

### Build back
```bash
cd trouvaille-back
mvn clean package
```

### Build front
```bash
cd trouvaille-front
npm run build
```
