#!/usr/bin/env bash
# Levanta todo sin Docker para los microservicios (mvn spring-boot:run nativo),
# de a poco igual que start-laptop.sh, para no saturar la maquina de golpe.
# MySQL sigue en Docker (un solo contenedor liviano) -- si no hay Docker
# disponible ni para eso, hace falta una instalacion nativa de MySQL en
# localhost:3306 en su lugar.
#
# Uso: ./start-laptop-nodocker.sh
# Logs de cada servicio en logs/<servicio>.log
# Para bajar todo: pkill -f spring-boot:run

set -e
cd "$(dirname "$0")"
mkdir -p logs

docker compose up -d bookpoint-mysql
sleep 5

run_service() {
  local nombre="$1"
  echo "Levantando $nombre..."
  (cd "$nombre" && mvn -q spring-boot:run > "../logs/$nombre.log" 2>&1 &)
}

run_service eureka-server
sleep 15

run_service catalogo
sleep 10
run_service clientes
sleep 10
run_service inventario
sleep 10
run_service carrito
sleep 10
run_service pedidos
sleep 10
run_service pagos
sleep 10
run_service envios
sleep 10
run_service resenas
sleep 10
run_service notificaciones
sleep 10
run_service sucursales

echo "Listo. Revisar logs/*.log si algo no arranca. pkill -f spring-boot:run para bajar todo."
