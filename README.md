# VM Manager Backend

API RESTful reactiva para gestión de máquinas virtuales con autenticación JWT (HttpOnly Cookie) y actualizaciones en tiempo real vía WebSocket.

## Stack Tecnológico

| Tecnología          | Versión  | Rol                                  |
|---------------------|----------|--------------------------------------|
| Java                | 21       | Lenguaje                             |
| Spring Boot         | 4.0.6    | Framework principal                  |
| Spring WebFlux      | -        | API reactiva (RouterFunction)        |
| Spring Security     | -        | Seguridad reactiva con JWT Cookie    |
| Spring Data R2DBC   | -        | Acceso reactivo a base de datos      |
| PostgreSQL          | 16       | Base de datos relacional             |
| JJWT                | 0.12.6   | Generación y validación de JWT       |
| Lombok              | -        | Reducción de boilerplate             |
| Gradle              | -        | Build tool                           |

---

## Arquitectura Hexagonal

```
src/main/java/com/ifx/vm_manager/
│
├── domain/                          ← Núcleo puro. Sin dependencias de framework.
│   ├── model/                       ← Entidades y enums de dominio
│   │   ├── User.java
│   │   ├── VirtualMachine.java
│   │   ├── Role.java
│   │   ├── VmStatus.java
│   │   └── VmEventType.java
│   └── ports/
│       ├── input/                   ← Contratos que el dominio expone (driven by infrastructure)
│       │   ├── AuthUseCase.java
│       │   └── VmUseCase.java
│       └── output/                  ← Contratos que el dominio necesita (implemented by infrastructure)
│           ├── UserRepositoryPort.java
│           ├── VmRepositoryPort.java
│           └── VmEventPort.java
│
├── application/                     ← Casos de uso. Orquesta dominio + puertos.
│   ├── dto/
│   │   ├── request/                 ← DTOs de entrada (validados)
│   │   └── response/                ← DTOs de salida (inmutables)
│   └── usecases/                    ← Implementaciones de puertos de entrada
│       ├── AuthUseCaseImpl.java
│       └── VmUseCaseImpl.java
│
└── infrastructure/                  ← Detalles técnicos. Adapta el mundo externo al dominio.
    ├── adapters/
    │   ├── input/
    │   │   ├── rest/                ← RouterFunction + HandlerFunction (sin @Controller)
    │   │   └── websocket/           ← WebSocketHandler reactivo con Sinks
    │   └── output/
    │       ├── persistence/         ← R2DBC entities, repositories, mappers, adapters
    │       └── security/            ← JwtService, SecurityContextRepository
    ├── config/                      ← SecurityConfig, WebFluxConfig, WebSocketConfig, R2dbc
    └── exceptions/                  ← GlobalExceptionHandler (WebExceptionHandler @Order(-2))
```

### Flujo de Dependencias

```
REST/WS Handler → UseCase (port) → Repository Port (output)
                                  → EventPort (output)
                    ↑ implemented by ↓
             UseCaseImpl          RepositoryAdapter / VmEventPublisher
```

La regla fundamental: **las flechas de dependencia siempre apuntan hacia adentro** (hacia el dominio).

---

## Seguridad - Flujo JWT HttpOnly

```
1. POST /login {email, password}
   ↓
2. AuthHandler extrae y valida el body
   ↓
3. AuthUseCase.authenticate() verifica credenciales con BCrypt
   ↓
4. JwtService.generateToken() genera el JWT (HS256)
   ↓
5. La respuesta incluye Set-Cookie: auth-token=<jwt>; HttpOnly; Secure; SameSite=Strict
   ↓
6. El cuerpo de la respuesta SOLO contiene {id, name, email, role} — sin token

7. Cada request posterior incluye el cookie automáticamente (browser)
   ↓
8. JwtSecurityContextRepository.load() lee el cookie, valida el JWT,
   construye el SecurityContext con roles
   ↓
9. Spring Security aplica las reglas de autorización por ruta
```

**Por qué HttpOnly Cookie y no localStorage:**
- `HttpOnly` impide acceso desde JavaScript → protege contra XSS
- `Secure` garantiza transmisión solo por HTTPS
- `SameSite=Strict` protege contra CSRF

---

## WebSocket - Actualización en Tiempo Real

**Tecnología:** Spring WebFlux WebSocket nativo con `Sinks.Many<String>`.

```
VM creada/actualizada/eliminada
   ↓
VmUseCaseImpl → VmEventPort.publishVmEvent()
   ↓ (implementado por)
VmEventPublisher.sink.tryEmitNext(json)
   ↓
Todos los clientes WebSocket conectados reciben el evento
```

**Endpoint WebSocket:**
```
ws://localhost:8080/ws/vms
```

**Formato de mensaje:**
```json
{
  "event": "VM_UPDATED",
  "data": {
    "id": 1,
    "name": "my-vm",
    "status": "RUNNING"
  }
}
```

**Eventos posibles:** `VM_CREATED`, `VM_UPDATED`, `VM_DELETED`, `VM_STATUS_CHANGED`

**Ejemplo de conexión desde JavaScript:**
```javascript
const ws = new WebSocket('ws://localhost:8080/ws/vms');
ws.onmessage = (event) => {
  const payload = JSON.parse(event.data);
  console.log(payload.event, payload.data);
};
```

> **Nota arquitectónica:** Se usa WebFlux nativo en lugar de STOMP/SockJS porque STOMP requiere servidor Servlet (Tomcat). En WebFlux con Netty, la solución reactiva correcta es `WebSocketHandler` + `Sinks`.

---

## Prerrequisitos

- Java 21+
- Docker y Docker Compose (opcional pero recomendado)
- PostgreSQL 14+ (si no se usa Docker)

---

## Ejecución

### Opción 1: Docker Compose (recomendado)

```bash
docker-compose up -d
```

La API estará disponible en `http://localhost:8080`.

### Opción 2: Local con PostgreSQL externo

1. Crear base de datos:
```sql
CREATE DATABASE vm_manager;
```

2. Configurar variables de entorno:
```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=vm_manager
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export JWT_SECRET=vm-manager-super-secret-key-256bits-minimum-length-required-for-hs256
```

3. Ejecutar:
```bash
./gradlew bootRun
```

---

## Variables de Entorno

| Variable              | Default                                            | Descripción                    |
|-----------------------|----------------------------------------------------|--------------------------------|
| `DB_HOST`             | `localhost`                                        | Host de PostgreSQL             |
| `DB_PORT`             | `5432`                                             | Puerto de PostgreSQL           |
| `DB_NAME`             | `vm_manager`                                       | Nombre de la base de datos     |
| `DB_USERNAME`         | `postgres`                                         | Usuario de PostgreSQL          |
| `DB_PASSWORD`         | `postgres`                                         | Contraseña de PostgreSQL       |
| `JWT_SECRET`          | *(ver application.yml)*                            | Clave secreta JWT (≥32 chars)  |
| `JWT_EXPIRATION_MS`   | `86400000`                                         | Expiración JWT en ms (24h)     |
| `SERVER_PORT`         | `8080`                                             | Puerto del servidor            |
| `CORS_ALLOWED_ORIGINS`| `http://localhost:3000,http://localhost:4200`       | Orígenes CORS permitidos       |

---

## API Endpoints

### Autenticación

| Método | Ruta      | Auth | Descripción          |
|--------|-----------|------|----------------------|
| POST   | `/login`  | No   | Iniciar sesión       |
| POST   | `/logout` | No   | Cerrar sesión        |

**Login Request:**
```json
{
  "email": "admin@test.com",
  "password": "123456"
}
```

**Login Response (HTTP 200):**
```json
{
  "id": 1,
  "name": "Admin User",
  "email": "admin@test.com",
  "role": "ADMIN"
}
```
El JWT se establece automáticamente en la cookie `auth-token`.

---

### Máquinas Virtuales

| Método | Ruta         | Roles         | Descripción             |
|--------|--------------|---------------|-------------------------|
| POST   | `/vms`       | ADMIN         | Crear VM                |
| GET    | `/vms`       | ADMIN, CLIENT | Listar todas las VMs    |
| GET    | `/vms/{id}`  | ADMIN, CLIENT | Obtener VM por ID       |
| PUT    | `/vms/{id}`  | ADMIN         | Actualizar VM           |
| DELETE | `/vms/{id}`  | ADMIN         | Eliminar VM             |

**Create/Update VM Request:**
```json
{
  "name": "web-server-01",
  "cores": 4,
  "ram": 8192,
  "disk": 100,
  "os": "Ubuntu 22.04 LTS",
  "status": "RUNNING"
}
```

**VM Response:**
```json
{
  "success": true,
  "message": "VM created successfully",
  "data": {
    "id": 1,
    "name": "web-server-01",
    "cores": 4,
    "ram": 8192,
    "disk": 100,
    "os": "Ubuntu 22.04 LTS",
    "status": "STOPPED",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## Usuarios Seed

El sistema crea automáticamente los siguientes usuarios al iniciar por primera vez:

| Nombre      | Email             | Password    | Rol    |
|-------------|-------------------|-------------|--------|
| Admin User  | admin@test.com    | `123456`    | ADMIN  |
| Client User | client@test.com   | `client123` | CLIENT |

---

## Manejo de Errores

Todas las respuestas de error siguen el formato:

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "NOT_FOUND",
  "message": "VM not found with id: 99",
  "path": "/vms/99"
}
```

| Código | Error               | Situación                           |
|--------|---------------------|-------------------------------------|
| 400    | BAD_REQUEST         | Datos de negocio inválidos          |
| 401    | UNAUTHORIZED        | Token ausente, inválido o expirado  |
| 403    | FORBIDDEN           | Rol insuficiente                    |
| 404    | NOT_FOUND           | Recurso no encontrado               |
| 422    | VALIDATION_ERROR    | Fallo de validación de campos       |
| 500    | INTERNAL_SERVER_ERROR | Error interno del servidor        |

---

## Estructura del Proyecto

```
ifx-virtual-machine-manager-backend/
├── src/
│   └── main/
│       ├── java/com/ifx/vm_manager/
│       │   ├── VmManagerApplication.java
│       │   ├── domain/
│       │   ├── application/
│       │   └── infrastructure/
│       └── resources/
│           ├── application.yml
│           └── db/
│               └── schema.sql
├── build.gradle
├── docker-compose.yml
├── Dockerfile
└── README.md
```
