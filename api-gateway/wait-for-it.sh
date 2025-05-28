#!/bin/bash
set -e

host="$1"
port="$2"
shift 2
cmd="$@"

echo "Esperando a que $host:$port esté disponible..."

while ! nc -z "$host" "$port"; do
  sleep 1
done

echo "$host:$port está disponible, ejecutando el comando: $cmd"
exec $cmd
