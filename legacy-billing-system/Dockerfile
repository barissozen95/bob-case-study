# Dockerfile for Legacy Billing System (Java 8)
# Demonstrates the "before" state

FROM maven:3.8-openjdk-8 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:8-jre

WORKDIR /app

# Copy jar from builder
COPY --from=builder /app/target/legacy-billing-system-*.jar app.jar

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]