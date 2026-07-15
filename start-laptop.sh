#!/usr/bin/env bash
# Levanta el stack completo de a poco, en vez de todo junto, para no saturar
# de golpe la CPU/RAM de una maquina con menos recursos (ej. laptop) -- lanzar
# los 12 contenedores + MySQL a la vez ahi puede tirar la maquina y dejar
# todo en loop de reintentos sin llegar nunca a levantar bien.
#
# Uso: ./start-laptop.sh

set -e
cd "$(dirname "$0")"

docker compose up -d bookpoint-mysql
sleep 5

docker compose up -d eureka-server
sleep 15

# Orden siguiendo la cadena de dependencias real (igual que la coleccion Postman)
docker compose up -d catalogo
sleep 10

docker compose up -d clientes
sleep 10

docker compose up -d inventario
sleep 10

docker compose up -d carrito
sleep 10

docker compose up -d pedidos
sleep 10

docker compose up -d pagos
sleep 10

docker compose up -d envios
sleep 10

docker compose up -d resenas
sleep 10

docker compose up -d notificaciones
sleep 10

docker compose up -d sucursales
sleep 10

docker compose up -d gateway

echo "Listo. Esperar 30-60s mas antes de confiar en llamadas cruzadas / via Gateway."
