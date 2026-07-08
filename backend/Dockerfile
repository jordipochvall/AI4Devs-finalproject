# ---- Etapa 1: compilación ----
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copiar POMs primero para aprovechar la caché de capas de Docker
COPY pom.xml .
COPY nova-common/pom.xml      nova-common/
COPY nova-domain/pom.xml      nova-domain/
COPY nova-application/pom.xml nova-application/
COPY nova-infrastructure/pom.xml nova-infrastructure/
COPY nova-simulator/pom.xml   nova-simulator/
COPY nova-web-api/pom.xml     nova-web-api/

RUN mvn dependency:go-offline -B -q

# Copiar fuentes y compilar (skip tests: se ejecutan en CI, no en la imagen)
COPY nova-common/src      nova-common/src
COPY nova-domain/src      nova-domain/src
COPY nova-application/src nova-application/src
COPY nova-infrastructure/src nova-infrastructure/src
COPY nova-simulator/src   nova-simulator/src
COPY nova-web-api/src     nova-web-api/src

RUN mvn -B package -DskipTests -q

# ---- Etapa 2: runtime ----
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Crear usuario no-root
RUN addgroup -S novacasino && adduser -S novacasino -G novacasino
USER novacasino

COPY --from=build /app/nova-web-api/target/*.jar app.jar

EXPOSE 8080
# -XX:MaxRAMPercentage raises the container-aware heap cap above the JDK's conservative 25% default,
# so the app fits comfortably within a small container's memory (e.g. Render's free 512 MB tier)
# instead of leaving most of it unused.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
