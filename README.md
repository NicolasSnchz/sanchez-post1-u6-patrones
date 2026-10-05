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

**Patrones aplicados:** Chain of Responsibility para las validaciones y Strategy para el descuento
por tipo de cliente. `GestorPedidos` quedó dividido en cuatro capas y ahora solo orquesta:

| Capa | Clases |
|---|---|
| Validación (Chain of Responsibility) | [`ValidadorPedido`](src/main/java/com/tienda/pedidos/validacion/ValidadorPedido.java), [`ValidadorStock`](src/main/java/com/tienda/pedidos/validacion/ValidadorStock.java), [`ValidadorCliente`](src/main/java/com/tienda/pedidos/validacion/ValidadorCliente.java), [`ContextoPedido`](src/main/java/com/tienda/pedidos/validacion/ContextoPedido.java) |
| Descuento (Strategy) | [`EstrategiaDescuento`](src/main/java/com/tienda/pedidos/descuento/EstrategiaDescuento.java), [`DescuentoVip`](src/main/java/com/tienda/pedidos/descuento/DescuentoVip.java), [`DescuentoFrecuente`](src/main/java/com/tienda/pedidos/descuento/DescuentoFrecuente.java), [`DescuentoEstandar`](src/main/java/com/tienda/pedidos/descuento/DescuentoEstandar.java), [`SelectorEstrategiaDescuento`](src/main/java/com/tienda/pedidos/descuento/SelectorEstrategiaDescuento.java) |
| Persistencia | [`PedidoRepository`](src/main/java/com/tienda/pedidos/service/PedidoRepository.java) (`@Repository`) |
| Notificación | [`NotificacionPedidoService`](src/main/java/com/tienda/pedidos/service/NotificacionPedidoService.java) (`@Service`) |

**Por qué Chain of Responsibility para validar.** Las validaciones tienen un orden real y
necesitan cortar el flujo: si `ValidadorStock` rechaza el pedido, `ValidadorCliente` ni siquiera
debe ejecutarse, porque no tiene sentido consultar la mora de un pedido que no se puede despachar.
En `ValidadorPedido.validar()` el siguiente eslabón solo se llama si el contexto no fue rechazado.
La alternativa que descarté fue un método `validarTodo()` con una lista de
`Predicate<ContextoPedido>`, porque evalúa todos los predicados aunque el primero falle y ningún
validador puede decidir no pasarle el pedido al siguiente.

**Por qué Strategy para el descuento y no un eslabón más.** Las reglas de descuento no dependen de
un orden ni necesitan cortar nada: siempre aplica exactamente una regla según el tipo de cliente.
`SelectorEstrategiaDescuento` cambia el `if/else` original por un `Map`, y agregar un tipo de
cliente nuevo es crear una clase y registrarla sin tocar las que ya existen (abierto/cerrado).
Meterlo en la cadena habría obligado a inventar un mecanismo para que solo un eslabón fijara el
descuento.

**Correcciones al código de referencia de la guía.**
- En la guía, `this.primerValidador = stock.encadenar(cliente)` guarda como primer eslabón lo que
  retorna `encadenar()`, que es `ValidadorCliente`. Con eso la validación de stock nunca se
  ejecutaría y el checkpoint "los cinco pedidos producen la misma salida" fallaría en el caso de
  stock insuficiente. En mi versión el primer eslabón es `ValidadorStock` y `encadenar()` solo se
  usa para enlazar.
- La versión de referencia de `ValidadorStock` no revisaba si el pedido venía sin ítems, cosa que
  el original sí hacía. La dejé dentro de `ValidadorStock` para no cambiar el comportamiento.
- `calcularSubtotal()` no hace SQL dentro de `GestorPedidos`: el precio se lee con
  `PedidoRepository.obtenerPrecioUnitario()`, así el orquestador no depende de `JdbcTemplate`.

**Comparación antes y después (Parte 1).** Mismos pedidos, misma salida. Las expectativas de
[`GestorPedidosTest`](src/test/java/com/tienda/pedidos/GestorPedidosTest.java) se escribieron
contra el código original y no se tocaron al refactorizar.

| # | Pedido | Original | Refactorizado |
|---|---|---|---|
| 1 | Cliente VIP, producto 3 x 10 (stock 5) | Rechazado: `Stock insuficiente: producto 3` | Igual |
| 2 | Cliente 99 (no existe) | Rechazado: `Cliente no registrado` | Igual |
| 3 | Cliente MOROSO a las 10:00 | Rechazado: `Cliente con deuda pendiente: $150000.0` | Igual |
| 4 | Cliente MOROSO a las 21:00, producto 1 x 2 | Confirmado, total 238.000 | Igual |
| 5 | Cliente VIP, producto 1 x 6 (subtotal 600.000, 10%) | Confirmado, total 642.600 | Igual |
| 6 | Cliente FRECUENTE con 5 pedidos previos, producto 2 x 4 (4%) | Confirmado, total 228.480 | Igual |
| 7 | Pedido sin ítems | Rechazado: `El pedido no contiene items` | Igual |

### Parte 2: crecimiento del proyecto

**Antipatrón identificado:** Golden Hammer.

Los enlaces apuntan al commit donde se agregaron las tres campañas como eslabones de la cadena.

Para las campañas BLACK_FRIDAY (25%), CORPORATIVO (10% con NIT) y VOLUMEN (12% con más de 20
unidades) se volvió a usar Chain of Responsibility porque "ya funcionó" en la Parte 1, sin revisar
si el problema nuevo tenía forma de cadena. La evidencia:

- **No hay dependencia de orden.** `GestorPedidos` ([líneas 35-36](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/39ee497e18c8c7c7ea20dc6a74eb9efd75e788cf/src/main/java/com/tienda/pedidos/service/GestorPedidos.java#L35-L36)) encadena
  stock, cliente, blackFriday, corporativo y volumen, pero los tres últimos dan el mismo resultado
  en cualquier orden, porque `aplicarDescuentoCampana()` ([líneas 27-29](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/39ee497e18c8c7c7ea20dc6a74eb9efd75e788cf/src/main/java/com/tienda/pedidos/validacion/ContextoPedido.java#L27-L29)) solo
  se queda con el valor mayor. Justo el orden es lo que sí tienen `ValidadorStock` y
  `ValidadorCliente`, y lo que justificaba la cadena.
- **No hay corte anticipado.** Ninguna de las tres clases llama a `rechazar()`:
  `PromocionBlackFriday` ([líneas 17-23](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/39ee497e18c8c7c7ea20dc6a74eb9efd75e788cf/src/main/java/com/tienda/pedidos/validacion/PromocionBlackFriday.java#L17-L23)), `PromocionCorporativo`
  ([líneas 16-22](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/39ee497e18c8c7c7ea20dc6a74eb9efd75e788cf/src/main/java/com/tienda/pedidos/validacion/PromocionCorporativo.java#L16-L22)) y `PromocionVolumen` ([líneas 11-17](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/39ee497e18c8c7c7ea20dc6a74eb9efd75e788cf/src/main/java/com/tienda/pedidos/validacion/PromocionVolumen.java#L11-L17))
  solo escriben `descuentoCampana`. El corte de `ValidadorPedido.validar()` nunca se usa con ellas.
- **Se rompe el contrato de `ValidadorPedido`.** Esa clase existe para decidir si el pedido sigue o
  se rechaza, y ahora la heredan tres clases que no validan nada. El mismo comentario de
  `PromocionBlackFriday` ([líneas 21-22](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/39ee497e18c8c7c7ea20dc6a74eb9efd75e788cf/src/main/java/com/tienda/pedidos/validacion/PromocionBlackFriday.java#L21-L22)) reconoce que el eslabón solo se
  aprovecha de que la cadena ya existe.
- **Estado mutable compartido.** Se agregó `descuentoCampana` a `ContextoPedido`
  ([línea 13](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/39ee497e18c8c7c7ea20dc6a74eb9efd75e788cf/src/main/java/com/tienda/pedidos/validacion/ContextoPedido.java#L13)) para que clases independientes se comuniquen escribiendo el mismo
  campo, y `GestorPedidos` ([línea 55](https://github.com/NicolasSnchz/sanchez-post1-u6-patrones/blob/39ee497e18c8c7c7ea20dc6a74eb9efd75e788cf/src/main/java/com/tienda/pedidos/service/GestorPedidos.java#L55)) lo combina a mano con el Strategy. Si mañana dos campañas
  tuvieran que sumarse en vez de competir por el máximo, la regla quedaría escondida en un setter
  del contexto y no en un lugar claro de cálculo.
- **El descuento quedó partido en dos mecanismos** para un mismo problema: el de tipo de cliente
  en `EstrategiaDescuento` y el de campañas dentro de la validación, aunque los dos calculan un
  porcentaje con datos del pedido o del cliente.

## Cómo ejecutar
```
mvn test
mvn spring-boot:run
```

## Herramientas utilizadas
- Java 17, Spring Boot 3.3, Spring JDBC, Maven, H2 Database
- VS Code / IntelliJ IDEA, Git, GitHub
