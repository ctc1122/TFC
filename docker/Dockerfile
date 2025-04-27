# Usa una imagen base de Java
FROM openjdk:17-jdk-slim

# Directorio de trabajo en el contenedor
WORKDIR /app

# Copia el archivo JAR de la aplicación
COPY ../target/*.jar app.jar

# Puerto que expone la aplicación
EXPOSE 8080

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "app.jar"] 