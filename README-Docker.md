# Trouvaille - Déploiement Docker

Ce guide explique comment déployer l'application Trouvaille avec Docker et Docker Compose.

## Architecture

L'application est composée de plusieurs services :

- **Frontend** : Application Vue.js servie par Nginx
- **Backend** : API Quarkus (JVM)
- **MongoDB** : Base de données
- **Traefik** : Reverse proxy et load balancer

## Prérequis

- Docker >= 20.10
- Docker Compose >= 2.0
- Credentials OAuth configurés

## Installation rapide

1. **Cloner et configurer**
   ```bash
   git clone <repository>
   cd trouvaille
   cp .env.example .env
   # Éditer .env avec vos credentials OAuth
   ```

2. **Builder et démarrer**
   ```bash
   ./deploy.sh build
   ./deploy.sh up
   ```

3. **Accéder à l'application**
   
   **Mode Production (avec Nginx):**
   - Application : http://localhost:8090 (port configurable via HTTP_PORT)

## Commandes disponibles

### Script de build (`./build.sh`)

```bash
# Build images
./build.sh latest

# Build avec tag personnalisé
./build.sh v1.0.0
```

### Script de déploiement (`./deploy.sh`)

```bash
# Démarrer tous les services
./deploy.sh up

# Arrêter tous les services
./deploy.sh stop

# Redémarrer
./deploy.sh restart

# Voir les logs
./deploy.sh logs

# Vérifier le status
./deploy.sh status

# Nettoyer tout (attention!)
./deploy.sh clean
```

## Intégration avec Nginx et Certbot

L'application est conçue pour s'intégrer dans une infrastructure existante avec Nginx et Certbot.

### Configuration Nginx

1. **Copier l'exemple de configuration**
   ```bash
   cp nginx-integration.conf.example /etc/nginx/sites-available/trouvaille
   ```

2. **Adapter la configuration**
   ```nginx
   # Dans votre configuration Nginx
   location / {
       proxy_pass http://127.0.0.1:8090;  # Port configurable
       proxy_set_header Host $host;
       proxy_set_header X-Forwarded-Proto $scheme;
       # ... autres headers
   }
   ```

3. **Activer le site**
   ```bash
   ln -s /etc/nginx/sites-available/trouvaille /etc/nginx/sites-enabled/
   nginx -t && systemctl reload nginx
   ```

### Configuration des ports

- **HTTP_PORT** : Port d'exposition de Traefik (défaut: 8090)

## Configuration

### Variables d'environnement (.env)

```bash
# OAuth (obligatoire)
OAUTH_CLIENT_ID=your-oauth-client-id
OAUTH_CLIENT_SECRET=your-oauth-client-secret

# MongoDB
MONGO_INITDB_ROOT_USERNAME=admin
MONGO_INITDB_ROOT_PASSWORD=password

# Application
QUARKUS_LOG_LEVEL=INFO
```

### Volumes persistants

Les données sont stockées dans des volumes Docker :
- `mongodb_data` : Données MongoDB
- `photos_data` : Photos uploadées
- `jwt_keys` : Clés JWT

## Surveillance et monitoring

### Logs
```bash
# Tous les services
docker compose logs -f

# Un service spécifique
docker compose logs -f backend
docker compose logs -f frontend
```

### Métriques Traefik
- Dashboard : http://localhost:8080
- Métriques : endpoints `/metrics` sur chaque service

### Santé des services
```bash
# Vérifier les containers
docker compose ps

# Health check frontend
curl http://localhost/health

# Health check backend  
curl http://localhost/api/health
```

## Dépannage

### Services ne démarrent pas
```bash
# Vérifier les logs
./deploy.sh logs

# Vérifier la configuration
cat .env

# Redémarrer proprement
./deploy.sh down
./deploy.sh up
```

### Problèmes de permissions
```bash
# Sur Linux, s'assurer que Docker est accessible
sudo usermod -aG docker $USER
# Se reconnecter

# Vérifier les volumes
docker volume ls
docker volume inspect trouvaille_photos_data
```

### MongoDB connection issues
```bash
# Vérifier que MongoDB est prêt
docker compose exec mongodb mongosh --eval "db.runCommand('ping')"

# Recréer les volumes si nécessaire
./deploy.sh down
docker volume rm trouvaille_mongodb_data
./deploy.sh up
```

## Production

### Sécurité
1. Changer les mots de passe par défaut
2. Utiliser HTTPS avec certificats SSL
3. Configurer un firewall
4. Mettre à jour les images régulièrement

### Performance
1. Configurer les limites de ressources
2. Monitorer les métriques
3. Implémenter un backup MongoDB

### Exemple configuration production
```yaml
# docker-compose.override.yml
services:
  backend:
    deploy:
      resources:
        limits:
          memory: 512M
        reservations:
          memory: 256M
  
  frontend:
    deploy:
      resources:
        limits:
          memory: 128M
        reservations:
          memory: 64M
```

## Backup et restauration

### Backup MongoDB
```bash
docker compose exec mongodb mongodump --out /backup
docker cp container_id:/backup ./backup-$(date +%Y%m%d)
```

### Backup photos
```bash
docker run --rm -v trouvaille_photos_data:/data -v $(pwd):/backup alpine tar czf /backup/photos-$(date +%Y%m%d).tar.gz /data
```

### Restauration
```bash
# MongoDB
docker compose exec mongodb mongorestore /backup

# Photos
docker run --rm -v trouvaille_photos_data:/data -v $(pwd):/backup alpine tar xzf /backup/photos-DATE.tar.gz -C /
```

## Support

En cas de problème :
1. Vérifier les logs : `./deploy.sh logs`
2. Vérifier la configuration : `cat .env`
3. Tester les health checks
4. Consulter la documentation Quarkus/Vue.js