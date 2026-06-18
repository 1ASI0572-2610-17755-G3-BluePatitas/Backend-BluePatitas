# Stage 1: Build the application
FROM eclipse-temurin:26-jdk AS build
WORKDIR /app

# Copy the maven wrapper and pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Ensure the maven wrapper script has executable permissions
RUN chmod +x mvnw

# Download dependencies (offline mode to cache them)
RUN ./mvnw dependency:go-offline -B

# Copy the source code and build the application jar
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:26-jre
WORKDIR /app

# Copy the built jar file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port 8080 (Render will override this using the PORT environment variable)
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
