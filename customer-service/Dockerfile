# Stage 1: Build the application
FROM gradle:8.3-jdk17 AS build
LABEL authors="Johannes Christopher Janka"
WORKDIR /usr/src/app

# Copy source files
COPY . .

# Build the application
RUN gradle clean build -x test

# Stage 2: Run the application
FROM eclipse-temurin:17-jre-alpine
WORKDIR /usr/src/app

# Copy the JAR file from the build stage
COPY --from=build /usr/src/app/build/libs/*.jar customer-service.jar

# Expose the port the application runs on
EXPOSE 8082

# Execute the application
CMD ["java", "-jar", "customer-service.jar"]
