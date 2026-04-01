#!/bin/bash

###############################################################################
# IBM Bob - Docker Startup Script
# 
# This script starts the complete application stack with Docker
###############################################################################

set -e

echo "╔════════════════════════════════════════════════════════════════════╗"
echo "║         IBM Bob - Starting Docker Stack                            ║"
echo "╔════════════════════════════════════════════════════════════════════╗"
echo ""

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Error: Docker is not installed"
    echo "   Please install Docker Desktop from: https://www.docker.com/products/docker-desktop"
    exit 1
fi

# Check if Docker is running
if ! docker info &> /dev/null; then
    echo "❌ Error: Docker is not running"
    echo "   Please start Docker Desktop"
    exit 1
fi

echo "✅ Docker is installed and running"
echo ""

cd docker

# Use the new docker compose command (without hyphen)
echo "🚀 Starting services..."
echo ""

if docker compose version &> /dev/null; then
    # New Docker Compose V2 (built into Docker)
    docker compose up -d
else
    # Fallback to old docker-compose
    docker-compose up -d
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ Services Started Successfully!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📊 Access the services:"
echo ""
echo "   🌐 Application:    http://localhost:8080"
echo "   📚 Swagger UI:     http://localhost:8080/swagger-ui.html"
echo "   💚 Health Check:   http://localhost:8080/actuator/health"
echo "   📈 Prometheus:     http://localhost:9090"
echo "   📊 Grafana:        http://localhost:3000 (admin/admin)"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📝 Useful commands:"
echo ""
echo "   View logs:         docker compose logs -f billing-app"
echo "   Stop services:     docker compose down"
echo "   Restart:           docker compose restart"
echo ""
echo "⏳ Note: The application may take 30-60 seconds to fully start"
echo "   Check health: curl http://localhost:8080/actuator/health"
echo ""

# Made with Bob
