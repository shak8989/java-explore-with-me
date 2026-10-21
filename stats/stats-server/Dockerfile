
# Этап 1. Собираем Java-приложение через Maven
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build


COPY pom.xml .
COPY stats ./stats
COPY ewm-main-service ./ewm-main-service
RUN mvn -pl stats/stats-server -am clean package -DskipTests

# Этап 2. Запускаем готовое приложение
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /build/stats/stats-server/target/stats-server-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]
