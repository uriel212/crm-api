# crm-api · API REST del CRM de Ventas

Backend del proyecto final: API REST en **Java 25 + Spring Boot 4** con MySQL 8, Flyway, Spring Security (JWT) y Swagger UI. La consume la app de escritorio `crm-desktop` (JavaFX) en `http://localhost:8080/api`.

## Requisitos

| Herramienta | Versión | Para qué |
|---|---|---|
| JDK | 25 | Gradle lo selecciona solo por *toolchain* (no hace falta instalar Gradle: se usa `./gradlew`). |
| MySQL | 8.x | Base de datos `crm_ventas` (local, en Homebrew o Docker). |
| Docker | cualquiera reciente | Solo para las pruebas (Testcontainers levanta un MySQL temporal). |

## 1. Preparar MySQL

Crea la base vacía una sola vez. Las tablas y los datos de prueba los crea **Flyway** al arrancar (migraciones `V1`–`V10` en `src/main/resources/db/migration/`):

```sql
CREATE DATABASE crm_ventas CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
```

Con Docker, en lugar de un MySQL instalado:

```bash
docker run -d --name crm-mysql -p 3306:3306 \
  -e MYSQL_ALLOW_EMPTY_PASSWORD=yes -e MYSQL_DATABASE=crm_ventas \
  mysql:8.0 --character-set-server=utf8mb4 --collation-server=utf8mb4_0900_ai_ci
```

> Nunca edites una migración ya aplicada: cada cambio de esquema va en una nueva `V<n>__descripcion.sql`. Hibernate solo **valida** el esquema (`ddl-auto=validate`).

## 2. Arrancar la API (perfil `dev`, el de por defecto)

```bash
cd crm-api
./gradlew bootRun
```

Por defecto se conecta a `jdbc:mysql://localhost:3306/crm_ventas` como `root` sin contraseña. Para usar otras credenciales:

```bash
DB_USER=crm DB_PASSWORD=secreto ./gradlew bootRun
```

| Variable | Por defecto (dev) | Uso |
|---|---|---|
| `DB_USER` | `root` | Usuario de MySQL |
| `DB_PASSWORD` | *(vacía)* | Contraseña de MySQL |
| `JWT_SECRET` | secreto local de desarrollo | Firma de los tokens (mínimo 32 caracteres) |

Cuando aparece `Started CrmApiApplication`, la API está en **http://localhost:8080** y el puerto 8080 debe estar libre.

- **Swagger UI:** http://localhost:8080/swagger-ui.html (haz login, copia el `token` y pégalo en **Authorize**).
- **Índice de endpoints:** [`docs/endpoints.md`](docs/endpoints.md).
- **Ejemplos listos para ejecutar:** [`docs/crm-api.http`](docs/crm-api.http) (se ejecuta con el HTTP Client de IntelliJ IDEA).

## 3. Usuarios de prueba

Todos tienen la contraseña **`Password123`**.

| Usuario | Email | Rol | Qué ve |
|---|---|---|---|
| Ana Torres | `ana.torres@crmventas.test` | ADMIN | Todo |
| Luis Ramírez | `luis.ramirez@crmventas.test` | GERENTE | Lo suyo y lo de su equipo (Carla, Jorge, Sofía, Mario) |
| Carla Mendoza | `carla.mendoza@crmventas.test` | VENDEDOR | Lo suyo y lo de sus clientes |
| Jorge Pérez | `jorge.perez@crmventas.test` | VENDEDOR | Lo suyo y lo de sus clientes |
| Sofía Herrera | `sofia.herrera@crmventas.test` | VENDEDOR | Lo suyo y lo de sus clientes |
| Mario Ruiz | `mario.ruiz@crmventas.test` | VENDEDOR (desactivado) | No puede iniciar sesión |

Ejemplo con `curl`:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"carla.mendoza@crmventas.test","password":"Password123"}' | sed -E 's/.*"token":"([^"]+)".*/\1/')
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/reportes/dashboard
```

> ⚠ Las contraseñas del seed son públicas: en un despliegue real hay que cambiarlas.

## 4. Pruebas

```bash
./gradlew test                      # pruebas (necesita Docker encendido)
./gradlew jacocoTestReport          # cobertura en build/reports/jacoco/test/html/index.html
```

- Las pruebas de integración levantan la app completa contra un **MySQL 8 real en Testcontainers**, aplican todas las migraciones y revierten cada prueba, así que los datos del seed no cambian. "Hoy" queda fijo en el 2026-10-20 10:00 (Guatemala).
- Las reglas puras (cálculo de ventas, descuentos, puntos, etapas) tienen pruebas unitarias con JUnit 5.

## 5. Producción (perfil `prod`)

```bash
./gradlew bootJar
DB_URL='jdbc:mysql://HOST:3306/crm_ventas?connectionTimeZone=America/Guatemala' \
DB_USER=crm DB_PASSWORD=... JWT_SECRET='un-secreto-de-al-menos-32-caracteres' PORT=8080 \
java -jar build/libs/crm-api-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

En `prod` todas las credenciales llegan por variables de entorno. Swagger UI sigue disponible, pero los endpoints exigen el token JWT. El proveedor de hosting aún no está decidido (ver `plan_backend.md`, P27).

## 6. Organización del código

```
com.devsuj.crmapi
├── controller/   endpoints HTTP (delgados, sin lógica)
├── service/      reglas de negocio y transacciones (+ estrategias de descuento y de puntos)
├── repository/   Spring Data JPA y consultas agregadas para reportes
├── model/        entidades JPA y enums con sus transiciones de estado
├── dto/          records de entrada y salida, por módulo
├── mapper/       conversión entidad ↔ DTO
├── exception/    excepciones propias + manejador global (ProblemDetail en español)
├── security/     JWT, usuario actual, alcance por rol (ControlAcceso)
├── validation/   validaciones propias (@RangoFechasValido)
└── config/       OpenAPI, seguridad, zona horaria
```

Convenciones y decisiones del proyecto: [`../plan_backend.md`](../plan_backend.md).

- **Zona horaria:** `America/Guatemala`.
- **Moneda:** quetzales, con `BigDecimal` y 2 decimales.
- **Errores:** `ProblemDetail` (RFC 9457) con `detail` en español.
