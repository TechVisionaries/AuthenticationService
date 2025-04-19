# Use a base image with Java
FROM openjdk:17-jdk-alpine

# Set the working directory
WORKDIR /app

# Copy the jar file into the container
COPY target/*.jar app.jar

# Expose the port your app runs on
EXPOSE 8080

# Command to run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]
