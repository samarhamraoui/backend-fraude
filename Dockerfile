# Build stage
FROM maven:3.8.7-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml first to leverage Docker cache for dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (WAR file)
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Install required libraries for email and PostgreSQL
RUN apk add --no-cache libc6-compat postgresql-client

# Create logs directory with appropriate permissions
RUN mkdir -p /app/logs && chmod 777 /app/logs

# Copy the built WAR file and log configuration
COPY --from=build /app/target/backend-fraude.war app.war
COPY --from=build /app/src/main/resources/log4j2.xml /app/log4j2.xml

# Expose port and set entry point
EXPOSE 8080
ENTRYPOINT ["java", "-Dlogging.config=/app/log4j2.xml", "-jar", "app.war"]

# Add a healthcheck to verify the container is running
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 CMD curl --fail http://localhost:8080/actuator/health || exit 1