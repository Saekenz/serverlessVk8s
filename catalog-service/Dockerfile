# Stage 1: Build the application
FROM gradle:8.3-jdk17 AS build
LABEL authors="Johannes Christopher Janka"
WORKDIR /app
COPY . .

# Build the application
RUN gradle clean build -x test

# Stage 2: Create a lightweight production image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/build/libs/*.jar catalog-service.jar

# Expose the port the app should run on
EXPOSE 8081

# Execute the application
CMD ["java", "-jar", "catalog-service.jar"]