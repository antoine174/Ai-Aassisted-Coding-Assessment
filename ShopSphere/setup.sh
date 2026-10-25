#!/bin/bash
# setup.sh — Install dependencies for ShopSphere

set -e

echo "==> Checking Java..."
java -version 2>&1 | head -1

echo "==> Checking Maven..."
mvn -version | head -1

echo "==> Installing backend dependencies..."
cd backend && mvn dependency:resolve -q && cd ..

echo "==> Checking Node.js..."
node -version

echo "==> Installing frontend dependencies..."
cd frontend && npm install --silent && cd ..

echo ""
echo "✅  Setup complete. Run ./run.sh to start both servers."
