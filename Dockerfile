# ── Etapa 1: build ───────────────────────────────────────────────────────
# Compila el proyecto con Maven dentro de un contenedor, así no es necesario
# tener Maven instalado en la máquina del desarrollador.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar solo el pom.xml primero: Docker cachea esta capa y no vuelve a
# descargar dependencias si el código cambia pero el pom.xml no.
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# ── Etapa 2: runtime ─────────────────────────────────────────────────────
# Imagen final mucho más ligera: solo el JRE, sin Maven ni código fuente.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Healthcheck usado por docker-compose para esperar a que el backend
# esté realmente listo antes de marcarlo como "healthy"
HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=5 \
  CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
