
# Этап 1. Собираем Java-приложение
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

COPY pom.xml .
COPY stats ./stats
COPY ewm-main-service ./ewm-main-service

RUN mvn -pl ewm-main-service -am clean package -DskipTests

# Этап 2. Готовим окружение для запуска
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /build/ewm-main-service/target/ewm-main-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
