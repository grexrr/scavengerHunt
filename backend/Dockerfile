# -------- Build Stage --------
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom.xml and download dependencies first (layer cached unless pom changes)
COPY pom.xml .
RUN mvn dependency:go-offline -B 2>/dev/null || true

COPY src ./src
RUN mvn clean package -DskipTest -B

# -------- Runtime Stage --------
FROM maven:3.9-eclipse-temurin-17 AS runtime
WORKDIR /app

# Run as non-root user
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
USER appuser
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
