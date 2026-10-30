# API 2 - Recordatorios (paquete de cambios)

Este ZIP contiene **solo archivos nuevos o modificados** respecto de la API 1 recibida.
No sustituye el proyecto Android Studio completo. Conservá los demás archivos del proyecto original.

## Instalación
1. Hacé un commit o una rama de respaldo de la API 1.
2. Cerrá la aplicación en el emulador.
3. Extraé este ZIP en la **raíz del proyecto `Recordatorios`**, no dentro de `app`.
4. Aceptá reemplazar `app/build.gradle.kts`, `MainActivity.kt` y `ui/ReminderScreen.kt`.
5. Abrí Android Studio, sincronizá Gradle y ejecutá `Build > Make Project`.
6. Si Gradle informa que no encuentra una dependencia, confirmá conexión a internet y repositorios `google()` y `mavenCentral()` en el proyecto raíz.

## Comportamientos
- Listado inicial con cinco datos simulados.
- FAB `+` abre formulario; título >= 4, descripción <= 200, categoría y prioridad válidas.
- Guardar agrega un recordatorio *solo en memoria* y regresa al listado.
- Seleccionar una tarjeta abre el detalle usando exclusivamente el ID.
- Estado de carga simulado en la lectura y en el guardado.
- Componentes reutilizables `ReminderCard` y `StatusMessage`.

## Pruebas de estados vacío / error
En la pantalla de listado hay tres controles de prueba: `Probar vacío`, `Probar error` y `Restaurar`.
`Probar vacío` muestra el mensaje y el acceso a creación. `Probar error` muestra el mensaje y el botón `Reintentar`.
`Restaurar` y `Reintentar` vuelven a consultar los datos del repositorio.
En creación existe `Probar error de guardado`, que presenta el mensaje de error general sin guardar un elemento.
Estos controles de prueba son deliberadamente visibles para producir evidencias y pueden retirarse después de la evaluación.

## Rutas
| Ruta | Argumento |
| --- | --- |
| `list` | Ninguno (inicio) |
| `create` | Ninguno |
| `detail/{id}` | `id`: Int positivo |

## Importante
- La creación no persiste tras finalizar el proceso Android.
- No hay un backend ni sincronización remota.
- El botón `Probar error de guardado` muestra un error simulado sin enviar al repositorio; sirve para comprobar el estado visual, no representa una falla real del guardado asíncrono.
- No se pudo compilar en este entorno porque el ZIP recibido contiene solo `app/`, no el Gradle Wrapper ni la configuración raíz de Android Studio. **Validá compilación y ejecución en tu computadora**.
