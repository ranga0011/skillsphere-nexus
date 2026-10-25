# Production Spring Boot Runtime Container using Eclipse Temurin JRE 25
FROM eclipse-temurin:25-jre
WORKDIR /app

# Copy the pre-compiled Spring Boot executable JAR
COPY target/skill-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

