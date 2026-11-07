# Triángulo de pruebas — Evidencia de ejecución

**Fecha:** 2026-11-06 · **App:** CalistenicTrack (Flutter) · **Servidor:** Spring Boot `calistenic-backend` (Docker, puerto 8083) · **Almacenamiento:** PostgreSQL 15
**Operación probada:** registro de sesión de entrenamiento (`POST /api/wearable-sessions/start` → `…/{id}/stop`) y consultas (`GET /api/wearable-sessions/{id}`).

> Nota de ejecución: los 3 primeros requests después de reiniciar el contenedor tardan ~8 s (arranque frío de Spring + hash BCrypt del login). Todas las operaciones subsecuentes tardan **0.06–0.3 s**.

---

## TC-01 Establecimiento de conexión

- **Esperado:** la aplicación establece comunicación con el servicio.
- **Ejecutado:** `POST /api/auth/login` con credenciales válidas del usuario de prueba.
- **Resultado:** ✅ Exitoso
- **Evidencia:** `HTTP 200`, cuerpo `{"token":"eyJhbGciOiJIUzUxMiJ9…"}` (JWT HS512 de 202 caracteres, expiración 1 h). Sin token el mismo endpoint responde `401`.
- **Observación:** Primera petición tras arranque frío: 7.9 s total (BCrypt + init JPA).

## TC-02 Envío de información

- **Esperado:** el servicio recibe correctamente los datos.
- **Ejecutado:** `POST /api/wearable-sessions/start` body `{"exerciseId":2}` → `200`. Posteriormente `POST /api/wearable-sessions/4/stop` con `durationSec=612`, `caloriesBurned=96.5`, `endedAt=2026-11-06T11:15:14Z` → `200`.
- **Resultado:** ✅ Exitoso
- **Evidencia:** respuesta del `start`:
```json
{"id":4,"userId":215,"exerciseId":2,"exerciseName":"Pull-ups","status":"STARTED","startedAt":"2026-11-06T11:14:01.725Z","endedAt":null}
```
- **Tiempos:** stop `0.30 s`.

## TC-03 Recepción de información

- **Esperado:** la aplicación muestra los datos recibidos.
- **Ejecutado:** `GET /api/wearable-sessions/4` con Bearer token.
- **Resultado:** ✅ Exitoso
- **Evidencia:** `HTTP 200, 0.057 s` y el registro COMPLETED con todos los campos poblados (ver TC-04).
- **Observación:** el endpoint exige JWT (`401` sin token); la app lo inyecta automáticamente de `flutter_secure_storage`.

## TC-04 Integridad de información

- **Esperado:** datos enviados y recibidos coinciden.
- **Resultado:** ✅ Exitoso
| Campo | Enviado | Recibido | ¿Coincide? |
|---|---|---|---|
| id | (asignado por servidor) | `4` | — tiene valor consistente |
| userId | (resuelto del JWT) | `215` | ✅ usuario autenticado |
| exerciseId | `2` | `2` | ✅ |
| exerciseName | — | `Pull-ups` | ✅ categoría referencial correcta |
| status | (acción stop) | `STARTED → COMPLETED` | ✅ transición correcta |
| startedAt | (servidor al start) | `2026-11-06T11:14:01.725Z` | ✅ persistido |
| endedAt | `2026-11-06T11:15:14Z` | `2026-11-06T11:15:14Z` | ✅ |
| durationSec | `612` | `612` | ✅ |
| caloriesBurned | `96.5` | `96.5` | ✅ |
| avgHr/maxHr/minHr | (no enviados — sin sensor) | `null` | ✅ honesto, no fabricados |

**Sin campos perdidos, alterados ni duplicados.**

## TC-05 Interrupción de conexión

- **Esperado:** la aplicación detecta e informa el fallo; no simula éxito falso.
- **Ejecutado:** `docker compose stop backend` (equivale a cortar la comunicación), seguido de `GET /api/exercises`.
- **Resultado:** ✅ Exitoso
- **Evidencia:** `HTTP=000, time=2.24 s, error: "Failed to connect to localhost port 8083: Could not connect to server"`.
- **Comportamiento en la app (mismo escenario emulado por la suite):** `ApiClient` traduce la caída a `ApiException('No se pudo conectar con el servidor.')`; las pantallas muestran la alerta de error y los servicios offline-first pintan el **Último caché** con banner `OfflineBanner` en vez de fingir éxito. Cubierto por los tests `services_test.dart` (ClientException → fallback de caché) — 22/22.

## TC-06 Recuperación de conexión

- **Esperado:** la aplicación vuelve a transferir información.
- **Ejecutado:** `docker compose start backend` → health → repetir `GET /api/wearable-sessions/4`.
- **Resultado:** ✅ Exitoso
- **Evidencia:** `HTTP=200, 2.6 s` con el **mismo registro íntegro** al reintentar (id 4, COMPLETED, 612 s, 96.5 kcal). `GET /actuator/health` → 200. Los datos sobrevivieron a la caída (Postgres en volumen persistente).

## TC-07 Prevención de duplicados

- **Esperado:** la operación no genera registros duplicados inesperados.
- **Ejecutado:** conteo de sesiones del usuario 215 antes y después de las operaciones; consultas repetidas.
- **Resultado:** ✅ Exitoso
- **Evidencia:** sesiones ANTES = 1, sesiones DESPUÉS = 1 → los `GET` repetidos **no crean registros**; cada `POST /start` crea exactamente 1 sesión (id asignado consecutivo). Sin duplicados.

---

---

## Adendumos de producción (corte 6-7-nov: Render + PostgreSQL Cloud)

- **Servidor:** backend Spring Boot desplegado en Render (`https://backend-calistenic.onrender.com`), health `GET /actuator/health` → **200 UP**, Flyway en nube hasta **V12** (`support_reports`).
- **Almacenamiento:** PostgreSQL 15 (Render, volumen persistente).
- **Smoke de producción (2026-11-06):** register 200, login 200, routines 200, user-routines 201, posts 201 — todos OK contra `https://backend-calistenic.onrender.com/api`.
- **Soporte técnico (Práctica 7) en producción:** `POST /api/reports` → 201 (reporte OPEN creado), `GET /api/reports/mine` → 200 `[]` (usuario autenticado, sin reportes previos).
- **Build móvil v1.0.0:** `flutter build apk --release --dart-define=API_BASE_URL=https://backend-calistenic.onrender.com/api` → `build/app/outputs/flutter-apk/app-release.apk` (52.6 MB) adjunto a GitHub Release `v1.0.0` (AlanRamos2212/Calistenic_Mobile).
- **Web:** rutas protegidas con `AuthGuard` (`dashboard`, `ejercicios`, `progreso`, `comunidad`, `soporte`, `perfil`); landing/contacto/politicas/privacidad públicas.

## Resultado general de las pruebas

| ID | Prueba | Resultado | Observación |
|---|---|---|---|
| TC-01 | Conexión | ✅ Exitoso | 401 sin credenciales, JWT correcto |
| TC-02 | Envío | ✅ Exitoso | start + stop aceptados |
| TC-03 | Recepción | ✅ Exitoso | 0.057 s subsecuentes |
| TC-04 | Integridad | ✅ Exitoso | 10/10 campos coinciden |
| TC-05 | Interrupción | ✅ Exitoso | error comunicado, caché offline, sin éxito falso |
| TC-06 | Recuperación | ✅ Exitoso | reintento OK, datos íntegros |
| TC-07 | Duplicados | ✅ Exitoso | 1 registro por operación |

## Complemento manual desde el teléfono (para capturas)

1. Corre la app (`--dart-define=API_BASE_URL=http://<IP-LAN>:8083/api`).
2. TC-01/02/03: inicia sesión, abre Rutinas → detalle → Comenzar → completa ≥1 serie (sesión abierta) → Completar. En home/estadística se refleja la sesión.
3. TC-05: desactiva Wi-Fi → intenta refrescar una pantalla → aparece el mensaje de error/offline.
4. TC-06: reactiva Wi-Fi → pull-to-refresh → los datos vuelven (y la sesión abortada se registra al reconectar por el flujo de cierre de sesión).
5. Captura de respaldo servidor-side: `docker compose logs backend --since 10m`.
