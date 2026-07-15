# Flujo de demo rapido (Swagger)

Swagger UI de cada servicio: `http://localhost:<puerto>/swagger-ui.html`
(ej. catalogo -> http://localhost:8081/swagger-ui.html)

Datos ya sembrados por Datafaker: libro id=1, cliente id=1, sucursal
"Santiago Centro" existen sin necesidad de crearlos.

## Flujo: crear una venta completa

1. **Catalogo** (8081) - `GET /api/v1/catalogo/1` -> confirmar que el libro existe
2. **Inventario** (8082) - `GET /api/v1/inventario/libro/1` -> ver stock disponible por sucursal
3. **Pedidos** (8083) - `POST /api/v1/pedidos`
   ```json
   { "clienteNombre": "Demo ET", "libroId": 1, "sucursal": "Santiago Centro", "cantidad": 2 }
   ```
   -> 201, guarda el `id` devuelto (pedidoId). Total se calcula solo (precio x cantidad).
   Internamente valida via Feign contra catalogo + inventario y descuenta stock.
4. **Inventario** (8082) - `GET /api/v1/inventario/libro/1` de nuevo -> mostrar que el stock bajo
5. **Pagos** (8087) - `POST /api/v1/pagos`
   ```json
   { "pedidoId": <pedidoId del paso 3>, "monto": <total del paso 3>, "metodoPago": "Webpay" }
   ```
   -> 201, estado "APROBADO", valida via Feign que el pedido existe
6. **Envios** (8088) - `POST /api/v1/envios`
   ```json
   { "pedidoId": <pedidoId>, "direccionDestino": "Av. Siempre Viva 742", "comuna": "Santiago Centro", "transportista": "Chilexpress" }
   ```
   -> 201, estado inicial "PREPARACION", genera codigoSeguimiento
7. **Envios** (8088) - `GET /api/v1/envios/{envioId}/historial` -> muestra la relacion JPA real (HistorialEstado)

## Casos negativos (para mostrar manejo de errores)

- `POST /api/v1/pedidos` con `"libroId": 99999` -> 404 (RecursoNoEncontradoException, Feign NotFound)
- `POST /api/v1/pedidos` con `"cantidad": 999999` (mayor al stock) -> 400 "Stock insuficiente"
- `GET /api/v1/catalogo/99999` -> 404

## Segundo flujo corto (entidad standalone, sin dependencias Feign)

- **Sucursales** (8085) - `POST /api/v1/sucursales`
  ```json
  { "nombre": "Sucursal Demo", "direccion": "Calle Falsa 123", "ciudad": "Concepcion" }
  ```
  -> util como ejemplo rapido de CRUD simple si piden crear "algo mas" aparte de la venta.

## Si preguntan por HATEOAS

Cualquier respuesta trae `_links.self`. Un listado (`GET /api/v1/catalogo`) trae
`_embedded` en vez de un arreglo plano -- mostrar la diferencia si preguntan.

## Si preguntan por autenticacion (clientes)

- `POST /api/v1/clientes/login` con `{"email": ..., "password": ...}` de un cliente
  sembrado -> devuelve JWT
- `PUT /api/v1/clientes/{id}` sin token -> 401
- `PUT /api/v1/clientes/{id}` con el token de OTRO cliente -> 403
- `PUT /api/v1/clientes/{id}` con su propio token -> 200
