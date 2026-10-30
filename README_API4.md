# API 4 - Recordatorios (Firebase y release)

## Instalación
1. Crear rama `feature/api4-firebase-release` a partir de `feature/api3-persistencia`.
2. Copiar el contenido del ZIP de cambios sobre la RAIZ del proyecto existente (mantener archivos no incluidos).
3. En Firebase Console crear proyecto Firebase, registrar aplicación Android `com.ignacio.recordatorios`.
4. Habilitar Firebase Authentication, proveedor **Correo/Contraseña**.
5. Crear Cloud Firestore y PUBLICAR las reglas de `firestore.rules` (no usar modo abierto en producción).
6. Descargar `google-services.json` REAL desde Firebase, copiarlo a `app/google-services.json`.
7. Sync Gradle, Build > Assemble Project, Run en Pixel 4. Este ZIP no contiene credenciales de firma ni configuración Firebase real.

## Flujo
Compose -> ViewModel -> ReminderRepository -> Room (datos visibles) -> Firestore (copia por usuario).
Los registros se guardan primero en Room, se marcan PENDING y se sincronizan con `set()` usando siempre el mismo `clientKey` como ID de documento. `Mutex` evita sincronizaciones concurrentes dentro de un proceso. Las escrituras pendientes pueden reenviarse sin crear un documento nuevo. Al leer se utiliza Source.SERVER. Los conflictos de edición simultánea NO están completamente resueltos (última escritura que llegue al servidor), y no se garantiza exactly-once ante fallos arbitrarios.

## Protección de sesiones
Hay una base Room SEPARADA por UID autenticado. Al cambiar de cuenta Compose crea otra navegación y ViewModels, así la segunda cuenta no observa temporalmente los datos de la primera. Las reglas Firestore restringen acceso al UID de la ruta y verifican ownerUid en escrituras. Una instalación anterior de API3 guardaba en `recordatorios.db`: no se copia automáticamente a ninguna cuenta para impedir filtraciones. Datos locales separados NO equivalen a cifrado de la base.

## Pruebas obligatorias antes de entregar
1. Usuario A se registra, crea recordatorio y sincroniza; revisar Firestore `users/A/reminders/{clientKey}`.
2. Editar sin conexión, comprobar Room y estado pendiente, recuperar red y sincronizar.
3. Repetir sincronización y comprobar 1 documento por clientKey.
4. Ingresar en otro dispositivo como A, recuperar listado mediante sincronización.
5. Cerrar sesión A e iniciar B: A no aparece. Intentar leer ruta A con credenciales de B: PERMISSION_DENIED (prueba con SDK autenticado, reglas o emulador de Firebase).
6. Reabrir con sesión activa, cerrar sesión, forzar pérdida de sesión y revisar UI.
7. Instalar el APK RELEASE firmado como aplicación independiente (distinto de Run debug) y comprobar Login, Firestore, Room, preferencias y error offline.

## Release firmado (pasos manuales necesarios)
- `versionCode=4`, `versionName=4.0.0-rc1`; Build Type `release`.
- Android Studio: Build > Generate Signed App Bundle / APK, primero APK, después Android App Bundle, usar MISMO keystore y misma variante release.
- Crear un keystore de prueba protegido, guardar password fuera de repo y hacer copia segura del keystore.
- Probar APK release real, anotar fecha, hash SHA-256 (`Get-FileHash ... -Algorithm SHA256`), versión, dispositivo y resultados.
- No incluir keystores, contraseñas ni claves administrativas en ZIP/Git. Incluir APK/AAB como archivos de entrega según consigna, y código de la misma revisión/commit.

## Notas
- Google Play Services en emulador necesario para flujo Firebase; si no hay `app/google-services.json`, el sync o compilación de Firebase fallará: es esperado.
- El proyecto previo usaba JSONPlaceholder para simulación: esta rama reemplaza la sincronización normal por Firestore. La clase vieja de Retrofit puede permanecer como referencia de API3 pero NO se utiliza para sincronizar esta API.
- No se incluye Firebase Admin SDK ni un servidor propio.
- Las preferencias DataStore de la API3 siguen a nivel del dispositivo (no son credenciales); si se desean preferencias privadas por cuenta, habría que separarlas también.
