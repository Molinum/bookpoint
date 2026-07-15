# BookPoint - Backend de Microservicios

Plataforma backend distribuida para la gestión de una cadena de librerías con sucursales y bodegas conectadas.
Proyecto desarrollado para la asignatura **DSY1103 - Desarrollo Fullstack I** (Duoc UC).

Cada microservicio es un módulo Maven independiente construido sobre **Spring Boot 4.1**, **Java 21** y su propia base de datos en un servidor **MySQL** compartido (puerto `3306`, una base por servicio). La comunicación entre servicios es síncrona vía REST (Feign), con descubrimiento de servicios (Eureka) y un API Gateway como punto de entrada único.

## Arquitectura y puertos

| Microservicio | Carpeta | Puerto | Base de datos |
|---|---|---|---|
| Servidor de Descubrimiento (Eureka) | `eureka-server` | 8761 | *N/A* |
| API Gateway | `gateway` | 8080 | *N/A* |
| Catálogo de Libros | `catalogo` | 8081 | `bookpoint_catalogo_db` |
| Inventario y Stock | `inventario` | 8082 | `bookpoint_inventario_db` |
| Ventas y Pedidos | `pedidos` | 8083 | `bookpoint_pedidos_db` |
| Gestión de Clientes | `clientes` | 8084 | `bookpoint_clientes_db` |
| Gestión de Sucursales | `sucursales` | 8085 | `bookpoint_sucursales_db` |
| Carrito de Compras | `carrito` | 8086 | `bookpoint_carrito_db` |
| Pasarela de Pagos | `pagos` | 8087 | `bookpoint_pagos_db` |
| Envíos y Logística | `envios` | 8088 | `bookpoint_envios_db` |
| Notificaciones y Alertas | `notificaciones` | 8089 | `bookpoint_notificaciones_db` |
| Reseñas y Calificaciones | `resenas` | 8090 | `bookpoint_resenas_db` |

Todas las bases de datos viven en el mismo servidor MySQL (una base por servicio, no compartida), corriendo en `localhost:3306` tanto en modo local como en Docker.

## Requisitos previos

- **Java 21** y **Maven** (o usar el wrapper si el equipo lo tiene configurado).
- **Docker** y **Docker Compose** (para levantar todo el stack de una vez, o solo MySQL).
- Un cliente para hacer requests HTTP: **Postman** (hay una colección lista en [`postman/`](postman/)) o el navegador (Swagger UI).

## Cómo levantar el proyecto

Hay dos formas de correrlo. Para probar el flujo completo (varios servicios hablando entre sí), **Docker es más simple**. Para trabajar/debuggear un solo servicio, es más rápido correrlo local con Maven.

### Opción A: Todo con Docker (recomendada para probar el flujo completo)

```bash
# 1. Compilar los jars de los servicios (salta los tests para que sea rápido)
./rebuild-jars.sh

# 2. Levantar todo el stack
docker compose up --build -d
```

**Importante:** al modificar el código, es necesario volver a ejecutar `./rebuild-jars.sh` y **no omitir el flag `--build`** al hacer `docker compose up` — sin él, Docker reutiliza la imagen anterior y el código nuevo nunca llega a ejecutarse (no se produce ningún error, simplemente corre el jar anterior).

Para levantar solo algunos servicios (más rápido si no se necesitan todos):

```bash
docker compose up --build -d catalogo pedidos inventario sucursales carrito clientes envios notificaciones pagos resenas eureka-server
```

Para ver los logs de un servicio mientras arranca:

```bash
docker logs -f catalogo-ms
```

Para apagar todo (dejando la data en el volumen de MySQL intacta):

```bash
docker compose stop
```

**Se recomienda considerar el tiempo de propagación de Eureka al levantar todo junto:** los servicios pueden tardar entre 30 y 90 segundos en verse entre sí después de mostrar "Started" — es el tiempo normal que tarda Eureka en propagar el registro. Si una llamada entre microservicios (vía Gateway o Feign) falla con `503` justo después de levantar el stack, no se trata de un error: basta con esperar un poco y reintentar. Las llamadas directas a un servicio (sin pasar por otro) funcionan casi de inmediato.

### Opción B: Local con Maven (para desarrollar o debuggear un servicio puntual)

1. Levantar solo MySQL (no hace falta Docker para los servicios):
   ```bash
   docker compose up -d bookpoint-mysql
   ```
2. Levantar Eureka (varios servicios lo necesitan para registrarse, aunque igual arrancan sin él):
   ```bash
   cd eureka-server
   mvn spring-boot:run
   ```
3. En otra terminal, levantar el servicio que se desee probar, por ejemplo:
   ```bash
   cd catalogo
   mvn spring-boot:run
   ```

Cada servicio usa `localhost:3306` por defecto en su `application.yml`/`.yaml`, así que no hace falta ninguna configuración extra para conectarse a la MySQL de Docker.

## Datos de prueba (Datafaker)

Todos los servicios generan datos de ejemplo realistas automáticamente la primera vez que arrancan con su tabla vacía (usando [Datafaker](https://www.datafaker.net/)): libros, clientes, pedidos, pagos, envíos, etc., ya relacionados entre sí de forma consistente (un pedido con un libro y stock real, un pago con el monto correcto, etc.). No hay que cargar nada a mano para empezar a probar la API.

Si una tabla ya tiene datos (de una corrida anterior), el servicio no vuelve a sembrar — esto ocurre automáticamente, sin intervención necesaria. Para partir de cero, es necesario vaciar las tablas correspondientes en MySQL antes de levantar el servicio de nuevo.

## Cómo probar la API

### 1. Colección Postman (recomendado)

En [`postman/`](postman/) hay una colección lista para importar (`BookPoint.postman_collection.json`) con requests para los 10 servicios, ordenados para poder correrlos de arriba hacia abajo (cada creación guarda el ID resultante y el siguiente request lo reutiliza). Instrucciones detalladas en [`postman/README.md`](postman/README.md).

### 2. Swagger UI

Con cualquier servicio arriba, se puede abrir su documentación interactiva en el navegador:

```
http://localhost:<puerto-del-servicio>/swagger-ui.html
```

Por ejemplo, para catálogo: `http://localhost:8081/swagger-ui.html`.

### 3. Tests automatizados

Para correr toda la suite de pruebas unitarias (JUnit + Mockito) de los 10 servicios:

```bash
mvn test
```

Esto **no** requiere Docker ni MySQL corriendo para la mayoría de las pruebas (los mocks reemplazan la base de datos), aunque cada servicio también tiene un test de contexto (`XApplicationTests`) que sí necesita la conexión real a MySQL para poder levantar el contexto de Spring completo.

Para ver el reporte de cobertura de un servicio después de correr los tests:

```
<servicio>/target/site/jacoco/index.html
```

## Estructura del repositorio

```
bookpoint/
├── eureka-server/       # Servidor de descubrimiento
├── gateway/              # API Gateway (punto de entrada único, puerto 8080)
├── catalogo/             # Uno de estos por cada microservicio de negocio...
├── clientes/
├── inventario/
├── pedidos/
├── sucursales/
├── carrito/
├── pagos/
├── envios/
├── notificaciones/
├── resenas/
├── postman/              # Colección Postman + instrucciones de uso
├── docker-compose.yml    # Orquesta los 12 contenedores + MySQL
├── rebuild-jars.sh       # Recompila solo los jars que cambiaron (para Docker)
└── pom.xml               # POM padre (dependencias y versiones compartidas)
```

Cada microservicio de negocio sigue la misma estructura interna: `controller` → `service` (con su interfaz + `impl`) → `repository`, más `model`/`dto`, `client` (Feign, si consume otro servicio), `exception` (manejo de errores) y `config` (incluye el `DataSeeder` de Datafaker).

## Problemas comunes

- **Puerto 3306 ocupado**: si existe una instancia de MySQL corriendo aparte (fuera de este proyecto), entrará en conflicto con el contenedor `bookpoint-mysql`. Es necesario detener la otra instancia antes de levantar Docker, o cambiar el mapeo de puerto en `docker-compose.yml`.
- **`docker compose up` sin `--build`**: reutiliza la imagen anterior aunque se haya recompilado el jar. Se debe usar `--build` siempre después de modificar el código.
- **Error 503 / "Load balancer does not contain an instance"**: es necesario esperar 30-90 segundos después de levantar el stack completo — es la propagación normal de Eureka, no un error de configuración.
