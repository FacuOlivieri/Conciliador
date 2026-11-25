# Plan 001 — Subida de archivos
Spec: `specs/001-subida-archivos-csv/spec.md` (Estado: aprobada, sin dudas abiertas)
Estado del plan: aprobado (2026-11-24)

## 0. Precondiciones verificadas
- Spec aprobada el 2026-11-24, 23 RF y 6 RNF, sin dudas abiertas.
- Constitución vigente: regla 1 (Spring Boot, Thymeleaf, POI, Commons CSV), regla 3 (lógica fuera de la vista), regla 4 (tests primero), regla 5 (Bean Validation, nada sensible en logs).
- No hace falta ninguna dependencia nueva. Todo sale de `pom.xml` actual + JDK (`MessageDigest`, `CharsetDecoder`).
- Paquete base: `com.proyecto.Coinciliador`. Estructura MVC + Helpers + Exceptions + Utils, interfaces `IXxxService`.
- **"Hoy" como parámetro:** ningún RF depende de la fecha actual, así que ninguna función recibe "hoy". Si una spec futura agrega reglas por período (por ejemplo, mes fiscal), esas funciones lo recibirán por parámetro.

## 1. Archivos y responsabilidades

Rutas relativas a `src/main/java/com/proyecto/Coinciliador/`.

### Modelo (`model/`)
| Archivo | Responsabilidad | RF |
|---|---|---|
| `Lugar` (enum) | `PROPIO`, `CONTRAPARTE`, con su etiqueta para la vista. | RF-1 |
| `FormatoArchivo` (enum) | `CSV`, `EXCEL`. | RF-3, RF-21 |
| `MensajeCarga` (enum) | Catálogo M1–M8 con su texto exacto en español. Única fuente de los mensajes. | RF-4–10, RF-20, RF-23 |
| `ArchivoCargado` (record) | Nombre, bytes, formato, huella SHA-256, hojas listables (si es Excel) y configuración CSV detectada (separador, codificación). Los bytes se guardan para cambiar de hoja sin volver a subir. | RF-11, RF-12, RF-17, RF-20 |
| `Resumen` (record) | Nombre con extensión, hoja usada (o vacío si es CSV), cantidad de filas con datos, vista previa (lista de filas de texto ya recortadas). | RF-18 |
| `EstadoLugar` (interfaz sellada) | Cuatro estados: `Vacio`, `Rechazado(nombreIntentado, mensaje)`, `PendienteDeHoja(archivo)`, `Valido(archivo, resumen)`. | RF-13, RF-15, RF-16, RF-18 |
| `Carga` (clase) | Estado de Propio y Contraparte + marca `confirmada`. Cualquier cambio de un lugar borra la confirmación. | RF-1, RF-2, RF-22, RF-23 |

### Repositorio (`repository/`)
| Archivo | Responsabilidad | RF |
|---|---|---|
| `ICargaRepository` | Contrato: obtener la carga, guardar el estado de un lugar, marcar confirmada. | RF-2 |
| `CargaEnMemoriaRepository` | Única instancia (singleton de Spring) que guarda la `Carga` en memoria, con métodos sincronizados. Sin disco. | RF-2, RNF-1, RNF-2 |

### Servicios (`service/`)
| Archivo | Responsabilidad | RF |
|---|---|---|
| `ICargaService` / `CargaService` | Orquesta los casos de uso: subir archivo a un lugar, registrar un rechazo, elegir hoja, confirmar, armar la pantalla. Traduce `ArchivoRechazadoException` en estado `Rechazado` del lugar afectado sin tocar el otro. | RF-2, RF-13, RF-16, RF-17, RF-19, RF-22, RF-23 |
| `IAnalizadorArchivoService` / `AnalizadorArchivoService` | Según la extensión, delega al lector de CSV o de Excel y devuelve el nuevo `EstadoLugar`. | RF-3, RF-21 |
| `ILectorCsvService` / `LectorCsvService` | Lee un CSV: texto o binario, codificación, BOM, separador, parseo con Commons CSV, conteo y vista previa. | RF-6, RF-7, RF-8, RF-12 |
| `ILectorExcelService` / `LectorExcelService` | Abre el Excel por contenido con POI, detecta cifrado o daño, calcula hojas listables y arma el resumen de una hoja. | RF-6, RF-7, RF-9, RF-10, RF-11, RF-14, RF-15 |

### Controlador (`controller/`)
| Archivo | Responsabilidad | RF |
|---|---|---|
| `CargaController` | Solo orquesta (regla 3). `GET /` muestra la pantalla; `POST /carga/{lugar}` sube; `POST /carga/{lugar}/hoja` elige hoja; `POST /carga/confirmar` confirma. Si Bean Validation falla, pide al service que registre el rechazo. Todo `POST` redirige a `GET /` (patrón PRG). | RF-1, RF-4, RF-5, RF-13, RF-17, RF-19, RF-23, RNF-2 |

### DTOs (`dto/`) y mapper (`mapper/`)
| Archivo | Responsabilidad | RF |
|---|---|---|
| `SubidaArchivoForm` | Formulario de subida: el archivo, anotado con `@ExtensionPermitida` y `@TamanoMaximo`. | RF-3, RF-4, RF-5 |
| `ElegirHojaForm` | Nombre de la hoja elegida, `@NotBlank`. | RF-15, RF-17 |
| `PantallaCargaDto` / `LugarVistaDto` | Todo lo que la plantilla necesita ya calculado: textos, banderas (`mostrarSelectorHoja`, `confirmarHabilitado`, `advertenciaMismoArchivo`), filas de la vista previa. La plantilla no decide nada. | RF-18, RF-20, RF-22, regla 3 |
| `PantallaCargaMapper` | Convierte `Carga` en `PantallaCargaDto`. Incluye la comparación de huellas para M7 y el texto de "qué falta". | RF-20, RF-22 |

### Excepciones (`exception/`)
| Archivo | Responsabilidad | RF |
|---|---|---|
| `ArchivoRechazadoException` | Lleva un `MensajeCarga`. La lanzan los lectores; la atrapa `CargaService`. | RF-4–10 |
| `ManejadorGlobalExcepciones` (`@ControllerAdvice`) | Atrapa `MaxUploadSizeExceededException` (archivo por encima del límite técnico de 30 MB), deduce el lugar de la URL y registra M2 en ese lugar. | RF-5, RF-13 |

### Helpers (`helper/`) — funciones puras, sin estado
| Archivo | Responsabilidad | RF |
|---|---|---|
| `ExtensionHelper` | Extensión en minúsculas tras el último punto; si está permitida. | RF-3, RF-4 |
| `CodificacionHelper` | Si los bytes son texto; detectar UTF-8 o Windows-1252; quitar BOM. | RF-8, RF-12 |
| `SeparadorHelper` | Detectar `;`, `,` o tabulación en las primeras líneas. | RF-12 |
| `ContenidoHelper` | Si una celda o una fila tiene datos. | Definiciones, RF-6 |
| `VistaPreviaHelper` | Recortar a 10 filas, 20 columnas y 50 caracteres por celda. | RF-18 |
| `AcumuladorFilas` | Recibe filas una por una: cuenta las que tienen datos, guarda las 10 primeras y corta al pasar 100.000. | RF-6, RF-7, RF-18 |
| `HuellaHelper` | SHA-256 de los bytes, en hexadecimal. | RF-20 |

### Utils (`utils/validacion/`)
| Archivo | Responsabilidad | RF |
|---|---|---|
| `ExtensionPermitida` + `ExtensionPermitidaValidator` | Restricción de Bean Validation sobre el archivo subido; usa `ExtensionHelper`. Mensaje M1. | RF-3, RF-4, regla 5 |
| `TamanoMaximo` + `TamanoMaximoValidator` | Restricción de Bean Validation: tamaño ≤ 10.485.760 bytes. Mensaje M2. | RF-5, regla 5 |

### Recursos
| Archivo | Responsabilidad | RF |
|---|---|---|
| `src/main/resources/templates/carga.html` | Página única con las dos tarjetas, la advertencia y el botón Confirmar. Sin lógica de negocio. | RF-1, RF-13, RF-15, RF-18, RF-22, RF-23, RNF-4 |
| `src/main/resources/static/css/carga.css` | Estilos locales, sin CDN ni fuentes externas. | RNF-5 |
| `src/main/resources/application.properties` | Límites de multipart y umbral de disco (ver D4). | RF-5, RNF-1 |
| `README.md` (sección Módulos) | Una línea por clase creada (regla de `CLAUDE.md`). | — |

## 2. Funciones puras de lógica

Ninguna recibe "hoy" (ver §0). Todas son deterministas y sin efectos secundarios, testeables sin Spring.

| Función | Entrada → Salida | RF |
|---|---|---|
| `ExtensionHelper.extension` | nombre → extensión en minúsculas, o vacío si no hay punto o termina en punto | RF-3 |
| `ExtensionHelper.esPermitida` | nombre → verdadero si la extensión es `csv`, `xlsx` o `xls` | RF-3, RF-4 |
| `CodificacionHelper.esTexto` | bytes → falso si contiene algún byte 0x00 | RF-8 |
| `CodificacionHelper.detectar` | bytes → UTF-8 si decodifica sin errores en modo estricto; si no, Windows-1252 | RF-12 |
| `CodificacionHelper.quitarBom` | texto → texto sin `﻿` inicial | Def. celda con datos |
| `SeparadorHelper.detectar` | primeras líneas → `;`, `,` o tab | RF-12 |
| `ContenidoHelper.celdaConDatos` | texto → verdadero si tiene un carácter distinto de espacio | Definiciones |
| `ContenidoHelper.filaConDatos` | lista de celdas → verdadero si alguna tiene datos | Definiciones |
| `VistaPreviaHelper.recortar` | filas → como mucho 10 filas × 20 columnas, celdas de hasta 50 caracteres (si se recorta, los últimos son "…" dentro de los 50) | RF-18 |
| `AcumuladorFilas.agregar` | fila → cuenta o ignora; error M4 al llegar a 100.001 | RF-7 |
| `HuellaHelper.sha256` | bytes → hexadecimal | RF-20 |

## 3. Algoritmos (pseudocódigo)

### 3.1 Subir un archivo a un lugar (RF-1–13, RF-19, RF-21)
```
controlador.subir(lugar, form, erroresDeValidacion):
    si erroresDeValidacion:                       # M1 o M2 por Bean Validation
        servicio.registrarRechazo(lugar, nombre, mensajeDelError)
    si no:
        servicio.subir(lugar, nombre, bytes)
    redirigir a "/"

servicio.subir(lugar, nombre, bytes):
    intentar:
        estado = analizador.analizar(nombre, bytes)    # Valido o PendienteDeHoja
    si ArchivoRechazadoException(m):
        estado = Rechazado(nombre, m)
    repositorio.guardar(lugar, estado)                 # reemplaza solo ese lugar (RF-13, RF-19)
    repositorio.desconfirmar()

analizador.analizar(nombre, bytes):
    si extension(nombre) == "csv": devolver lectorCsv.leer(nombre, bytes)
    si no:                         devolver lectorExcel.leer(nombre, bytes)
```

### 3.2 Leer un CSV (RF-6, RF-7, RF-8, RF-12)
```
lectorCsv.leer(nombre, bytes):
    si bytes vacío: rechazar M3
    si no esTexto(bytes): rechazar M5          # también cubre un Excel renombrado a .csv
    codificacion = detectar(bytes)
    texto = quitarBom(decodificar(bytes, codificacion))
    separador = SeparadorHelper.detectar(primeras 20 líneas con datos de texto)
    acumulador = nuevo AcumuladorFilas
    intentar:
        para cada registro de CommonsCSV(texto, separador):
            acumulador.agregar(celdas del registro)      # puede rechazar M4
    si error de formato (comillas sin cerrar): rechazar M5
    si acumulador.cantidad == 0: rechazar M3
    archivo = ArchivoCargado(nombre, bytes, CSV, sha256(bytes), separador, codificacion)
    devolver Valido(archivo, Resumen(nombre, sin hoja, acumulador.cantidad, recortar(acumulador.primeras)))
```

### 3.3 Leer un Excel (RF-6, RF-7, RF-9, RF-10, RF-11, RF-14, RF-15)
```
lectorExcel.leer(nombre, bytes):
    si bytes vacío: rechazar M3
    intentar:
        libro = WorkbookFactory.crear(bytes en memoria)   # detecta .xls/.xlsx por contenido (RF-11)
    si documento cifrado: rechazar M6
    si cualquier otro error de lectura o zip bomb: rechazar M5
    listables = [hoja para cada hoja del libro si visible(hoja) y tieneFilaConDatos(hoja)]
    si listables vacía: rechazar M3              # incluye "única hoja con datos oculta"
    archivo = ArchivoCargado(nombre, bytes, EXCEL, sha256(bytes), listables)
    si listables tiene 1: devolver Valido(archivo, resumenDeHoja(libro, listables[0]))   # RF-14
    si no:                devolver PendienteDeHoja(archivo)                              # RF-15

resumenDeHoja(libro, hoja):
    acumulador = nuevo AcumuladorFilas
    para cada fila existente de la hoja:                 # filas ocultas incluidas
        acumulador.agregar(celdas formateadas como en Excel)   # resultado de fórmula, #REF! tal cual
    devolver Resumen(nombre, hoja, acumulador.cantidad, recortar(acumulador.primeras))
```

### 3.4 Elegir o cambiar hoja (RF-16, RF-17)
```
servicio.elegirHoja(lugar, hoja):
    estado = repositorio.estado(lugar)
    si estado no es PendienteDeHoja ni Valido de Excel, o hoja no está en listables: ignorar
    libro = abrir de nuevo desde archivo.bytes         # no se vuelve a subir
    repositorio.guardar(lugar, Valido(archivo, resumenDeHoja(libro, hoja)))
    repositorio.desconfirmar()
```

### 3.5 Pantalla, mismo archivo y confirmación (RF-20, RF-22, RF-23)
```
mapper.aPantalla(carga):
    para cada lugar: LugarVistaDto según su EstadoLugar
    ambosValidos = Propio es Valido y Contraparte es Valido
    advertenciaM7 = ambos tienen archivo y sus huellas son iguales   # no bloquea
    confirmarHabilitado = ambosValidos
    textoFalta = lugares que no son Valido ("Falta el archivo propio", "La contraparte no es válida"...)
    mensajeFinal = M8 si carga.confirmada

servicio.confirmar():
    si ambos válidos: repositorio.confirmar()   # control también en el servidor, no solo en el botón
```

## 4. Cómo se pinta en la interfaz

Una sola página (`GET /`) renderizada en servidor. El diseño visual se hace con la skill `/impeccable` (regla de `CLAUDE.md`).

```
┌───────────────────────────── Carga de archivos ─────────────────────────────┐
│ ┌──────── Propio ────────┐          ┌──────── Contraparte ────────┐         │
│ │ [Elegir archivo][Subir]│          │ [Elegir archivo][Subir]     │         │
│ │ <estado del lugar>     │          │ <estado del lugar>          │         │
│ └────────────────────────┘          └─────────────────────────────┘         │
│ ⚠ M7 (si Propio y Contraparte son el mismo archivo)                          │
│ [Confirmar] (deshabilitado + "Falta: …")   →   ✓ M8 tras confirmar           │
└──────────────────────────────────────────────────────────────────────────────┘
```

Estados de cada tarjeta:
| Estado | Se muestra | RF |
|---|---|---|
| Vacío | "Todavía no cargaste un archivo." | RF-22 |
| Rechazado | Nombre intentado + mensaje M1–M6 en rojo, con `role="alert"`, dentro de la tarjeta del lugar | RF-13 |
| Pendiente de hoja | Lista de hojas listables como opciones sin preselección + botón "Usar esta hoja" | RF-15, RF-16 |
| Válido | Nombre con extensión, hoja usada con selector para cambiarla (si hay más de una listable), "N filas con datos", tabla de vista previa | RF-17, RF-18 |

- Cada tarjeta tiene su propio formulario `multipart/form-data` con `accept=".csv,.xlsx,.xls"`. El `accept` es solo ayuda: la validación real es la del servidor.
- Patrón PRG: después de cada `POST` se redirige a `GET /`. Refrescar no reenvía el archivo y muestra el estado guardado (RNF-2).
- Textos en español (RNF-4). CSS y recursos locales, sin CDN (RNF-5).

## 5. Configuración (`application.properties`)
| Propiedad | Valor | Motivo |
|---|---|---|
| `spring.servlet.multipart.max-file-size` | `30MB` | Límite técnico. El límite de negocio (10.485.760 bytes) lo aplica `@TamanoMaximo` para poder indicar el lugar (RF-13) y testear el borde exacto (RF-5). El default de Spring es 1MB. |
| `spring.servlet.multipart.max-request-size` | `30MB` | Un archivo por request. |
| `spring.servlet.multipart.file-size-threshold` | `31MB` | Por encima del máximo: el archivo nunca se escribe en disco (RNF-1). El default es `0B`, que escribe todo a un temporal. |

## 6. Decisiones técnicas

| # | Decisión | Alternativa descartada y por qué |
|---|---|---|
| D1 | Estado en un repositorio singleton en memoria. | `HttpSession`: expira a los 30 min por defecto y depende de la cookie. Para una app local de un solo usuario el singleton cumple RNF-2 (refrescar y dos pestañas comparten) sin configurar nada. |
| D2 | Guardar los bytes del archivo en memoria (máximo 2 × 10 MB). | Guardar solo el resumen: RF-17 obligaría a volver a subir para cambiar de hoja. |
| D3 | Extensión y tamaño con Bean Validation (restricciones propias); vacío, legibilidad, cifrado y filas en los lectores con `ArchivoRechazadoException`. | Todo en el service: incumple la regla 5. Todo en restricciones: una restricción que abre el Excel duplicaría el parseo y metería lógica pesada en la validación. |
| D4 | Multipart a 30 MB, umbral de disco a 31 MB y límite de negocio en `@TamanoMaximo`. | `max-file-size=10MB`: Spring corta antes del controlador con un error genérico y es más difícil indicar el lugar y testear el byte exacto. |
| D5 | `WorkbookFactory` detecta `.xls`/`.xlsx` por contenido. | Elegir `HSSFWorkbook`/`XSSFWorkbook` por extensión: rechazaría los archivos renombrados (RF-11). |
| D6 | Codificación: UTF-8 estricto y, si falla, Windows-1252. Texto = sin bytes 0x00. | Librería de detección (por ejemplo juniversalchardet): dependencia nueva (regla 1) para solo dos codificaciones. |
| D7 | Separador: contar `;`, `,` y tab fuera de comillas en las primeras 20 líneas con datos; gana el que aparece en la mayor cantidad de líneas con la misma cuenta; empate → `;` (lo usual en Argentina). | Preguntar al usuario: la spec pide detectar. |
| D8 | API normal de POI (carga la hoja en memoria). | API de streaming (SAX/eventos): mucho más código. Con 10 MB, máximo 100.000 filas y la protección anti zip bomb de POI (ratio mínimo 1 % por defecto) alcanza. Se revisa si un test con 100.000 filas supera tiempos razonables. **Revisado en T12 (2026-11-25):** 100.000 filas x 3 columnas tardaron de 6 a más de 10 s; se decidió mantener la API normal y subir el límite del test a 30 s. Un xlsx de 10 MB puede tardar bastante más: si molesta en el uso real, pasar a lectura en streaming. |
| D9 | Fórmulas: se muestra el resultado guardado en el archivo (lo que se vio en Excel al guardarlo). | Evaluar con `FormulaEvaluator`: más lento y falla con referencias a otros libros. |
| D10 | Formato de celdas con `DataFormatter` en locale `es-AR`. | Locale por defecto de la JVM: el resultado cambiaría según la PC. |
| D11 | Página única con PRG, sin JavaScript de lógica. | Interfaz con `fetch`/JS: más piezas y la lógica se iría a la vista (regla 3). |
| D12 | Archivos de prueba generados por código en los tests. | Binarios en `src/test/resources`: riesgo de colar datos reales (regla 5, RNF-6) y difíciles de revisar. |

## 7. Estrategia de tests

Runner: **JUnit 5** con `./mvnw test` (starters `*-test` ya presentes: `spring-boot-starter-webmvc-test` para MockMvc, AssertJ). Una sola clase: `./mvnw test -Dtest=NombreTest`.

Cada tarea empieza con su test en rojo (regla 4). Los archivos de prueba los genera `FabricaArchivosPrueba` (en `src/test/java/.../helper/`): CSV en UTF-8, Windows-1252, con BOM y con distintos separadores; Excel `.xlsx` y `.xls` con una o varias hojas, hojas ocultas, fórmulas, `#REF!`, celdas combinadas y libros cifrados con contraseña (POI `Encryptor` agile para `.xlsx`; `Biff8EncryptionKey` para `.xls`, a confirmar en la tarea). Todos con datos ficticios (RNF-6).

| Nivel | Qué se prueba | RF |
|---|---|---|
| Unitario puro | `ExtensionHelper` (mayúsculas, sin extensión, doble extensión, `.ods`, `.xlsm`) | RF-3, RF-4 |
| Unitario puro | `CodificacionHelper` (UTF-8, Windows-1252, byte 0x00, BOM) | RF-8, RF-12 |
| Unitario puro | `SeparadorHelper` (`;`, `,`, tab, separador dentro de comillas, empate) | RF-12 |
| Unitario puro | `ContenidoHelper`, `VistaPreviaHelper` (10 filas, 20 columnas, 50 caracteres), `AcumuladorFilas` (100.000 pasa, 100.001 rechaza M4), `HuellaHelper` | RF-6, RF-7, RF-18, RF-20 |
| Validación | `TamanoMaximoValidator` con 10.485.760 y 10.485.761 bytes; `ExtensionPermitidaValidator` | RF-4, RF-5 |
| Servicio | `LectorCsvService`: vacío, solo BOM, `;;;`, binario, Excel renombrado a `.csv`, comillas sin cerrar, filas de distinto largo | RF-6, RF-8, RF-12 |
| Servicio | `LectorExcelService`: una hoja, varias, oculta, gráfico, cifrado `.xlsx`/`.xls`, dañado, `.xlsx` renombrado a `.xls` y al revés, fórmulas, `#REF!`, combinadas, filas ocultas | RF-6, RF-9, RF-10, RF-11, RF-14, RF-15 |
| Servicio | `CargaService` + repositorio: rechazo conserva el otro lugar, reemplazo descarta la hoja, cambio de hoja sin resubir, mezcla CSV + Excel, mismo archivo, confirmar solo con dos válidos | RF-2, RF-13, RF-16, RF-17, RF-19, RF-20, RF-21, RF-22, RF-23 |
| Web (MockMvc) | `CargaController`: cada M1–M8 aparece en el HTML dentro de la tarjeta correcta, redirección PRG, botón Confirmar con `disabled`, archivo de más de 30 MB → M2 | RF-1, RF-4, RF-5, RF-13, RF-22, RF-23, RNF-2, RNF-4 |
| Configuración | Las propiedades de multipart cargadas cumplen umbral > máximo | RNF-1 |
| Logs | `OutputCaptureExtension` de Spring Boot: tras subir un archivo, ni el nombre ni su contenido aparecen en la salida | RNF-3 |
| Navegador (manual) | Diseño de tarjetas, tabla de vista previa, recortes visibles, funcionamiento con la red desactivada | RF-18 (presentación), RNF-5 |

El test existente `contextLoads` sigue pasando: no hay datasource que configurar.

## 8. Matriz de cobertura

| RF / RNF | Componentes | Test |
|---|---|---|
| RF-1 | `Lugar`, `CargaController`, `carga.html` | Web |
| RF-2 | `CargaEnMemoriaRepository`, `Carga` | Servicio |
| RF-3, RF-4 | `ExtensionHelper`, `@ExtensionPermitida` | Unitario, validación, web |
| RF-5 | `@TamanoMaximo`, `ManejadorGlobalExcepciones`, config | Validación, web |
| RF-6 | `AcumuladorFilas`, lectores | Servicio |
| RF-7 | `AcumuladorFilas` | Unitario |
| RF-8 | `CodificacionHelper`, `LectorCsvService` | Unitario, servicio |
| RF-9, RF-10, RF-11 | `LectorExcelService` | Servicio |
| RF-12 | `CodificacionHelper`, `SeparadorHelper`, `LectorCsvService` | Unitario, servicio |
| RF-13 | `CargaService`, `EstadoLugar.Rechazado`, `carga.html` | Servicio, web |
| RF-14, RF-15 | `LectorExcelService`, `PendienteDeHoja` | Servicio |
| RF-16, RF-17 | `CargaService.elegirHoja`, `ElegirHojaForm` | Servicio, web |
| RF-18 | `Resumen`, `VistaPreviaHelper`, `PantallaCargaMapper`, `carga.html` | Unitario, web, navegador |
| RF-19 | `CargaService.subir` | Servicio |
| RF-20 | `HuellaHelper`, `PantallaCargaMapper` | Unitario, servicio |
| RF-21 | `AnalizadorArchivoService` | Servicio |
| RF-22 | `PantallaCargaMapper`, `CargaService.confirmar` | Servicio, web |
| RF-23 | `Carga.confirmada`, `MensajeCarga.M8` | Servicio, web |
| RNF-1 | `CargaEnMemoriaRepository`, config multipart | Configuración |
| RNF-2 | Repositorio singleton + PRG | Web |
| RNF-3 | Sin logs de nombre ni contenido en ningún componente | Logs |
| RNF-4 | `MensajeCarga`, `carga.html` | Web |
| RNF-5 | `static/css`, sin CDN | Navegador |
| RNF-6 | `FabricaArchivosPrueba` | Todos |

## 9. Riesgos y avisos
- **Tamaño de la spec:** el plan crea unos 30 archivos. Si en `tasks.md` salen más de 10 tareas, la skill pide proponer dividir la spec (por ejemplo, 001a CSV y 001b Excel).
- **Memoria:** un `.xlsx` de 10 MB puede ocupar cientos de MB al abrirse con la API normal de POI. Si el test de 100.000 filas es lento o falla por memoria, se revisa D8.
- **APIs a confirmar en su tarea (con test):** la forma exacta de leer el resultado guardado de una fórmula (D9) y la creación de un `.xls` cifrado para los tests.
- **Regla 5 resuelta así:** Bean Validation donde la regla es un atributo del archivo (extensión, tamaño); los rechazos que necesitan leer el contenido usan una excepción de dominio. Si preferís otra interpretación, conviene decidirlo antes de `tasks.md`.
