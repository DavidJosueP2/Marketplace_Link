# Marketplace Link — Backend

API REST de **Marketplace Link**, un marketplace geolocalizado donde los usuarios
publican productos y servicios, los compran/marcan como favoritos y un flujo de
moderación gestiona reportes, incidencias y apelaciones sobre las publicaciones.

Este repositorio contiene **únicamente el backend**. El cliente web está en el
repositorio [`Marketplace-Link-Front`](../Marketplace-Link-Front).

## ¿Qué problema resuelve?

Centraliza la publicación y descubrimiento de productos/servicios cercanos
(usando coordenadas geográficas), con autenticación por roles y un sistema de
moderación que permite reportar publicaciones, abrir incidencias, resolverlas y
apelar decisiones.

## Tecnologías

- **Java 21** + **Spring Boot 3.5**
- **Maven** (con wrapper `./mvnw`)
- **PostgreSQL 16 + PostGIS** (datos geográficos `GEOGRAPHY(Point, 4326)`)
- **Hibernate Spatial** / Spring Data JPA
- **Spring Security + JWT** (jjwt)
- **MapStruct** + **Lombok**
- **SpringDoc / Swagger UI**
- Spring Mail (SMTP), Spring Actuator

> Nota: PostGIS es obligatorio. No se puede sustituir la base de datos por H2 u
> otra sin soporte espacial, porque las entidades usan tipos geográficos.

## Estructura del proyecto

```
src/main/java/com/gpis/marketplace_link/
├── config/          # Configuración general (async, etc.)
├── dto/             # Objetos de transferencia (request/response)
├── entities/        # Entidades JPA
├── enums/
├── exceptions/      # Excepciones de negocio + @ControllerAdvice
├── jobs/            # Tareas programadas
├── mail/            # Envío de correos
├── mappers/         # MapStruct
├── repositories/    # Spring Data JPA
├── rest/            # Controllers REST
├── security/        # JWT, filtros, configs de seguridad y CORS, seeders
├── services/        # Lógica de negocio
├── specifications/  # JPA Specifications (filtros dinámicos)
└── validation/      # Validaciones y anotaciones custom
src/main/resources/
├── application.yml        # Perfil por defecto (prod)
├── application-dev.yml    # Perfil dev
├── application-test.yml   # Perfil test
└── emails/                # Plantillas de correo
docker/
└── init.sql               # Esquema + datos semilla (PostgreSQL/PostGIS)
```

## Almacenamiento de imágenes

Este backend **no persiste imágenes** (ni en disco ni en almacenamiento externo).
Anteriormente usaba Azure Blob Storage; al migrar a VPS se eliminó esa
dependencia. `FileStorageService` genera una **referencia de texto** (nombre de
archivo) que se guarda en base de datos, pero el binario no se almacena.

Consecuencia: las imágenes subidas por la app no quedan accesibles tras subirlas.
Si en el futuro se necesita persistencia real, el único punto a implementar es
`FileStorageService.storeFile(...)`.

## Variables de entorno

Copia `.env.example` a `.env` y ajústalo. Variables principales:

| Variable | Descripción |
|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | Conexión a PostgreSQL/PostGIS |
| `SERVER_PORT` | Puerto del backend (8080 por defecto) |
| `JWT_SECRET` | Clave para firmar JWT (>= 32 caracteres). **Obligatoria en producción** |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` | SMTP para correos |
| `FRONTEND_URL` | Orígenes CORS (coma-separados) y URL usada en enlaces de email |
| `MODERATOR_DEFAULT_PASSWORD` | Password inicial de moderadores |
| `SUSPENDED_TIME_DAYS` | Días de suspensión por defecto |

## Ejecución en local

### Requisitos
- JDK 21
- Docker (para la base de datos PostGIS)

### Pasos

```bash
# 1) Copiar variables de entorno
cp .env.example .env    # edita los valores

# 2) Levantar solo la base de datos
docker compose up -d mplink_postgres

# 3) Ejecutar el backend con el perfil dev (usa defaults de application-dev.yml)
export $(grep -v '^#' .env | xargs)   # opcional: cargar .env en la shell
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

La API queda en `http://localhost:8080`.
Documentación interactiva: `http://localhost:8080/swagger-ui.html`.

### Compilar / empaquetar

```bash
./mvnw clean package            # genera target/*.jar (ejecuta tests)
./mvnw clean package -DskipTests
```

### Tests

Los tests de integración necesitan una base de datos PostGIS de pruebas
(ver `DB_*_TEST` en `.env.example`). Levántala aparte antes de ejecutar:

```bash
./mvnw test
```

## Ejecución con Docker

```bash
cp .env.example .env            # ajusta DB_PASSWORD, JWT_SECRET, MAIL_*
docker compose up -d --build    # levanta PostGIS + backend
docker compose logs -f mplink_backend
```

- Backend: `http://localhost:8080`
- El servicio crea la red `back_mplink_net`, que el **frontend** reutiliza como
  red externa. Levanta primero este backend.

### Comandos útiles (Docker)

```bash
docker compose ps                       # estado de contenedores
docker compose logs -f mplink_backend   # logs del backend
docker compose restart mplink_backend   # reiniciar
docker compose down                     # detener (conserva el volumen de datos)
docker compose down -v                  # detener y BORRAR datos (¡cuidado!)
```

### Backup / restauración de la base de datos

```bash
# Backup
docker exec mplink_marketplace_db pg_dump -U postgres marketplace_db > backup.sql

# Restauración
cat backup.sql | docker exec -i mplink_marketplace_db psql -U postgres -d marketplace_db
```

## Despliegue en VPS

Consulta la guía completa en [`docs/deployment-vps.md`](docs/deployment-vps.md)
(requisitos del servidor, Docker, Nginx reverse proxy, HTTPS con Certbot,
backups y actualización).

## Seguridad — antes de producción

- [ ] Definir un `JWT_SECRET` propio y fuerte (`openssl rand -base64 48`).
- [ ] Cambiar `DB_PASSWORD` y `MODERATOR_DEFAULT_PASSWORD`.
- [ ] Revisar/limpiar los datos semilla de `docker/init.sql` (incluye usuarios y
      contraseñas de ejemplo como `admin123`). No usar en producción tal cual.
- [ ] Valorar deshabilitar Swagger en producción (`springdoc.swagger-ui.enabled=false`).
- [ ] No exponer el puerto de PostgreSQL al exterior (usar solo la red Docker).

## Estado actual

Funcional: autenticación/roles, publicaciones, favoritos, categorías,
moderación (reportes, incidencias, apelaciones) y correos. Sin persistencia de
imágenes (referencia de texto).

## Mejoras futuras recomendadas

- Implementar persistencia real de imágenes si el producto lo requiere.
- Unificar las dos clases `TokenJwtConfig` duplicadas.
- Reducir el tamaño del seed de `init.sql` o separarlo en un script opcional.
