# Índice de endpoints · `crm-api`

- **URL base:** `http://localhost:8080/api`
- **Swagger UI:** `http://localhost:8080/swagger-ui.html` (también en producción). Haz login, copia el `token` y pégalo en **Authorize**.
- **Autenticación:** todos los endpoints, salvo `POST /api/auth/login`, exigen `Authorization: Bearer <token>`.
- **Roles:** `VENDEDOR` (solo lo suyo), `GERENTE` (lo suyo y lo de su equipo), `ADMIN` (todo). *Todos* = los tres roles.
- **Errores:** `ProblemDetail` (RFC 9457) con `detail` en español. Los 400 de validación traen `errores: {"campo": "mensaje"}`. También traen `errores` el 409 por email duplicado (cliente o usuario: `errores.email`) y el 400 por contraseña actual incorrecta (`errores.passwordActual`).
- **Códigos comunes:** `401` sin token o token inválido/expirado; `403` el rol o el alcance no lo permite. No se repiten en cada tabla.
- **Paginación:** `?page=0&size=20&sort=campo,asc` (máximo 100 por página). Respuesta: `{contenido, pagina, tamano, totalElementos, totalPaginas}`.
- **Montos** en quetzales (`BigDecimal` con 2 decimales). **Fechas** en hora de Guatemala (`America/Guatemala`), formato ISO `2026-10-20T10:00:00`.
- Los esquemas completos (campos y ejemplos) están en Swagger; este archivo es solo el índice.

## Autenticación

| Método | Ruta | Descripción | Roles | Códigos |
|---|---|---|---|---|
| POST | `/api/auth/login` | Email + contraseña → `token` JWT, `expiraEn` y datos del usuario | Público | 200, 400, 401 |
| GET | `/api/auth/me` | Usuario dueño del token | Todos | 200 |

## Usuarios

| Método | Ruta | Descripción | Roles | Códigos |
|---|---|---|---|---|
| GET | `/api/usuarios?rol=&activo=&q=&page=&size=` | Lista paginada | GERENTE, ADMIN | 200 |
| GET | `/api/usuarios/{id}` | Detalle (nunca incluye la contraseña) | GERENTE, ADMIN | 200, 404 |
| POST | `/api/usuarios` | Crea usuario (contraseña con BCrypt; `gerenteId` solo para VENDEDOR) | ADMIN | 201, 400, 409 |
| PUT | `/api/usuarios/{id}` | Actualiza nombre, email, rol y gerente | ADMIN | 200, 400, 404, 409 |
| PATCH | `/api/usuarios/{id}/estado` | Activa/desactiva. Con clientes asignados responde 409 con `clientesAsignados` salvo `reasignarClientes: true` (pasan a su gerente) | ADMIN | 200, 404, 409 |
| PATCH | `/api/usuarios/{id}/password` | Cambia contraseña (el propio usuario envía `passwordActual`) | ADMIN o el propio usuario | 204, 400, 404 |

## Clientes

Todos los usuarios **ven** todos los clientes; solo el vendedor asignado, su gerente o un ADMIN los **gestionan** (editar, cambiar estado, registrar oportunidades, ventas, interacciones, contratos y canjes). No se borran: se pasan a `INACTIVO`.

| Método | Ruta | Descripción | Roles | Códigos |
|---|---|---|---|---|
| GET | `/api/clientes?estado=&vendedorId=&q=&page=&size=&sort=` | Búsqueda paginada (`q` en nombre, empresa y email) | Todos | 200, 400 |
| GET | `/api/clientes/{id}` | Detalle | Todos | 200, 404 |
| POST | `/api/clientes` | Crea cliente/lead como `PROSPECTO`. Un VENDEDOR queda como responsable | Todos | 201, 400, 403, 409 |
| PUT | `/api/clientes/{id}` | Actualiza contacto (`vendedorId` null = no cambia; distinto = reasigna, solo GERENTE/ADMIN) | Responsable, su gerente, ADMIN | 200, 400, 403, 404, 409 |
| PATCH | `/api/clientes/{id}/estado` | Cambia estado (con compras no vuelve a `PROSPECTO`) | Responsable, su gerente, ADMIN | 200, 400, 403, 404, 409 |
| PATCH | `/api/clientes/{id}/vendedor` | Asigna o quita responsable (VENDEDOR o GERENTE activo del equipo) | GERENTE, ADMIN | 200, 403, 404, 409 |
| POST | `/api/clientes/reasignaciones` | Reasigna en lote `{clienteIds, vendedorId}` | GERENTE, ADMIN | 200, 400, 403, 404, 409 |

## Interacciones

Llamadas, correos, reuniones y visitas. `REALIZADA` (ya ocurrió; sin fecha = ahora; no puede ser futura) o `PROGRAMADA` (actividad agendada; fecha futura). No se borran: una PROGRAMADA se cierra como `REALIZADA` (con `resultado`) o `CANCELADA`. Se registran en clientes que el usuario gestiona.

| Método | Ruta | Descripción | Roles | Códigos |
|---|---|---|---|---|
| GET | `/api/clientes/{clienteId}/interacciones?tipo=&estado=&page=&size=` | Historial del cliente (más reciente primero) | Todos (por alcance) | 200, 404 |
| POST | `/api/clientes/{clienteId}/interacciones` | Registra una interacción o agenda una actividad | Responsable del cliente, su gerente, ADMIN | 201, 400, 403, 404, 409 |
| GET | `/api/interacciones?usuarioId=&clienteId=&tipo=&estado=&desde=&hasta=` | Búsqueda paginada (fechas inclusive) | Todos (por alcance) | 200, 400 |
| GET | `/api/interacciones/proximas?usuarioId=&dias=7` | Agenda de actividades PROGRAMADA (incluye vencidas) | Todos (por alcance) | 200, 400 |
| GET | `/api/interacciones/{id}` | Detalle (con `vencida`) | Todos (por alcance) | 200, 404 |
| PUT | `/api/interacciones/{id}` | Corrige o reprograma. Una CANCELADA no se edita | Autor, su gerente, ADMIN | 200, 400, 404, 409 |
| PATCH | `/api/interacciones/{id}/estado` | Cierra una PROGRAMADA: `{estado: REALIZADA, resultado}` o `{estado: CANCELADA}` | Autor, su gerente, ADMIN | 200, 400, 404, 409 |

## Oportunidades

Pipeline `PROSPECTO → CALIFICADA → PROPUESTA → NEGOCIACION` (se avanza o retrocede). Se cierra en `PERDIDA` (manual) o `GANADA` (**solo** al registrar la venta con `oportunidadId`). Las cerradas no cambian: una PERDIDA se retoma creando otra con `oportunidadAnteriorId`. Cada paso queda en el historial.

| Método | Ruta | Descripción | Roles | Códigos |
|---|---|---|---|---|
| GET | `/api/oportunidades?etapa=&usuarioId=&clienteId=&abiertas=&page=&size=&sort=` | Lista paginada | Todos (por alcance) | 200 |
| GET | `/api/clientes/{clienteId}/oportunidades` | Oportunidades de un cliente | Todos (por alcance) | 200 |
| GET | `/api/oportunidades/{id}` | Detalle | Todos (por alcance) | 200, 404 |
| GET | `/api/oportunidades/{id}/historial` | Línea de tiempo: etapa, fechas, `diasEnEtapa`, usuario, comentario, `actual` | Todos (por alcance) | 200, 404 |
| POST | `/api/oportunidades` | Crea en PROSPECTO (cliente no INACTIVO) | Responsable del cliente, su gerente, ADMIN | 201, 400, 403, 404, 409 |
| PUT | `/api/oportunidades/{id}` | Edita título, monto y fecha estimada (solo abiertas) | Dueño, su gerente, ADMIN | 200, 400, 404, 409 |
| PATCH | `/api/oportunidades/{id}/etapa` | `{etapa, comentario}`. GANADA a mano → 409 | Dueño, su gerente, ADMIN | 200, 400, 404, 409 |

## Categorías

| Método | Ruta | Descripción | Roles | Códigos |
|---|---|---|---|---|
| GET | `/api/categorias` | Lista completa ordenada por nombre (sin paginar) | Todos | 200 |
| GET | `/api/categorias/{id}` | Detalle | Todos | 200, 404 |
| POST | `/api/categorias` | Crea (nombre único) | GERENTE, ADMIN | 201, 400, 409 |
| PUT | `/api/categorias/{id}` | Actualiza | GERENTE, ADMIN | 200, 400, 404, 409 |
| DELETE | `/api/categorias/{id}` | Borra si no tiene productos | ADMIN | 204, 404, 409 |

## Productos e inventario

El stock solo cambia con movimientos de inventario (kardex): `ENTRADA`/`SALIDA` manuales, `VENTA` y `CANJE` automáticos. Los productos no se borran: se desactivan.

| Método | Ruta | Descripción | Roles | Códigos |
|---|---|---|---|---|
| GET | `/api/productos?categoriaId=&activo=&canjeables=&q=&page=&size=` | Catálogo paginado (`stockBajo`, `canjeable`) | Todos | 200 |
| GET | `/api/productos/stock-bajo` | Activos con stock ≤ mínimo | GERENTE, ADMIN | 200 |
| GET | `/api/productos/{id}` | Detalle | Todos | 200, 404 |
| POST | `/api/productos` | Crea (`stockInicial` queda como ENTRADA) | GERENTE, ADMIN | 201, 400, 404 |
| PUT | `/api/productos/{id}` | Actualiza (no toca ventas pasadas ni el stock) | GERENTE, ADMIN | 200, 400, 404 |
| PATCH | `/api/productos/{id}/estado` | Activa/desactiva `{activo}` | GERENTE, ADMIN | 200, 404 |
| POST | `/api/productos/{id}/movimientos` | Ajuste manual `{tipo: ENTRADA/SALIDA, cantidad, motivo}` | GERENTE, ADMIN | 201, 400, 404, 409 |
| GET | `/api/productos/{id}/movimientos` | Kardex (más reciente primero) | GERENTE, ADMIN | 200, 404 |

## Ventas · ✅ Publicado

Una venta **solo existe si se concretó** (pagada): no tiene estados, no se edita ni se cancela. **El cliente de la API nunca envía precios ni descuentos.**

**Cálculo** (lo hace el servidor con las promociones vigentes hoy):
1. Por renglón: `precioUnitario` = precio actual del producto (queda congelado). Se aplica la mejor promoción de alcance PRODUCTO que incluya al producto (`PORCENTAJE` = % del renglón; `MONTO_FIJO` = quetzales **por unidad**). `subtotal = cantidad × precioUnitario − descuento`.
2. Venta: `subtotal` = suma de renglones ya descontados. Se aplica la mejor promoción de alcance VENTA cuyo `montoMinimo` se alcance (`subtotal ≥ montoMinimo`; `MONTO_FIJO` = quetzales una sola vez). `total = subtotal − descuento`.
3. "Mejor" = mayor descuento; en empate, la de id menor. El descuento nunca supera el importe. Una promoción ligada a una campaña solo aplica con la campaña ACTIVA.

**Al registrar** (todo en una transacción): descuenta stock (kardex `VENTA`; 409 si no alcanza), la oportunidad ligada pasa a `GANADA` (queda en su historial), un cliente `PROSPECTO` pasa a `ACTIVO` y el cliente gana **1 punto por cada Q100** completos del total (`puntosGanados`).

| Método | Ruta | Descripción | Roles | Códigos |
|---|---|---|---|---|
| GET | `/api/ventas?clienteId=&usuarioId=&desde=&hasta=&page=&size=&sort=` | Lista paginada sin renglones (`fecha` desc por defecto; fechas inclusive) | Todos (por alcance) | 200, 400 |
| GET | `/api/clientes/{clienteId}/ventas` | Historial de compras del cliente | Todos (por alcance) | 200, 404 |
| GET | `/api/ventas/{id}` | Venta con renglones, promociones y `puntosGanados` | Todos (por alcance) | 200, 404 |
| POST | `/api/ventas/calculo` | Cotiza `{detalles}` sin guardar (acepta el mismo cuerpo que POST `/api/ventas`) | Todos | 200, 400, 404, 409 |
| POST | `/api/ventas` | Registra `{clienteId, oportunidadId?, usuarioId?, detalles: [{productoId, cantidad}]}` | Responsable del cliente, su gerente, ADMIN | 201, 400, 403, 404, 409 |

- `usuarioId` (responsable) solo lo pueden indicar GERENTE/ADMIN y debe estar en su alcance; por defecto, quien registra.
- 400: sin renglones, cantidad ≤ 0, producto repetido. 409: cliente INACTIVO, producto desactivado, stock insuficiente, oportunidad cerrada o de otro cliente.
- Respuesta de `POST /api/ventas` y `GET /api/ventas/{id}`: `{id, clienteId, clienteNombre, usuarioId, usuarioNombre, oportunidadId, fecha, subtotal, descuento, promocionId, promocionNombre, total, puntosGanados, detalles: [{productoId, productoNombre, cantidad, precioUnitario, descuento, subtotal, promocionId, promocionNombre}]}`.
- Respuesta de `POST /api/ventas/calculo`: `{detalles: [...], subtotal, descuento, promocionId, promocionNombre, total}`.
- Listado: `{id, clienteId, clienteNombre, usuarioId, usuarioNombre, oportunidadId, fecha, subtotal, descuento, total}`.

## Metas de venta · ✅ Publicado

Una meta mensual por VENDEDOR activo (solo los VENDEDOR tienen metas). El avance suma el total de **todas** las ventas del vendedor en ese mes.

| Método | Ruta | Descripción | Roles | Códigos |
|---|---|---|---|---|
| GET | `/api/metas?usuarioId=&anio=&mes=&page=&size=` | Lista paginada con avance (año/mes desc) | Todos (VENDEDOR las suyas, GERENTE su equipo) | 200 |
| GET | `/api/metas/{id}` | Detalle con avance | Todos (por alcance) | 200, 404 |
| POST | `/api/metas` | Crea `{usuarioId, anio, mes, montoObjetivo}` | GERENTE (su equipo), ADMIN | 201, 400, 404, 409 |
| PUT | `/api/metas/{id}` | Edita | GERENTE (su equipo), ADMIN | 200, 400, 404, 409 |
| DELETE | `/api/metas/{id}` | Elimina | GERENTE (su equipo), ADMIN | 204, 404 |

- Respuesta: `{id, usuarioId, usuarioNombre, anio, mes, montoObjetivo, montoVendido, numeroVentas, porcentajeAvance, faltante, cumplida}`. `porcentajeAvance` puede pasar de 100.
- 409: ya hay meta para ese vendedor y mes, o el usuario no es VENDEDOR activo.

## Reportes y KPIs · ✅ Publicado

Solo lectura, para el tablero y los informes (JavaFX Charts). Todo se filtra por alcance: VENDEDOR lo suyo, GERENTE su equipo, ADMIN todo. **Ventas y oportunidades cuentan por su responsable** (`usuario`); clientes por su vendedor asignado. `desde`/`hasta` son días completos (ambos incluidos). Por defecto: el tablero usa el **mes en curso**; los demás, del **1 de enero del año en curso a hoy**.

| Método | Ruta | Descripción | Roles | Códigos |
|---|---|---|---|---|
| GET | `/api/reportes/dashboard?desde=&hasta=` | KPIs (ver abajo) | Todos | 200, 400 |
| GET | `/api/reportes/ventas/por-mes?anio=` | 12 renglones `{anio, mes, numeroVentas, total}` (0 en meses sin ventas) | Todos | 200, 400 |
| GET | `/api/reportes/ventas/por-vendedor?desde=&hasta=` | `{usuarioId, usuarioNombre, numeroVentas, total, ticketPromedio}` de mayor a menor | Todos | 200, 400 |
| GET | `/api/reportes/ventas/por-categoria?desde=&hasta=` | `{categoriaId, categoriaNombre, unidades, total}` (suma de renglones, antes del descuento sobre el total) | Todos | 200, 400 |
| GET | `/api/reportes/productos/mas-vendidos?desde=&hasta=&limite=10` | `{productoId, productoNombre, unidades, total}` por unidades (`limite` 1–50) | Todos | 200, 400 |
| GET | `/api/reportes/oportunidades/pipeline` | Situación actual: las 6 etapas en orden `{etapa, cantidad, montoEstimado}` | Todos | 200 |
| GET | `/api/reportes/oportunidades/conversion?desde=&hasta=` | `{desde, hasta, ganadas, perdidas, tasaConversion, abiertas}` por fecha de cierre | Todos | 200, 400 |
| GET | `/api/reportes/rendimiento-vendedores?anio=&mes=` | Por VENDEDOR del mes (por defecto el actual): `{usuarioId, usuarioNombre, numeroVentas, totalVendido, metaObjetivo, avanceMeta, oportunidadesGanadas, oportunidadesPerdidas, tasaConversion}` de mayor a menor | Todos | 200, 400 |

- Tablero: `{desde, hasta, totalVendido, numeroVentas, ticketPromedio, metaMes, vendidoMes, avanceMetaMes, clientesActivos, clientesProspecto, clientesNuevos, oportunidadesAbiertas, montoPipeline, oportunidadesGanadas, oportunidadesPerdidas, tasaConversion, ticketsAbiertos, productosStockBajo}`. `metaMes`/`vendidoMes`/`avanceMetaMes` son siempre del mes en curso; el resto, del periodo.
- Porcentajes con 2 decimales (`71.43` = 71.43 %). `metaObjetivo` y `avanceMeta` son `null` si el vendedor no tiene meta ese mes.

### Reportes de campañas, soporte, fidelización y vista 360 · ✅ Publicado

| Método | Ruta | Descripción | Roles | Códigos |
|---|---|---|---|---|
| GET | `/api/reportes/campanas/efectividad` | Métricas de todas las campañas (mismo formato que `/api/campanas/{id}/metricas`), más reciente primero | GERENTE, ADMIN | 200 |
| GET | `/api/reportes/soporte` | `{abiertos, enProceso, cerrados, sinAsignar, horasPromedioCierre, pendientesPorPrioridad: [{prioridad, cantidad}]}` (BAJA, MEDIA, ALTA) | Todos (por alcance) | 200 |
| GET | `/api/reportes/fidelizacion` | `{puntosEmitidos, puntosCanjeados, saldoEnCirculacion, clientesConSaldo, topClientes: [{clienteId, clienteNombre, saldo}]}` (top 5) | Todos (clientes del alcance) | 200 |
| GET | `/api/clientes/{id}/resumen` | Vista 360 (ver abajo) | Vendedor del cliente, su gerente, ADMIN | 200, 403, 404 |

- Vista 360: `{cliente: {...ClienteResponse}, numeroCompras, totalComprado, ticketPromedio, ultimaCompra, ultimasVentas: [5 × VentaResumen], oportunidadesAbiertas, montoPipeline, ultimaInteraccion, actividadesProgramadas, saldoPuntos, contratosVigentes, ticketsAbiertos, campanas}`. `ultimaCompra`/`ultimaInteraccion` son `null` si no hay; `horasPromedioCierre` es `null` si no hay tickets cerrados.

## Fidelización · ✅ Publicado

1 punto por cada Q100 completos del total de cada venta (se otorgan solos al registrarla). Saldo = ganados − canjeados. Los puntos se canjean por productos con `precioPuntos` (`canjeable: true` en el catálogo); el canje descuenta stock (kardex `CANJE`).

| Método | Ruta | Descripción | Roles | Códigos |
|---|---|---|---|---|
| GET | `/api/clientes/{clienteId}/puntos` | `{clienteId, clienteNombre, puntosGanados, puntosCanjeados, saldo}` | Todos | 200, 404 |
| GET | `/api/clientes/{clienteId}/puntos/movimientos?page=&size=` | Estado de cuenta paginado (más reciente primero) | Todos | 200, 404 |
| POST | `/api/clientes/{clienteId}/puntos/canjes` | Canjea `{productoId, cantidad}` | Responsable del cliente, su gerente, ADMIN | 201, 400, 403, 404, 409 |

- Movimiento: `{id, tipo: GANADO/CANJEADO, puntos, motivo, fecha, ventaId, productoId, productoNombre, cantidad, usuarioId, usuarioNombre}` (`ventaId` solo en GANADO; producto y cantidad solo en CANJEADO). Los puntos siempre son positivos.
- Respuesta del canje (201): `{movimiento: {...}, saldo}` con el saldo que le queda al cliente.
- 409: cliente INACTIVO, producto desactivado o sin precio en puntos, stock insuficiente, o puntos insuficientes (el ProblemDetail trae `saldo` y `puntosNecesarios`).

## Campañas · ✅ Publicado

Campañas dirigidas a un segmento. **No se envían mensajes reales**: se registran los destinatarios y su resultado `ENVIADO → ABIERTO → RESPONDIO` (solo avanza). No se borran: se cancelan. Estados `PLANEADA`, `ACTIVA`, `FINALIZADA`, `CANCELADA` (cambio manual libre para corregir errores; FINALIZADA/CANCELADA no se editan ni generan destinatarios).

Segmentos: `TODOS` (clientes no INACTIVO), `PROSPECTOS`, `ACTIVOS`, `INACTIVOS`, `SIN_COMPRA_90_DIAS` (ya compraron y su última compra fue hace más de 90 días).

| Método | Ruta | Descripción | Roles | Códigos |
|---|---|---|---|---|
| GET | `/api/campanas?estado=&canal=&q=&page=&size=` | Lista paginada (`fechaInicio` desc) con `totalDestinatarios` | Todos | 200 |
| GET | `/api/campanas/{id}` | Detalle | Todos | 200, 404 |
| POST | `/api/campanas` | Crea (nace PLANEADA) | GERENTE, ADMIN | 201, 400 |
| PUT | `/api/campanas/{id}` | Edita (no FINALIZADA/CANCELADA) | GERENTE, ADMIN | 200, 400, 404, 409 |
| PATCH | `/api/campanas/{id}/estado` | `{estado}` | GERENTE, ADMIN | 200, 400, 404, 409 |
| GET | `/api/campanas/{id}/destinatarios?resultado=&page=&size=` | Destinatarios (por nombre) | Todos | 200, 404 |
| POST | `/api/campanas/{id}/destinatarios/generar` | Agrega los del segmento que faltan (idempotente) → `{agregados, totalDestinatarios}` | GERENTE, ADMIN | 200, 404, 409 |
| POST | `/api/campanas/{id}/destinatarios` | Agrega uno `{clienteId}` | GERENTE, ADMIN | 201, 400, 404, 409 |
| DELETE | `/api/campanas/{id}/destinatarios/{clienteId}` | Quita uno (solo PLANEADA) | GERENTE, ADMIN | 204, 404, 409 |
| PATCH | `/api/campanas/{id}/destinatarios/{clienteId}/resultado` | `{resultado}` (solo avanza; campaña ACTIVA o FINALIZADA) | Vendedor del cliente, su gerente, ADMIN | 200, 400, 403, 404, 409 |
| GET | `/api/campanas/{id}/metricas` | Efectividad | GERENTE, ADMIN | 200, 404 |

- Campaña: `{id, nombre, canal: CORREO/SMS/REDES, asunto, mensaje, segmento, fechaInicio, fechaFin, presupuesto, estado, totalDestinatarios}`. El request es igual sin `id`, `estado` ni `totalDestinatarios`.
- Destinatario: `{campanaId, clienteId, clienteNombre, clienteEmail, vendedorId, vendedorNombre, resultado}`.
- Métricas: `{campanaId, campanaNombre, estado, destinatarios, abiertos (ABIERTO + RESPONDIO), respondieron, tasaApertura, tasaRespuesta, presupuesto, costoPorRespuesta (null si nadie respondió)}`.

## Promociones · ✅ Publicado

Las aplica **solo el servidor** al vender (ver *Ventas*). `PORCENTAJE` (≤ 100) o `MONTO_FIJO`; alcance `PRODUCTO` (lleva `productoIds`; monto fijo por unidad) o `VENTA` (sin productos; `montoMinimo` opcional; monto fijo una vez). Con `campanaId`, solo aplica mientras la campaña está ACTIVA. Editar no cambia ventas pasadas.

| Método | Ruta | Descripción | Roles | Códigos |
|---|---|---|---|---|
| GET | `/api/promociones?activa=&alcance=&campanaId=&productoId=&page=&size=` | Lista paginada | Todos | 200 |
| GET | `/api/promociones/vigentes` | Las que aplicarían hoy (lista, por id) | Todos | 200 |
| GET | `/api/promociones/{id}` | Detalle | Todos | 200, 404 |
| POST | `/api/promociones` | Crea | GERENTE, ADMIN | 201, 400, 404 |
| PUT | `/api/promociones/{id}` | Edita | GERENTE, ADMIN | 200, 400, 404 |
| PATCH | `/api/promociones/{id}/estado` | `{activa}` | GERENTE, ADMIN | 200, 404 |
| DELETE | `/api/promociones/{id}` | Borra si nunca se aplicó en una venta (si no, desactivar) | ADMIN | 204, 404, 409 |

- Request: `{campanaId?, nombre, tipo, alcance, valor, montoMinimo?, fechaInicio, fechaFin, productoIds}`. 400 si porcentaje > 100, PRODUCTO sin productos, VENTA con productos, `montoMinimo` en PRODUCTO o fechas al revés.
- Respuesta: `{id, campanaId, campanaNombre, nombre, tipo, alcance, valor, montoMinimo, fechaInicio, fechaFin, activa, vigente, productos: [{id, nombre}]}`.

## Contratos · ✅ Publicado

`BORRADOR → VIGENTE | CANCELADO`; `VIGENTE → VENCIDO | CANCELADO` (VENCIDO solo si `fechaFin` ya pasó). Solo se editan en BORRADOR; un VIGENTE se **extiende**; la **renovación** crea un contrato nuevo en BORRADOR ligado (`contratoAnteriorId`, una vez por contrato, desde VIGENTE o VENCIDO). No se borran. Se ven según el vendedor asignado al cliente; los gestiona el vendedor del cliente, su gerente o ADMIN.

| Método | Ruta | Descripción | Roles | Códigos |
|---|---|---|---|---|
| GET | `/api/contratos?clienteId=&estado=&porVencerDias=&page=&size=` | Lista (`fechaFin` asc). `porVencerDias` = VIGENTE que vencen entre hoy y hoy + N (0–365) | Todos (por alcance) | 200, 400 |
| GET | `/api/clientes/{clienteId}/contratos` | Contratos del cliente | Todos (por alcance) | 200 |
| GET | `/api/contratos/{id}` | Detalle | Todos (por alcance) | 200, 404 |
| POST | `/api/contratos` | Crea en BORRADOR `{clienteId, ventaId?, titulo, fechaInicio, fechaFin, monto}` | Gestor del cliente | 201, 400, 403, 404, 409 |
| PUT | `/api/contratos/{id}` | Edita (solo BORRADOR; el cliente no cambia) | Gestor del cliente | 200, 400, 404, 409 |
| PATCH | `/api/contratos/{id}/estado` | `{estado}` | Gestor del cliente | 200, 400, 404, 409 |
| PATCH | `/api/contratos/{id}/extension` | `{fechaFin}` posterior a la actual (solo VIGENTE) | Gestor del cliente | 200, 400, 404, 409 |
| POST | `/api/contratos/{id}/renovacion` | `{titulo?, fechaInicio, fechaFin, monto}` → contrato nuevo | Gestor del cliente | 201, 400, 404, 409 |
| POST | `/api/contratos/vencimientos` | Marca VENCIDO los VIGENTE con `fechaFin` pasada → `{vencidos, contratoIds}` | GERENTE, ADMIN | 200 |

- Respuesta: `{id, clienteId, clienteNombre, ventaId, contratoAnteriorId, titulo, fechaInicio, fechaFin, monto, estado, diasParaVencer, clienteVendedorId}` (`diasParaVencer` solo en VIGENTE; negativo si ya pasó la fecha). `clienteVendedorId` = vendedor asignado al cliente (null si no tiene), para saber de antemano quién puede gestionarlo.
- 409: cliente INACTIVO, venta de otro cliente, transición no permitida, ya renovado.

## Tickets de soporte · ✅ Publicado

`ABIERTO ↔ EN_PROCESO → CERRADO` (también `ABIERTO → CERRADO`). EN_PROCESO exige responsable. Un CERRADO no se edita ni se reabre: se crea un **seguimiento** (ticket nuevo ligado con `ticketRelacionadoId`). Se ven por responsable o por el vendedor del cliente; los gestiona el responsable (o su gerente/ADMIN) o quien gestiona al cliente.

| Método | Ruta | Descripción | Roles | Códigos |
|---|---|---|---|---|
| GET | `/api/tickets?estado=&prioridad=&clienteId=&usuarioId=&sinAsignar=&page=&size=` | Lista (`creadoEn` desc) | Todos (por alcance) | 200 |
| GET | `/api/clientes/{clienteId}/tickets` | Tickets del cliente | Todos (por alcance) | 200 |
| GET | `/api/tickets/{id}` | Detalle | Todos (por alcance) | 200, 404 |
| POST | `/api/tickets` | Abre `{clienteId, asunto, descripcion?, prioridad, usuarioId?}` (null = sin asignar; un VENDEDOR solo se asigna a sí mismo) | Gestor del cliente | 201, 400, 403, 404, 409 |
| PUT | `/api/tickets/{id}` | `{asunto, descripcion?, prioridad}` (no CERRADO) | Responsable o gestor del cliente | 200, 400, 404, 409 |
| PATCH | `/api/tickets/{id}/asignacion` | `{usuarioId}` o `{usuarioId: null}` | GERENTE, ADMIN | 200, 404, 409 |
| PATCH | `/api/tickets/{id}/estado` | `{estado}`; CERRADO registra `cerradoEn` | Responsable o gestor del cliente | 200, 400, 404, 409 |
| POST | `/api/tickets/{id}/seguimiento` | `{asunto, descripcion?, prioridad}` sobre un CERRADO → ticket nuevo ABIERTO | Responsable o gestor del cliente | 201, 400, 404, 409 |

- Respuesta: `{id, clienteId, clienteNombre, usuarioId, usuarioNombre, asunto, descripcion, prioridad: BAJA/MEDIA/ALTA, estado, creadoEn, cerradoEn, ticketRelacionadoId, clienteVendedorId}` (`clienteVendedorId` = vendedor asignado al cliente, null si no tiene).
