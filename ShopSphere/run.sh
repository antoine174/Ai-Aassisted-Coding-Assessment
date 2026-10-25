#!/bin/bash
# run.sh — Start backend and frontend dev servers

set -e

echo "==> Starting Spring Boot backend on port 8080..."
cd backend && mvn spring-boot:run &
BACKEND_PID=$!
cd ..

echo "==> Waiting for backend to start..."
sleep 10

echo "==> Starting React frontend on port 3000..."
cd frontend && npm start &
FRONTEND_PID=$!
cd ..

echo ""
echo "✅  ShopSphere is running!"
echo "   Backend:  http://localhost:8080"
echo "   Frontend: http://localhost:3000"
echo ""
echo "Press Ctrl+C to stop both servers."

trap "kill $BACKEND_PID $FRONTEND_PID 2>/dev/null" EXIT
wait
