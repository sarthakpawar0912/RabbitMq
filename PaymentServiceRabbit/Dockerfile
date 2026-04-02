# Use Eclipse Temurin for Java 21
FROM eclipse-temurin:21-jdk-jammy

# Set working directory
WORKDIR /app

# Copy jar file
COPY target/*.jar app.jar

# Expose port
EXPOSE 8081

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]