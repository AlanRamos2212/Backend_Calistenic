# Evaluación de calidad técnica

## Funcionalidad crítica

Autenticación y control de acceso para consultar estadísticas y sesiones del
wearable. El flujo combina Angular, API REST, JWT, Spring Security y
PostgreSQL.

## Diagnóstico inicial

| Nivel | Evidencia | Cantidad | Estado |
|---|---|---:|---|
| Unitarias backend | `JwtTokenProviderTest` aislado del contexto Spring | 4 casos | Implementadas |
| Unitarias/integración frontend | `auth.service.spec.ts` | 3 casos | Implementadas |
| Integración frontend | `auth.integration.spec.ts` | 3 casos | Implementadas |
| Integración backend | `WearableSessionControllerAuthTest` | 4 casos | Implementadas |
| Contexto backend | `ModuloApplicationTests` | 1 caso | Implementado |
| E2E automatizadas | No hay Playwright/Cypress configurado todavía | 0 | Pendiente |

Las pruebas de backend que usan `@SpringBootTest` requieren PostgreSQL,
Elasticsearch y las dependencias Maven disponibles. No deben clasificarse como
unitarias puras.

## Casos unitarios backend

`JwtTokenProviderTest` cubre:

- Token válido: generación, firma, extracción de usuario y validación.
- Caso límite funcional: token emitido para otro usuario es rechazado.
- Caso inválido: token malformado produce `JwtException`.
- Regla específica del wearable: device token válido con claim `token_use`.

## Resultado comprobado

Comando ejecutado en Angular:

```bash
npm test -- --watch=false
```

Resultado: **3 archivos y 8 pruebas aprobadas**.

Comando pendiente de ejecutar cuando Maven pueda acceder al repositorio de
dependencias:

```bash
./mvnw.cmd -Dtest=JwtTokenProviderTest test
./mvnw.cmd test
```

La ejecución actual fue bloqueada por la red al descargar
`spring-boot-starter-parent:3.4.13`, no por un fallo de las pruebas.

## Brechas y mejoras priorizadas

1. **Alta prioridad:** añadir una prueba E2E de registro/login y acceso a
   progreso, porque actualmente no se valida el flujo completo desde el
   navegador.
2. **Alta prioridad:** ejecutar integración backend con una base de datos de
   prueba aislada, evitando depender de datos locales o productivos.

Antes de cada entrega deben ejecutarse las pruebas unitarias y de integración;
antes de publicar, también la E2E y una verificación de salud de la API.
