# Use Eclipse Temurin for Java 21
FROM eclipse-temurin:21-jdk-jammy

# Standard setup
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]