# Stage 1: Build the application
FROM gradle:8.3-jdk17 AS build
LABEL authors="Johannes Christopher Janka"
WORKDIR /usr/src/app

# Copy source files
COPY . .

# Build the application
RUN gradle clean build -x test

# Debugging step: List contents of the build/libs directory
RUN ls -l /usr/src/app/build/libs

# Stage 2: Run the application
FROM openjdk:17-jdk-slim
WORKDIR /usr/src/app

# Copy the JAR file from the build stage
COPY --from=build /usr/src/app/build/libs/inventory-opt-service-0.0.1-SNAPSHOT.jar /usr/src/app/app.jar

# Expose the port the application runs on
EXPOSE 8085

# Execute the application
CMD ["java", "-jar", "app.jar"]