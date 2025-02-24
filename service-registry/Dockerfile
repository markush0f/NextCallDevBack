FROM openjdk:21-jdk-slim

WORKDIR /app/service-registry

# Instalar curl y netcat
RUN apt-get update && apt-get install -y curl netcat-openbsd && rm -rf /var/lib/apt/lists/*

# Copiar el archivo JAR de Eureka al contenedor
COPY ./target/service-registry-0.0.1-SNAPSHOT.jar /app/service-registry/service-registry-0.0.1-SNAPSHOT.jar

# Copiar el script de espera
COPY wait-for-it.sh /app/wait-for-it.sh

# Dar permisos de ejecución al script
RUN chmod +x /app/wait-for-it.sh

# Exponer el puerto para Eureka
EXPOSE 8761

# Usar wait-for-it para esperar a que config-server esté listo
ENTRYPOINT ["/app/wait-for-it.sh", "config-server", "8888", "--", "java", "-jar", "/app/service-registry/service-registry-0.0.1-SNAPSHOT.jar"]
