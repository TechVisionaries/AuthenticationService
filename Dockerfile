# Use a lightweight official OpenJDK 17 image based on Alpine Linux
FROM openjdk:17-jdk-alpine

# Create a writable mount point for temporary files (used by Spring Boot)
VOLUME /tmp

# Copy the JAR file from the target directory into the image and name it app.jar
COPY ${JAR_FILE} app.jar

# Define the command to run the Spring Boot application using the JAR file
ENTRYPOINT ["java","-jar","/app.jar"]
