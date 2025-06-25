#!/bin/bash

# Charger les variables d'environnement depuis .env si le fichier existe
if [ -f .env ]; then
    echo "Loading environment variables from .env"
    export $(grep -v '^#' .env | xargs)
else
    echo "Warning: .env file not found. Please create one based on .env.example"
fi

# Lancer Quarkus en mode dev
mvn quarkus:dev