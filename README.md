# Post-contenido Unidad 6: Antipatrones de Diseño

**Estudiante:** Nicolás Andrés Sánchez Villamizar
**Asignatura:** Patrones de Diseño de Software, Universidad de Santander (UDES)

## Descripción
Este repositorio tiene el post-contenido de la Unidad 6. Es un solo proyecto Spring Boot
(`pedidos-service`, en la raíz del repositorio) con dos partes. En la primera diagnostiqué y
refactoricé el antipatrón combinado que había en `GestorPedidos`. En la segunda el mismo
proyecto creció con tres campañas de descuento, se metió un antipatrón distinto y lo corregí.

## Decisiones de diseño

### Parte 1: GestorPedidos

**Antipatrón identificado:** God Object y Spaghetti Code combinados.

Los enlaces apuntan a `GestorPedidos.java` en su versión original, la del primer commit del proyecto.

**God Object: una sola clase con seis razones para cambiar.**
El único método público, `procesarPedido()` ([líneas 28-133](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/c430e85730a04b1d343f35272cf9e27c4b309a68/src/main/java/com/tienda/pedidos/service/GestorPedidos.java#L28-L133), 106 líneas), hace seis trabajos
distintos y la clase depende al mismo tiempo de `JdbcTemplate` y de `EmailService` ([líneas 21-25](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/c430e85730a04b1d343f35272cf9e27c4b309a68/src/main/java/com/tienda/pedidos/service/GestorPedidos.java#L21-L25)).

| Responsabilidad | Dónde está | Qué lo demuestra |
|---|---|---|
| Validación de stock | [líneas 31-44](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/c430e85730a04b1d343f35272cf9e27c4b309a68/src/main/java/com/tienda/pedidos/service/GestorPedidos.java#L31-L44) | `SELECT stock FROM inventario` dentro de un `for` ([líneas 37-39](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/c430e85730a04b1d343f35272cf9e27c4b309a68/src/main/java/com/tienda/pedidos/service/GestorPedidos.java#L37-L39)) |
| Validación de cliente y mora | [líneas 46-65](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/c430e85730a04b1d343f35272cf9e27c4b309a68/src/main/java/com/tienda/pedidos/service/GestorPedidos.java#L46-L65) | `SELECT tipo_cliente` ([líneas 47-48](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/c430e85730a04b1d343f35272cf9e27c4b309a68/src/main/java/com/tienda/pedidos/service/GestorPedidos.java#L47-L48)) y `SELECT SUM(monto) FROM facturas` ([líneas 53-55](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/c430e85730a04b1d343f35272cf9e27c4b309a68/src/main/java/com/tienda/pedidos/service/GestorPedidos.java#L53-L55)) |
| Cálculo de subtotal | [líneas 67-73](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/c430e85730a04b1d343f35272cf9e27c4b309a68/src/main/java/com/tienda/pedidos/service/GestorPedidos.java#L67-L73) | una consulta `SELECT precio FROM productos` por cada ítem ([líneas 70-71](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/c430e85730a04b1d343f35272cf9e27c4b309a68/src/main/java/com/tienda/pedidos/service/GestorPedidos.java#L70-L71)) |
| Cálculo de descuento e impuesto | [líneas 75-96](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/c430e85730a04b1d343f35272cf9e27c4b309a68/src/main/java/com/tienda/pedidos/service/GestorPedidos.java#L75-L96) | `if/else` por tipo de cliente con SQL metido en la rama FRECUENTE ([líneas 86-87](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/c430e85730a04b1d343f35272cf9e27c4b309a68/src/main/java/com/tienda/pedidos/service/GestorPedidos.java#L86-L87)) |
| Persistencia | [líneas 98-113](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/c430e85730a04b1d343f35272cf9e27c4b309a68/src/main/java/com/tienda/pedidos/service/GestorPedidos.java#L98-L113) | `INSERT INTO pedidos`, `CALL IDENTITY()`, `INSERT INTO detalle_pedido` y `UPDATE inventario`, sin repositorio ni transacción |
| Notificación y registro | [líneas 115-131](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/c430e85730a04b1d343f35272cf9e27c4b309a68/src/main/java/com/tienda/pedidos/service/GestorPedidos.java#L115-L131) | arma el correo con `StringBuilder` ([líneas 116-123](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/c430e85730a04b1d343f35272cf9e27c4b309a68/src/main/java/com/tienda/pedidos/service/GestorPedidos.java#L116-L123)), lo envía ([líneas 124-129](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/c430e85730a04b1d343f35272cf9e27c4b309a68/src/main/java/com/tienda/pedidos/service/GestorPedidos.java#L124-L129)) y escribe el log final ([línea 131](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/c430e85730a04b1d343f35272cf9e27c4b309a68/src/main/java/com/tienda/pedidos/service/GestorPedidos.java#L131)) |

Si cambia el formato del correo, el esquema de la tabla `pedidos`, la regla de mora o los
porcentajes de descuento, hay que tocar el mismo método de la misma clase.

**Spaghetti Code: anidamiento y niveles de abstracción mezclados.**
- La validación de mora llega a **3 niveles de anidamiento**: `else if (tipoCliente.equals("MOROSO"))`
  ([línea 52](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/c430e85730a04b1d343f35272cf9e27c4b309a68/src/main/java/com/tienda/pedidos/service/GestorPedidos.java#L52)), luego `if (deudaPendiente != null && deudaPendiente > 0)` ([línea 56](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/c430e85730a04b1d343f35272cf9e27c4b309a68/src/main/java/com/tienda/pedidos/service/GestorPedidos.java#L56)) y luego
  `if (ahora.isBefore(LocalTime.of(20, 0)))` ([línea 58](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/c430e85730a04b1d343f35272cf9e27c4b309a68/src/main/java/com/tienda/pedidos/service/GestorPedidos.java#L58)). La regla de negocio que depende de la
  hora del sistema quedó enterrada en el tercer nivel.
- El descuento llega a **2 niveles** (`if VIP` en la [línea 77](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/c430e85730a04b1d343f35272cf9e27c4b309a68/src/main/java/com/tienda/pedidos/service/GestorPedidos.java#L77) y `if subtotal > 1_000_000` en la
  [línea 78](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/c430e85730a04b1d343f35272cf9e27c4b309a68/src/main/java/com/tienda/pedidos/service/GestorPedidos.java#L78)) y la rama FRECUENTE mezcla una consulta SQL ([líneas 86-87](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/c430e85730a04b1d343f35272cf9e27c4b309a68/src/main/java/com/tienda/pedidos/service/GestorPedidos.java#L86-L87)) con la regla de negocio.
- El método trabaja en **tres niveles de abstracción a la vez**: SQL embebido, reglas de negocio
  ([líneas 77-96](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/c430e85730a04b1d343f35272cf9e27c4b309a68/src/main/java/com/tienda/pedidos/service/GestorPedidos.java#L77-L96)) y formato de texto del correo ([líneas 116-123](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/c430e85730a04b1d343f35272cf9e27c4b309a68/src/main/java/com/tienda/pedidos/service/GestorPedidos.java#L116-L123)), todo en la misma secuencia de líneas.
- `tipoCliente` se lee en la [línea 47](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/c430e85730a04b1d343f35272cf9e27c4b309a68/src/main/java/com/tienda/pedidos/service/GestorPedidos.java#L47) y se vuelve a usar 30 líneas después ([línea 77](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/c430e85730a04b1d343f35272cf9e27c4b309a68/src/main/java/com/tienda/pedidos/service/GestorPedidos.java#L77) y [línea 85](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/c430e85730a04b1d343f35272cf9e27c4b309a68/src/main/java/com/tienda/pedidos/service/GestorPedidos.java#L85)).
  `subtotal`, `descuento`, `impuesto` y `total` viajan por todo el método hasta la persistencia
  y el correo, así que no se entiende un bloque sin leer los anteriores.
- **Agregar un tipo de cliente nuevo** con su propio descuento obliga a abrir el `if/else` de las
  [líneas 77-93](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/c430e85730a04b1d343f35272cf9e27c4b309a68/src/main/java/com/tienda/pedidos/service/GestorPedidos.java#L77-L93) y meter otra rama `else if`, dentro del mismo método que guarda en la base de datos y
  manda correos.

## Cómo ejecutar
```
mvn test
mvn spring-boot:run
```

## Herramientas utilizadas
- Java 17, Spring Boot 3.3, Spring JDBC, Maven, H2 Database
- VS Code / IntelliJ IDEA, Git, GitHub
