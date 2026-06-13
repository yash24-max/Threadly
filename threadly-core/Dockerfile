FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Resolve deps first (layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Build
COPY src ./src
RUN mvn package -Dmaven.test.skip=true -q

# Runtime image
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
