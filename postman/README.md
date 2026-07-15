# BookPoint - Colección Postman

Colección para validar CRUD (JPA/ORM) y comunicación REST entre microservicios, requisito específico de la rúbrica ET (Situación 1: 4% CRUD + 5% REST entre microservicios).

## Requisitos

- Los 10 microservicios corriendo (local con `mvn spring-boot:run` o con Docker) más Eureka.
- **No pasa por el Gateway (8080) a propósito** — el Gateway y Eureka no están en la pauta del ET, así que la colección golpea cada servicio directo en su puerto, para aislar el CRUD y la comunicación entre microservicios de esa capa.
- MySQL disponible en el puerto 3306 (contenedor `bookpoint-mysql`).

## Cómo levantar el stack

Desde la raíz del proyecto:

```bash
./rebuild-jars.sh
docker compose up -d catalogo pedidos inventario sucursales carrito clientes envios notificaciones pagos resenas eureka-server
```

(`bookpoint-mysql` normalmente ya está corriendo aparte; si no, agrégalo al comando de arriba.)

Los servicios tardan ~20-40 segundos en levantar. Los llamados directos a cada servicio funcionan casi de inmediato; si algún request que depende de Feign (inventario, carrito, pedidos, pagos, envíos, reseñas) da `503` o `Load balancer does not contain an instance`, es el delay normal de propagación de Eureka (~30-90s) — no es un bug, solo hay que reintentar.

## Cómo importar y correr

1. Abrir Postman → **Import** → seleccionar `BookPoint.postman_collection.json`.
2. Las URLs base de cada servicio ya vienen como variables de colección (`catalogo_url`, `clientes_url`, etc.) — no hace falta crear un Environment aparte.
3. Ejecutar las carpetas **en orden, de arriba hacia abajo** (o usar **Run collection** / Collection Runner para correrlas todas de un tiro). El orden importa: cada carpeta reutiliza los IDs creados por la anterior, igual que las validaciones cruzadas via Feign en el código real:

   1. Catálogo (Libros)
   2. Clientes
   3. Inventario *(Feign → Catálogo)*
   4. Carrito *(Feign → Clientes + Catálogo)*
   5. Pedidos *(Feign → Catálogo + Inventario, descuenta stock)*
   6. Pagos *(Feign → Pedidos)*
   7. Envíos *(Feign → Pedidos)*
   8. Reseñas *(Feign → Catálogo + Clientes)*
   9. Notificaciones *(independiente)*
   10. Sucursales *(independiente)*
   11. Errores y validaciones cruzadas *(404 / 400)*

4. Cada request `POST` de creación guarda su ID en una variable de colección (`libroId`, `clienteId`, `pedidoId`, etc.) automáticamente vía script de test — no hay que copiar/pegar IDs a mano.
5. La carpeta 11 demuestra el mapeo de errores agregado en el proyecto: libro/pedido/pago inexistente → `404`, stock insuficiente → `400`, validación de campo (`@NotBlank`/`@NotNull`) → `400`.
6. **Clientes tiene JWT en `PUT`/`DELETE`** (registro, login y lecturas siguen abiertos). El request "Login" guarda el token en `{{clienteToken}}` automáticamente; los requests de actualizar/eliminar ya lo mandan en el header `Authorization: Bearer {{clienteToken}}`. Un cliente solo puede modificar su propio perfil — token de otro cliente devuelve `403`.

## Verificación

Todos los requests de esta colección fueron probados en vivo contra los 10 servicios corriendo en Docker (creaciones, casos negativos, `PATCH` de estado de envío, query params de descuento de stock, `PUT`/`DELETE`) — no es solo teoría, los status codes y formas de respuesta documentados acá son los reales.

## Apagar el stack

```bash
docker compose stop catalogo pedidos inventario sucursales carrito clientes envios notificaciones pagos resenas eureka-server
```

(deja `bookpoint-mysql` corriendo, es el contenedor que se usa para pruebas locales sueltas también).
