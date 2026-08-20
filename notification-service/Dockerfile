# Build stage
FROM maven:3.9.11-eclipse-temurin-17 AS builder
WORKDIR /app
VOLUME /tmp
COPY pom.xml ./pom.xml
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests=true

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar notification-service.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "/app/notification-service.jar"]
