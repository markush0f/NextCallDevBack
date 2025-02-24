# Usamos la imagen base de OpenJDK
FROM openjdk:21-jdk-slim

# Especificamos el directorio de trabajo
WORKDIR /app

# Copiamos el archivo JAR del microservicio al contenedor
COPY ./target/restaurant-service.jar /app/restaurant-service.jar

# Exponemos el puerto que usará el microservicio (8082 en este caso)
EXPOSE 8082

# Definimos el comando para ejecutar el microservicio
ENTRYPOINT ["java", "-jar", "restaurant-service.jar"]
