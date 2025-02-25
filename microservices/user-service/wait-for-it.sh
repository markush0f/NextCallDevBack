#!/bin/bash
# Esperar a que config-server esté disponible
until curl -s http://config-server:8888/actuator/health; do
  echo "Esperando config-server..."
  sleep 2
done

# Ejecutar el servicio Eureka
exec java -jar /app/service-registry.jar
