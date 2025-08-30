
#FROM eclipse-temurin:17-jdk-alpine

#WORKDIR /app

#COPY applications/app-service/build/libs/requestloan-credya-reactivo.jar app.jar

#EXPOSE 5001


#ENTRYPOINT ["java", "-jar", "app.jar"]

# Etapa 1: build del JAR
# Etapa 1: Compilación
FROM gradle:8.4.0-jdk17 AS builder

WORKDIR /app

# Copiamos todo el proyecto al contenedor
COPY . .

# Ejecutamos el build SOLO del módulo app-service, sin tests
RUN ./gradlew :app-service:bootJar -x test -x validateStructure

# Etapa 2: Imagen de ejecución
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copiamos solo el .jar generado en el paso anterior
COPY --from=builder /app/applications/app-service/build/libs/*.jar app.jar

# Puerto de tu aplicación (ajústalo si es otro)
EXPOSE 8080

# Comando para ejecutar el .jar
ENTRYPOINT ["java", "-jar", "app.jar"]
