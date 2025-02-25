FROM openjdk:21-jdk-slim

WORKDIR /app/gateway-service

# Instalar netcat para wait-for-it
RUN apt-get update && apt-get install -y netcat-traditional && rm -rf /var/lib/apt/lists/*

# Copiar el JAR
COPY ./target/api-gateway-0.0.1-SNAPSHOT.jar /app/gateway-service/api-gateway-0.0.1-SNAPSHOT.jar

# Copiar wait-for-it
COPY wait-for-it.sh /app/wait-for-it.sh
RUN chmod +x /app/wait-for-it.sh

EXPOSE 8080

# Esperar a naming-service en el puerto 8761 antes de ejecutar el gateway
ENTRYPOINT ["/app/wait-for-it.sh", "naming-service", "8761", "--", "java", "-jar", "/app/gateway-service/api-gateway-0.0.1-SNAPSHOT.jar"]
