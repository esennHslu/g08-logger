# Deployment stage using Eclipse Temurin JRE for a smaller image size
FROM eclipse-temurin:21-jre

# Set the working directory in the final image
WORKDIR /app

# Copy the entire target directory for debugging
COPY logger-server/target /app/target

# Expose the port the app runs on
EXPOSE 8080

RUN ls -lisat /app/target
# Set the startup command to execute the jar
CMD ["java", "-jar", "/app/target/g08-loggerserver-2.0.0-SNAPSHOT.jar"]
