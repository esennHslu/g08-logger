# Deployment stage using Eclipse Temurin JRE for a smaller image size
FROM eclipse-temurin:21-jre
# Expose the port the app runs on
EXPOSE 9999/tcp
# Set the working directory in the final image
WORKDIR /app
# Set the startup command to execute the jar
CMD ["java", "-jar", "/app/target/g08-loggerserver.jar"]
# Copy the entire target directory for debugging
COPY logger-server/target /app/target
