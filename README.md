# VM Manager — Backend

Servicio backend **reactivo** (Spring WebFlux + R2DBC) para gestionar máquinas virtuales: API REST con **JWT en cookie HttpOnly**, roles **ADMIN / CLIENT**, eventos en tiempo real por **WebSocket**, y arquitectura **hexagonal** (puertos y adaptadores).

---

## Qué incluye este proyecto

| Área | Detalle |
|------|---------|
| **API** | `RouterFunction` / `HandlerFunction` (sin controllers anotados) |
| **Persistencia** | PostgreSQL vía **R2DBC** (sin bloqueos) |
| **Seguridad** | Spring Security WebFlux, BCrypt, JWT leído desde cookie |
| **Tiempo real** | WebSocket `/ws/vms` + `Sinks` para broadcast de eventos VM |
| **Calidad** | Pruebas unitarias (JUnit 5, Mockito, Reactor Test), **JaCoCo** con umbral de cobertura en `check` |

---

## Demo rápida

### 1. Levantar servicios

```bash
docker compose up -d --build
```

API: **http://localhost:8080**

*(Si prefieres solo PostgreSQL en Docker y la app en local: levanta `postgres`, crea la BD `vm_manager` y ejecuta `./gradlew bootRun`.)*

### 2. Cuentas de prueba

| Rol | Email | Contraseña |
|-----|-------|-------------|
| **ADMIN** | `admin@test.com` | `123456` |
| **CLIENT** | `client@test.com` | `client123` |

Los usuarios se cargan automáticamente la primera vez que arranca la aplicación contra una base vacía.

### 3. Probar login (cookie `auth-token`)

```bash
curl -s -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -c cookies.txt \
  -d '{"email":"admin@test.com","password":"123456"}'

curl -s http://localhost:8080/vms -b cookies.txt
```

Desde un front en otro origen (p. ej. Angular en `:4200`), usa **`withCredentials: true`** para enviar/recibir cookies.

### 4. WebSocket (eventos VM)

```bash
# Ejemplo con wscat: npm i -g wscat
wscat -c ws://localhost:8080/ws/vms
```

Al crear o modificar VMs verás mensajes JSON con `event` y `data`.

---

## Requisitos

- **JDK 21**
- **Docker** + Docker Compose *(recomendado para demo completa)*
- **PostgreSQL 14+** *(solo si ejecutas la app sin Compose para la BD)*

---

## Cómo ejecutar

### Opción A — Todo con Docker Compose

```bash
docker compose up -d --build
```

Variables útiles están en `docker-compose.yml` (`DB_*`, `JWT_*`, `CORS_ALLOWED_ORIGINS`, etc.). Para HTTPS detrás de proxy, define **`APP_COOKIE_SECURE=true`** en el servicio `app`.

### Opción B — App local + PostgreSQL

1. Crear base de datos:

```sql
CREATE DATABASE vm_manager;
```

2. Variables mínimas (o usar valores por defecto de `application.yml`):

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=vm_manager
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
```

3. Arrancar:

```bash
./gradlew bootRun
```

---

## Tests y cobertura

El proyecto incluye **pruebas unitarias** alineadas con las capas del código:

- **Dominio / aplicación:** casos de uso (`AuthUseCaseImpl`, `VmUseCaseImpl`), validación de DTOs.
- **Infraestructura:** handlers REST, seguridad JWT, adaptadores R2DBC (con **H2** en tests), WebSocket/eventos, configuración y manejo global de errores.

### Comandos

```bash
# Solo tests
./gradlew test

# Informe HTML de cobertura (tras ejecutar tests)
./gradlew jacocoTestReport
# → build/reports/jacoco/test/html/index.html

# Pipeline de verificación del proyecto (tests + umbral JaCoCo)
./gradlew check
```

La tarea **`check`** también ejecuta **`jacocoTestCoverageVerification`**: se exige **≥ 80 %** de líneas cubiertas; si la cobertura baja de ese valor, el build falla.

---

## Variables de entorno

| Variable | Default típico | Uso |
|----------|----------------|-----|
| `DB_HOST`, `DB_PORT`, `DB_NAME` | `localhost`, `5432`, `vm_manager` | Conexión PostgreSQL / R2DBC |
| `DB_USERNAME`, `DB_PASSWORD` | `postgres` | Credenciales BD |
| `JWT_SECRET` | *(ver `application.yml`)* | Firma HS256 del JWT |
| `JWT_EXPIRATION_MS` | `86400000` | TTL del token (24 h) |
| `SERVER_PORT` | `8080` | Puerto HTTP |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000`, `http://localhost:4200` | Orígenes permitidos (*credenciales*: lista explícita, no `*`) |
| `APP_COOKIE_SECURE` | `false` | `true` en producción HTTPS: cookie solo por canal seguro |
| `APP_COOKIE_SAMESITE` | `Strict` | Política SameSite del cookie `auth-token` |

En **HTTP local**, `APP_COOKIE_SECURE=false` evita que el navegador rechace la cookie; en **producción** usa HTTPS y **`APP_COOKIE_SECURE=true`**.

---

## API REST (resumen)

### Autenticación

| Método | Ruta | Rol |
|--------|------|-----|
| `POST` | `/login` | Público |
| `POST` | `/logout` | Público |

**Body login:**

```json
{
  "email": "admin@test.com",
  "password": "123456"
}
```

**Respuesta 200:** `{ "id", "name", "email", "role" }` — **sin JWT en el body**. El token va en la cookie **`auth-token`** (HttpOnly).

### VMs

| Método | Ruta | Roles |
|--------|------|--------|
| `POST` | `/vms` | ADMIN |
| `GET` | `/vms` | ADMIN, CLIENT |
| `GET` | `/vms/{id}` | ADMIN, CLIENT |
| `PUT` | `/vms/{id}` | ADMIN |
| `DELETE` | `/vms/{id}` | ADMIN |

**Crear VM (`POST /vms`):**

```json
{
  "name": "web-server-01",
  "cores": 4,
  "ram": 8,
  "disk": 100,
  "os": "Ubuntu 22.04 LTS"
}
```

El estado inicial es **`STOPPED`**. Para cambiar estado u otros campos, usa **`PUT /vms/{id}`** (incluye `status`: `RUNNING` \| `STOPPED` \| `PAUSED`).

Las respuestas de listado/detalle van envueltas en **`ApiResponse`** (`success`, `message`, `data`, `timestamp`).

---

## Errores HTTP

Las respuestas de error siguen un JSON uniforme (`timestamp`, `status`, `error`, `message`, `path`). Códigos habituales: **401** no autenticado, **403** sin rol, **404** recurso no encontrado, **422** validación.

---

## Arquitectura hexagonal

Las dependencias van **hacia el dominio**. La infraestructura implementa puertos de salida (repositorios, eventos, seguridad); los casos de uso implementan puertos de entrada.

```
domain/           → modelos y contratos (ports)
application/      → DTOs + implementación de casos de uso
infrastructure/   → REST, WebSocket, R2DBC, Security, config, excepciones
```

Árbol principal:

```
src/main/java/com/ifx/vm_manager/
├── domain/model|ports
├── application/dto|usecases
└── infrastructure/adapters/input/rest|websocket
                              └──output/persistence|security
                              config|exceptions
```

---

## Seguridad (JWT en cookie)

1. `POST /login` valida credenciales (BCrypt).
2. Se genera JWT y se envía como **`Set-Cookie`** (`HttpOnly`, `SameSite` configurable, `Secure` según `APP_COOKIE_SECURE`).
3. Las peticiones siguientes envían la cookie; **`JwtSecurityContextRepository`** reconstruye el contexto y los roles (`ROLE_ADMIN`, `ROLE_CLIENT`).

Ventaja frente a `localStorage`: reduce superficie ante XSS en el token; combinar con HTTPS y CORS acotado en producción.

---

## WebSocket

- **URL:** `ws://<host>:8080/ws/vms`
- **Eventos:** `VM_CREATED`, `VM_UPDATED`, `VM_DELETED`, `VM_STATUS_CHANGED`
- Implementación **nativa WebFlux** (`WebSocketHandler` + `Sinks`), acorde a Netty (sin stack Servlet/STOMP).

---

## Estructura del repositorio

```
ifx-virtual-machine-manager-backend/
├── src/main/java/...          # Código de producción
├── src/main/resources/
│   ├── application.yml
│   └── db/schema.sql
├── src/test/java/...          # Pruebas unitarias (por paquete, espejo del código)
├── build.gradle               # Gradle + JaCoCo
├── docker-compose.yml
├── Dockerfile
└── README.md
```

---

## Licencia y uso

Yeison Rua - Proyecto orientado a **demo técnica.** 
