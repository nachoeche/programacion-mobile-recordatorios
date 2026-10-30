# Actividad 3 — Recordatorios (archivos de actualización)

## Aplicación

Este ZIP contiene **archivos para agregar o reemplazar** sobre la rama `feature/api3-persistencia` que ya contiene la API 2. No es un proyecto Android completo. Antes de descomprimir, guardá una copia o commit de tu trabajo; extraé el contenido en la carpeta raíz `Recordatorios` conservando subcarpetas.

No borres `model`, `ui/theme`, `data/SampleData.kt` ni los demás archivos de la API 2. Se mantienen para aprovechar los modelos y los datos de categoría. El código del módulo `app` incluido en este ZIP actualiza los archivos de Gradle y manifiesto.

### 1. Preparar el entorno

- Abrí Android Studio y ejecutá `Sync Project with Gradle Files`.
- Ejecutá `Build > Assemble Project`. Después elegí el Pixel 4 API 34 y `Run app`.
- **Verificación pendiente:** la compilación no se pudo ejecutar en el entorno que generó el ZIP porque no se dispone del proyecto raíz/SDK completo. Se reemplazó `org.jetbrains.kotlin.kapt` (incompatible con Kotlin integrado de AGP 9) por `com.android.legacy-kapt`, con la versión de AGP en `gradle/libs.versions.toml`. El ZIP también incorpora `build.gradle.kts` de raíz para declarar el plugin. Si tenés personalizaciones en esos archivos, revisá diferencias antes de reemplazarlos.
- La primera ejecución de API 3 abre la base **vacía**: los ejemplos anteriores de API 2 estaban en memoria. Creá un recordatorio manualmente. No desinstales la app ni borres sus datos si querés comprobar persistencia.

### 2. Configuración remota

El proyecto trae `gradle.properties.example`. Para probar con el servicio didáctico predeterminado no hace falta editar nada: `https://jsonplaceholder.typicode.com/`.

Si necesitás otro servidor, agregá en el `gradle.properties` de la raíz (sin subir secretos):

```
API_BASE_URL=https://tu-servidor-ejemplo.test/
```

La URL **debe terminar en /**. Se usa Retrofit/OkHttp con `GET /posts`, `POST /posts`, `PUT /posts/{id}` y `DELETE /posts/{id}`. JSONPlaceholder simula escrituras pero **no las conserva realmente** ni implementa garantía de idempotencia de servidor. Por ello, la entrega demuestra arquitectura, manejo de red y sincronización *de demostración*; para garantizar ausencia de duplicaciones en un servicio real se necesita un backend que respete `X-Idempotency-Key` y soporte operaciones idempotentes. El identificador local `clientKey` permanece estable en Room para reintentos.

### 3. Arquitectura

- `data/settings/`: Preferences DataStore para tema oscuro, visualización de completados y orden.
- `data/local/`: Room `ReminderEntity`, `ReminderDao`, `ReminderDatabase` y mapeo a modelos de dominio.
- `data/remote/`: DTO `PostDto`, interfaz Retrofit, cliente OkHttp.
- `data/RoomReminderRepository.kt`: fuente local de verdad, altas/cambios/borrado lógico; operaciones pendientes y `Mutex` para serializar sincronización.
- `data/ReminderRepository.kt`: contrato consumido por ViewModels (no acceso directo a Room ni DTO).
- `ui/`: Compose, `collectAsStateWithLifecycle`, listado reactivo, formulario, edición, detalle y preferencias.
- `MainActivity.kt`: navegación y reintento de sync al recuperar conectividad.

Flujo: **Compose -> ViewModel -> ReminderRepository -> Room -> Flow -> ViewModel -> Compose**. En paralelo, el repositorio intenta comunicar cambios por Retrofit y actualiza las marcas de sincronización de Room.

### 4. Pruebas que tenés que realizar (y documentar)

1. **Room:** crear recordatorio, cerrar la app completamente y reabrir. Debe permanecer. Editar título y descripción y cambiar el estado desde Detalle; volver a abrir y verificar. Eliminar y volver a abrir; no debe mostrarse.
2. **DataStore:** cambiar modo oscuro, ocultar completados y ordenar por título. Forzar cierre y reabrir; las tres preferencias deben mantenerse. Sin datos previos los valores predeterminados son claro, mostrar completados y más nuevos. Una preferencia de orden desconocida vuelve a `NEWEST`.
3. **Offline:** desactivar la conexión del emulador; crear/editar/eliminar. La pantalla debe seguir operativa. Presionar `Sync`: mensaje recuperable por error de red. Reconectar y volver a pulsar `Sync` (el intento automático al recuperar la conexión no está implementado en esta versión; utilizá el botón Sync).
4. **Reintentos:** pulsar `Sync` varias veces; `Mutex` evita ejecuciones simultáneas. El DAO usa una clave local única; `GET /posts` vuelve a importar los mismos tres IDs sin duplicar. **Advertencia:** JSONPlaceholder no garantiza exactamente una creación remota si la respuesta de POST se pierde.
5. **Errores:** simular falta de conectividad. Registrar mensaje. Para casos HTTP 4xx/5xx o timeout reproducibles se requiere un servidor de pruebas que devuelva esos códigos; no afirmar cobertura sin ejecutarlos.

### 5. Informe y entrega (Actividad 3)

La consigna pide un **ZIP del proyecto completo**, no solo este ZIP de diferencias, acompañado de un informe técnico. Comprimí desde tu computadora el proyecto real incluyendo archivos raíz Gradle y `app/src`, pero excluyendo `.git`, `build`, `.gradle`, `local.properties` y credenciales.

Explicá: evolución API 2 -> 3; diferencias DataStore y Room; arquitectura local-first y mapeos; operaciones pendientes; prevención de duplicaciones y limitaciones del servicio de demostración; resultados reales de pruebas; instrucciones de instalación; configuración remota sin secretos. Incluí el diagrama de flujo anterior.
