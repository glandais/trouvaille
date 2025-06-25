#!/bin/bash

# Script pour générer les clés JWT RSA

KEYS_DIR="data/keys"

# Créer le répertoire s'il n'existe pas
mkdir -p "$KEYS_DIR"

echo "Generating JWT RSA key pair..."

# Générer la clé privée
echo "1. Generating private key..."
openssl genpkey -algorithm RSA -out "$KEYS_DIR/privateKey.pem"

if [ $? -eq 0 ]; then
    echo "✅ Private key generated: $KEYS_DIR/privateKey.pem"
else
    echo "❌ Failed to generate private key"
    exit 1
fi

# Générer la clé publique correspondante
echo "2. Generating public key..."
openssl rsa -in "$KEYS_DIR/privateKey.pem" -pubout -out "$KEYS_DIR/publicKey.pem"

if [ $? -eq 0 ]; then
    echo "✅ Public key generated: $KEYS_DIR/publicKey.pem"
else
    echo "❌ Failed to generate public key"
    exit 1
fi

# Définir les permissions appropriées
chmod 600 "$KEYS_DIR/privateKey.pem"
chmod 644 "$KEYS_DIR/publicKey.pem"

echo ""
echo "🔐 JWT RSA key pair generated successfully!"
echo "   Private key: $KEYS_DIR/privateKey.pem (600 permissions)"
echo "   Public key:  $KEYS_DIR/publicKey.pem (644 permissions)"
echo ""
echo "⚠️  Important: Keep the private key secure and never commit it to version control!"