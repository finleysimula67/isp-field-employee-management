# Stage 1: Build the application with Maven using a Java 25 compatible JDK
FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . .
# Force Maven to compile targeting Java 25 if your toolchain requires it
RUN mvn clean package -DskipTests

# Stage 2: Run the application using the official OpenJDK 25 runtime environment
FROM openjdk:25-slim
# Copies your exact jar file from the build folder
COPY --from=build /target/workflow-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]