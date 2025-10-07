#!/bin/bash
set -e

echo "🔨 Building JAR..."
cd /Users/grexrr/Documents/scavengerHunt/backend
mvn clean package -DskipTests

echo "🐳 Building Docker image..."
docker build -t spring-backend:local .

echo "🛑 Stopping old container..."
docker stop spring-backend 2>/dev/null || true
docker rm spring-backend 2>/dev/null || true

echo "🚀 Starting new container..."
# docker run -d \
#   --name spring-backend \
#   -p 8443:8080 \
#   -e SPRING_DATA_MONGODB_URI=mongodb://mongo-scavenger:27017/scavengerhunt \
#   --link mongo-scavenger:mongo-scavenger \
#   spring-backend:local

docker run -d --name spring-backend \
  --network scavenger-net \
  -p 8443:8080 \
  -e SPRING_DATA_MONGODB_URI=mongodb://mongo-scavenger:27017/scavengerhunt \
  spring-backend:local

echo "✅ Deployment complete! Checking logs..."
sleep 3
docker logs spring-backend --tail 50

echo ""
echo "📍 Access points:"
echo "   Swagger UI: http://localhost:8443/swagger-ui.html"
echo "   OpenAPI:    http://localhost:8443/api-docs"
echo "   Health:     http://localhost:8443/actuator/health"


docker network connect scavenger-net spring-backend