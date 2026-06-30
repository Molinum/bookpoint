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