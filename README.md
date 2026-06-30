# BookPoint - Backend de Microservicios

Plataforma backend distribuida para la gestión de una cadena de librerías con sucursales y bodegas conectadas. 
Proyecto desarrollado para la asignatura de Desarrollo Full Stack (Duoc UC).

Cada microservicio es un proyecto Maven independiente construido sobre **Spring Boot 3.3.4**, **Java 17** y su propio esquema de base de datos en un servidor central **MySQL** (Puerto `3306`). La intercomunicación entre servicios se realiza de forma síncrona mediante peticiones REST.

---

## 🏛️ Ecosistema de Microservicios y Puertos

El sistema está diseñado bajo el patrón *Database-per-Microservice*, donde ningún servicio comparte tablas ni realiza JOINs directos en el motor de base de datos.

| #  | Microservicio | Carpeta en VS Code | Puerto | Base de Datos (MySQL 3306) |
|---|---------------|--------------------|--------|----------------------------|
| 1  | Servidor de Descubrimiento | eureka-server | 8761 | *N/A (En Memoria)* |
| 2  | API Gateway (Enrutador) | gateway | 8080 | *N/A (En Memoria)* |
| 3  | Gestión de Clientes | clientes | 8081 | `bookpoint_usuarios_db` |
| 4  | Catálogo de Libros | catalogo | 8082 | `bookpoint_catalogo_db` |
| 5  | Inventario y Stock | inventario | 8083 | `bookpoint_inventario_db` |
| 6  | Ventas y Pedidos | pedidos | 8084 | `bookpoint_pedidos_db` |
| 7  | Pasarela de Pagos | pagos | 8085 | `bookpoint_pagos_db` |
| 8  | Carrito de Compras | carrito | 8086 | `bookpoint_carrito_db` |
| 9  | Envío y Logística | envios | 8087 | `bookpoint_envios_db` |
| 10 | Reseñas y Calificaciones | resenas | 8088 | `bookpoint_resenas_db` |
| 11 | Gestión de Sucursales | sucursales | 8089 | `bookpoint_sucursales_db` |
| 12 | Notificaciones y Alertas | notificaciones | 8090 | `bookpoint_notificaciones_db` |



# Plan de Pruebas Unitarias - BookPoint (MS-Catalogo)

## Estructura de Pruebas
Las pruebas unitarias fueron automatizadas utilizando **JUnit 5** y **Mockito**, siguiendo el patrón **AAA (Arrange, Act, Assert)** de manera aislada sin tocar la base de datos real.

## Casos de Uso Evaluados
1. **Guardar Libro Exitosamente (Happy Path):** - *Entrada:* LibroDTO con datos válidos.
   - *Resultado esperado:* Retorna la entidad Libro persistida con un ID asignado.
2. **Obtener Libro por ID Existente:**
   - *Entrada:* ID válido (`1L`).
   - *Resultado esperado:* Retorna los datos correctos del libro mapeado.
3. **Control de Excepciones (Edge Case):**
   - *Entrada:* ID inexistente (`99L`).
   - *Resultado esperado:* Lanza `RuntimeException` con el mensaje "Libro no encontrado con id: 99".
