# Usa una imagen base de OpenJDK
FROM openjdk:21-jdk-slim

# Establece el directorio de trabajo
WORKDIR /app

# Copia el archivo JAR desde el directorio target al contenedor
COPY ./target/user-service-0.0.1-SNAPSHOT.jar /app/user-service.jar

# Expone el puerto que la aplicación va a usar
EXPOSE 8081

# Comando para ejecutar el JAR
ENTRYPOINT ["java", "-jar", "user-service.jar"]
