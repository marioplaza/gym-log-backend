# Fase 1: Construir el JAR de la aplicación con Maven
FROM maven:3.9-eclipse-temurin-21 AS build

# Copiar el código fuente
WORKDIR /app
COPY . .

# Construir el artefacto
RUN mvn clean install -DskipTests

# Fase 2: Crear la imagen final de la aplicación
FROM eclipse-temurin:21-jre-jammy

# Crear un usuario no-root para mejorar la seguridad
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

# Copiar el JAR desde la fase de build
ARG JAR_FILE=target/*.jar
COPY --from=build /app/${JAR_FILE} app.jar

# Exponer el puerto
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
