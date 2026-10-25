#!/bin/bash
# clean.sh — Remove compiled artifacts and node_modules

echo "==> Cleaning backend..."
cd backend && mvn clean -q && cd ..

echo "==> Cleaning frontend..."
rm -rf frontend/node_modules frontend/build

echo "✅  Clean complete."
