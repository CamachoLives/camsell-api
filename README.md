# Camsell API

Backend del marketplace **Camsell** para compra y venta de vehículos: catálogo
parametrizable, flujo de ofertas de compra y sección informativa editorial.

Frontend que lo consume: [camsell-web](https://github.com/CamachoLives/camsell-web).

---

## Qué resuelve

| Capacidad | Detalle |
|---|---|
| **Catálogo parametrizable** | Las características de un vehículo (combustible, transmisión, color, equipamiento, ciudad, cilindrada…) no están codificadas: son *grupos de parámetros* administrables. Crear un grupo nuevo agrega automáticamente un campo al formulario de publicación y, si es filtrable, un filtro en la búsqueda — sin migraciones ni despliegues. |
| **Venta** | Publicación con imágenes, estados `DRAFT → PUBLISHED → RESERVED → SOLD → ARCHIVED`, destacados y reglas de validación configurables. |
| **Compra** | Ofertas de compra con mínimo del 40 % del precio, aceptación/rechazo por el vendedor y reserva automática del vehículo al aceptar. |
| **Informativo** | Notas editoriales con categorías (parametrizadas), etiquetas, destacados, tiempo de lectura calculado y notas relacionadas. |
| **Identidad** | Registro y autenticación JWT con roles `USER`, `DEALER`, `ADMIN`. |

---

## Arquitectura

Monolito modular con **arquitectura hexagonal (puertos y adaptadores)** por
contexto acotado. Cada módulo es independiente y solo se comunica con los demás
a través de puertos públicos.

```
com.camsell
├── shared/          Núcleo compartido: paginación de dominio, errores RFC-9457, auditoría
├── identity/        Usuarios, JWT, roles      ── expone CurrentUserPort
├── parameters/      Grupos de parámetros, marcas y modelos
├── catalog/         Publicaciones, ofertas, favoritos
├── news/            Contenido editorial
└── bootstrap/       Carga de datos de demostración
```

Dentro de cada contexto:

```
domain/
  model/         Agregados y objetos de valor (records inmutables, sin Spring)
  port/in/       Casos de uso que ofrece el módulo
  port/out/      Lo que el módulo necesita del exterior
application/     Servicios que implementan los casos de uso y orquestan transacciones
infrastructure/
  persistence/   Entidades JPA, repositorios Spring Data y adaptadores
  web/           Controladores REST y DTOs
  security/      Adaptadores de seguridad (solo en identity)
```

**Reglas que el código respeta y los tests verifican con ArchUnit:**

- `domain` no depende de Spring, JPA ni de `infrastructure`.
- `application` no depende de `infrastructure`.
- Los contextos se comunican solo por `domain.port`, nunca por entidades JPA.
- Solo `identity.infrastructure.security` lee el `SecurityContext` de Spring.

### Decisiones de diseño

- **Paginación de dominio.** `PageQuery`/`PageResult` mantienen `Pageable` y
  `Page` de Spring Data fuera de los puertos; `PageMappings` es el único punto
  de traducción.
- **Búsqueda con Criteria API.** `ListingSpecifications` arma la consulta
  dinámicamente; los filtros por parámetros se resuelven con subconsultas
  `EXISTS`, de modo que un grupo nuevo se vuelve filtrable sin tocar esa clase.
- **Atributos flexibles.** `listing_attribute` guarda los valores de parámetros
  por publicación, lo que permite agregar especificaciones sin alterar la tabla
  `listing`.
- **JWT propio como resource server.** La API firma sus tokens con HMAC-SHA256
  (`NimbusJwtEncoder`) y los valida como *resource server* de sí misma: cero
  sesiones, cero estado compartido entre instancias.
- **Errores uniformes.** Todas las respuestas de error son `ProblemDetail`
  (RFC 9457) con un `code` estable que el frontend puede interpretar.
- **Correlación.** Cada petición recibe un `X-Request-Id` que viaja al log vía
  MDC y se devuelve en la respuesta.

---

## Stack

| | |
|---|---|
| Java | 25 (compilado a bytecode 21) |
| Spring Boot | 4.1.1 — Web MVC, Data JPA, Security, Validation, Actuator, Cache |
| Persistencia | Hibernate 7, Flyway 12 — H2 en desarrollo, PostgreSQL 17 en producción |
| Seguridad | Spring Security 7 + OAuth2 Resource Server (JWT HS256) |
| Tests | JUnit 5, MockMvc, ArchUnit |
| Extras | Virtual threads habilitados, caché de parámetros |

---

## Cómo correrlo

### Desarrollo (H2 en memoria, sin instalar nada)

```bash
mvn spring-boot:run
```

- API: <http://localhost:8080/api/v1>
- Consola H2: <http://localhost:8080/h2-console> (JDBC `jdbc:h2:mem:camsell`, usuario `sa`, sin clave)
- Salud: <http://localhost:8080/actuator/health>

El perfil `dev` carga datos de demostración en el primer arranque.

### Producción local (PostgreSQL con Docker)

```bash
docker compose up --build
```

### Variables de entorno

| Variable | Default | Descripción |
|---|---|---|
| `CAMSELL_PROFILE` | `dev` | Perfil activo (`dev` \| `prod`) |
| `CAMSELL_PORT` | `8080` | Puerto HTTP |
| `CAMSELL_JWT_SECRET` | secreto de desarrollo | **Rotar en producción.** Mínimo 32 bytes |
| `CAMSELL_DB_URL` | `jdbc:postgresql://localhost:5432/camsell` | Solo perfil `prod` |
| `CAMSELL_DB_USER` / `CAMSELL_DB_PASSWORD` | `camsell` | Solo perfil `prod` |
| `CAMSELL_DEMO_DATA` | `true` en `dev`, `false` en `prod` | Carga el set de demostración |

### Parámetros de negocio configurables

En `application.yml`, bajo `camsell.catalog`:

| Clave | Default | Efecto |
|---|---|---|
| `max-images-per-listing` | `12` | Tope de imágenes por publicación |
| `max-featured-listings` | `8` | Tope de destacados en portada |
| `min-year` | `1950` | Año mínimo aceptado |

---

## Credenciales de demostración

Clave para todas: `Camsell2026*`

| Correo | Rol | Para qué sirve |
|---|---|---|
| `admin@camsell.dev` | `ADMIN` | Administrar parámetros, marcas, noticias y destacados |
| `ventas@autonorte.co` | `DEALER` | Vendedor profesional con publicaciones destacadas |
| `laura.gomez@mail.com` | `USER` | Vendedora particular |
| `carlos.ruiz@mail.com` | `USER` | Comprador con ofertas y favoritos activos |

---

## API

Base: `/api/v1`. Contrato completo en [`openapi.yaml`](src/main/resources/static/openapi.yaml).

### Autenticación

| Método | Ruta | Acceso |
|---|---|---|
| `POST` | `/auth/register` | público |
| `POST` | `/auth/login` | público |
| `GET` | `/auth/me` | autenticado |
| `PUT` | `/auth/me` | autenticado |
| `PUT` | `/auth/me/password` | autenticado |

### Parámetros — el esquema que hace parametrizable el sitio

| Método | Ruta | Acceso |
|---|---|---|
| `GET` | `/parameters/groups` | público |
| `GET` | `/parameters/groups/{code}` | público |
| `GET` | `/parameters/groups/{code}/options` | público |
| `POST` `PUT` `DELETE` | `/admin/parameters/groups[/{code}][/options[/{id}]]` | `ADMIN` |

### Marcas y modelos

| Método | Ruta | Acceso |
|---|---|---|
| `GET` | `/brands?withModels=true` | público |
| `GET` | `/brands/{slug}` | público |
| `GET` | `/brands/{brandId}/models` | público |
| `POST` `PUT` `DELETE` | `/admin/brands[...]` | `ADMIN` |

### Catálogo

| Método | Ruta | Acceso |
|---|---|---|
| `GET` | `/listings` | público |
| `GET` | `/listings/featured` | público |
| `GET` | `/listings/{slug}` | público |
| `GET` | `/listings/mine` | autenticado |
| `POST` | `/listings` | autenticado |
| `PUT` `DELETE` | `/listings/{id}` | dueño o `ADMIN` |
| `PATCH` | `/listings/{id}/status` | dueño o `ADMIN` |
| `PUT` | `/listings/{id}/featured` | `ADMIN` |
| `GET` | `/stats/catalog` | público |

**Filtros de búsqueda.** Además de `text`, `brandIds`, `modelIds`, `minYear`,
`maxYear`, `minPrice`, `maxPrice`, `maxMileage`, `cityCode`, `negotiable` y
`featured`, los filtros por parámetros se envían con el parámetro repetible
`attr` en formato `GRUPO:OPCION`:

```
GET /api/v1/listings?attr=FUEL_TYPE:HYBRID&attr=TRANSMISSION:AUTOMATIC&maxPrice=180000000
```

Un grupo de parámetros nuevo queda filtrable al instante, sin cambios en el
contrato ni en el código de búsqueda.

### Compra y favoritos

| Método | Ruta | Acceso |
|---|---|---|
| `POST` | `/listings/{id}/offers` | autenticado |
| `GET` | `/listings/{id}/offers` | vendedor o `ADMIN` |
| `GET` | `/offers/sent` · `/offers/received` · `/offers/pending-count` | autenticado |
| `POST` | `/offers/{id}/accept` · `/reject` | vendedor o `ADMIN` |
| `POST` | `/offers/{id}/withdraw` | comprador |
| `GET` | `/favorites` | autenticado |
| `POST` | `/favorites/{listingId}/toggle` | autenticado |

### Noticias

| Método | Ruta | Acceso |
|---|---|---|
| `GET` | `/news` · `/news/featured` · `/news/tags` | público |
| `GET` | `/news/{slug}` · `/news/{slug}/related` | público |
| `GET` `POST` `PUT` `PATCH` `DELETE` | `/admin/news[...]` | `ADMIN` |

---

## Tests

```bash
mvn test
```

Cubre las reglas de arquitectura (ArchUnit), el dominio del catálogo y de
parámetros, y los endpoints públicos con MockMvc.

---

## Licencia

MIT
