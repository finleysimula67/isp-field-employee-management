# Stage 1: Use a pure Eclipse Temurin JDK 25 image and add Maven manually
FROM eclipse-temurin:25-jdk AS build

# Install Maven manually in the container
RUN apt-get update && apt-get install -y maven

WORKDIR /app
COPY . .

# Run the compilation targeting Java 25
RUN mvn clean package -DskipTests

# Stage 2: Clean, fast Eclipse Temurin Java 25 JRE runtime environment
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/workflow-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
