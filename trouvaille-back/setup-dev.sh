#!/bin/bash

# Script de configuration pour l'environnement de développement

echo "🚀 Setting up development environment for Trouvaille Backend"
echo ""

# 1. Créer le fichier .env s'il n'existe pas
if [ ! -f .env ]; then
    echo "📝 Creating .env file from template..."
    cp .env.example .env
    echo "✅ .env file created. Please edit it with your configuration."
    echo ""
else
    echo "✅ .env file already exists"
    echo ""
fi

# 2. Générer les clés JWT si elles n'existent pas
if [ ! -f data/keys/privateKey.pem ] || [ ! -f data/keys/publicKey.pem ]; then
    echo "🔐 Generating JWT RSA key pair..."
    ./generate-keys.sh
    echo ""
else
    echo "✅ JWT keys already exist"
    echo ""
fi

# 3. Créer le répertoire pour les photos
mkdir -p data/photos
echo "✅ Photos directory created: data/photos"
echo ""

echo "🎉 Development environment setup complete!"
echo ""
echo "Next steps:"
echo "1. Edit .env file with your OAuth configuration"
echo "2. Run './run-dev.sh' to start the development server"
echo ""