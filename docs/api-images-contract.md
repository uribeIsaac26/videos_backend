# Image Gallery API — Contrato v1

Módulo para subir, listar, servir, eliminar y etiquetar imágenes. Sigue el mismo patrón que el módulo de videos, pero sin transcodificación, streaming por rangos ni thumbnails: la imagen que se sube es el archivo que se sirve.

- **Base path:** `/api/images`
- **Formato:** JSON (salvo subida y descarga de archivo)
- **Auth:** cookie httpOnly

## Autenticación

Igual que el resto del backend: no hay endpoints públicos en `/api/images`. Todas las peticiones requieren la cookie `jwt` (httpOnly, `SameSite=Lax`) que se obtiene al hacer login en `POST /api/auth/login`.

Desde el front, las llamadas deben incluir credenciales para que el navegador envíe la cookie:

```js
fetch("/api/images", { credentials: "include" });

// axios
axios.create({ withCredentials: true });
```

> Sin cookie válida, Spring Security corta la petición antes de llegar al controller (comportamiento por defecto, sin body JSON estructurado).

## Modelos

### ImageModel

Se devuelve en subida, listado y asignación de tags.

```json
{
  "id": 42,
  "title": "Atardecer oficina",
  "imageUrl": "/api/images/42/image",
  "size": 184320,
  "contentType": "image/jpeg",
  "tags": [
    { "id": 3, "name": "eventos" }
  ]
}
```

| Campo | Tipo | Notas |
|---|---|---|
| `id` | integer | Autogenerado |
| `title` | string | Título dado en la subida |
| `imageUrl` | string | Ruta relativa para `<img src>` — requiere la cookie de sesión |
| `size` | integer | Bytes del archivo original |
| `contentType` | string | MIME reportado por el cliente al subir (no verificado contra el archivo real) |
| `tags` | `TagResponse[]` | Vacío si no se han asignado tags |

### TagResponse

```json
{ "id": 3, "name": "eventos" }
```

Los tags se crean y listan con los endpoints ya existentes en `/api/tags` (`GET`, `POST`, `GET /by-names`) — se reutilizan tal cual, no hay un catálogo de tags separado para imágenes.

### Page\<T\>

Wrapper estándar de Spring Data en todos los listados paginados.

```json
{
  "content": [/* ImageModel[] */],
  "totalElements": 57,
  "totalPages": 3,
  "number": 0,
  "size": 20,
  "first": true,
  "last": false,
  "numberOfElements": 20,
  "empty": false
}
```

### ErrorResponse

```json
{
  "status": 404,
  "message": "Imagen no encontrada",
  "timeStamp": "2026-07-07T10:32:01.123"
}
```

## Endpoints

### `POST /api/images` — Subir imagen

**201 Created**

Sube una imagen y crea el registro. Guarda el archivo en `{media.base-path}/images/` conservando la extensión original.

**Request:** `Content-Type: multipart/form-data`

| Campo | Tipo | Requerido |
|---|---|---|
| `title` | string | sí |
| `imageFile` | file | sí · MIME debe iniciar con `image/` |

```js
const form = new FormData();
form.append("title", title);
form.append("imageFile", file);

await fetch("/api/images", {
  method: "POST",
  credentials: "include",
  body: form
});
```

**Response 201:** body `ImageModel` (ver arriba). El registro se crea de forma síncrona — a diferencia de los videos, no hay estado `PROCESANDO`: si la respuesta llega, la imagen ya está lista para consultarse.

**Errores:**

| Status | Motivo |
|---|---|
| 500 | Falta `title`/`imageFile`, o el archivo no es una imagen válida. Ver [Limitaciones conocidas](#limitaciones-conocidas) — el código HTTP no es 400. |
| 500 | Fallo al escribir en disco o al guardar en base de datos. |

### `GET /api/images` — Listar

**200 OK**

Lista imágenes paginadas, con sus tags.

**Query params (Pageable):**

| Param | Tipo | Default |
|---|---|---|
| `page` | integer | 0 |
| `size` | integer | 20 |
| `sort` | string | opcional · ej. `id,desc` |

**Response 200:** body `Page<ImageModel>`.

### `GET /api/images/{id}/image` — Obtener archivo

**200 OK**

Sirve el binario de la imagen. Úsalo directo como `src` de un `<img>`; el navegador debe tener la cookie de sesión (mismo dominio o CORS con credenciales).

**Response 200:** bytes de la imagen. `Content-Type` autodetectado por extensión.

**Errores:**

| Status | Motivo |
|---|---|
| 404 | `ErrorResponse` — id inexistente o archivo faltante en disco. |

### `DELETE /api/images/{id}` — Eliminar

**200 OK**

Elimina el archivo físico y el registro. Body vacío en éxito.

**Errores:**

| Status | Motivo |
|---|---|
| 404 | `ErrorResponse` — id inexistente. |

### `PUT /api/images/tag` — Asignar tags

**200 OK**

Reemplaza por completo el set de tags de una imagen (no es un "agregar", es un "set to").

**Request body:**

```json
{
  "imageId": 42,
  "tagIds": [3, 7]
}
```

**Response 200:** body `ImageModel` con el nuevo set de tags.

**Errores:**

| Status | Motivo |
|---|---|
| 404 | `ErrorResponse` — `imageId` inexistente. |

> Los `tagIds` que no existan en la base de datos simplemente se ignoran (no hay error) — revisa el `ImageModel` devuelto para confirmar qué quedó realmente aplicado.

### `GET /api/images/tag` — Filtrar por tags

**200 OK**

Filtra imágenes que tengan **todos** los tags indicados (lógica AND, igual que en videos).

**Query params:**

| Param | Tipo | Notas |
|---|---|---|
| `tagIds` | integer[] | sí · repetido: `?tagIds=3&tagIds=7` |
| `page, size, sort` | — | Pageable estándar |

**Response 200:** body `Page<ImageModel>`.

> No probado con `tagIds` vacío — evita mandar la petición sin al menos un id.

## Limitaciones conocidas

- **Errores de validación en la subida devuelven 500, no 400.** El backend valida `title`/`imageFile` lanzando una excepción que cae en el handler genérico, así que hoy el front solo puede distinguir "no fue 201" — no confíes en `error.message` para mostrarlo al usuario todavía. Si esto bloquea la UX de subida, hay que priorizar el fix en el backend.
- **No hay validación de tamaño máximo de archivo** a nivel de este módulo (puede existir un límite global de Spring en `multipart.max-file-size` fuera de este contrato).
- **No hay redimensionado ni miniatura** — el `imageUrl` siempre sirve el archivo original. Si se necesitan miniaturas para una grilla de galería, hay que agregarlo.
