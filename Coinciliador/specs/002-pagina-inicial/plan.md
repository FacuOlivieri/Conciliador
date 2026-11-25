# Plan 002 — Página inicial
Spec: `specs/002-pagina-inicial/spec.md` (Estado: aprobada, sin dudas abiertas)
Estado del plan: aprobado

## 0. Precondiciones verificadas
- Spec aprobada, 11 RF y 3 RNF. Las dos dudas abiertas quedaron resueltas.
- Constitución vigente: regla 1 (stack mínimo), regla 3 (lógica fuera de la vista), regla 4 (tests primero; lo solo visual se verifica en el navegador), regla 5 (toda entrada con Bean Validation), regla 6 (español).
- No hace falta ninguna dependencia nueva: todo sale de Spring MVC, Thymeleaf y Bean Validation, que ya están en el `pom.xml`.
- Paquete base: `com.proyecto.Coinciliador`. Los enums viven en `enums/` (no en `model/`, como decía el plan 001: así quedó tras T2).
- **"Hoy" como parámetro:** ningún RF depende de la fecha. Ninguna función recibe "hoy".
- **Dependencia con la spec 001:** esta spec toca el estado de la carga (tipo activo, descarte) y la ruta de la pantalla de carga. Esas piezas las crean las tareas T13–T19 de la 001, que todavía no están hechas. Por eso el plan se parte en dos tramos (ver §8).

## 1. Archivos y responsabilidades

Rutas relativas a `src/main/java/com/proyecto/Coinciliador/`. **N** = archivo nuevo; **M** = archivo de la spec 001 que se modifica.

### Enums (`enums/`)
| Archivo | Responsabilidad | RF |
|---|---|---|
| **N** `TipoConciliacion` | `IVA`, `VENTAS`, `COMPRAS`, `INGRESOS_BRUTOS`, en ese orden. Cada uno lleva su nombre visible, su *slug* para la URL (`iva`, `ventas`, `compras`, `ingresos-brutos`) y su frase de ayuda (A-IVA, A-VEN, A-COM, A-IIBB). Única fuente de la lista de tipos. `deSlug(texto)` devuelve el tipo o vacío. | RF-2, RF-3, RF-11 |
| **N** `MensajeInicio` | Catálogo P1 y D1 con su texto exacto. Mismo patrón que `MensajeCarga`. | RF-7, RF-11, RNF-3 |
| **N** `ResultadoEleccion` | `ABRIR_CARGA` o `PEDIR_CONFIRMACION`. Lo devuelve la función pura de decisión. | RF-4, RF-6, RF-7 |

### Modelo (`model/`)
| Archivo | Responsabilidad | RF |
|---|---|---|
| **M** `Carga` (T13 de la 001) | Suma el campo `tipo` (puede ser nulo antes de elegir) y la operación `reiniciar(tipo)`: deja ambos lugares en `Vacio`, borra la confirmación y fija el tipo. | RF-4, RF-5, RF-8 |

### Repositorio (`repository/`)
| Archivo | Responsabilidad | RF |
|---|---|---|
| **M** `ICargaRepository` / `CargaEnMemoriaRepository` (T13 de la 001) | Dos operaciones nuevas: `tipoActivo()` y `reiniciar(tipo)`. Siguen sincronizadas y en memoria. | RF-4, RF-6, RF-8, RF-9 |

### Helpers (`helper/`) — funciones puras, sin estado
| Archivo | Responsabilidad | RF |
|---|---|---|
| **N** `CargaEnCursoHelper` | Dado el estado de Propio, el de Contraparte y la marca de confirmada, dice si hay carga en curso (definición de la spec). | RF-6, RF-7, RF-9 |
| **N** `EleccionTipoHelper` | Dado el tipo activo (o nada), si hay carga en curso y el tipo elegido, devuelve `ResultadoEleccion`. | RF-4, RF-6, RF-7 |

### Servicios (`service/`)
| Archivo | Responsabilidad | RF |
|---|---|---|
| **N** `IConciliacionService` / `ConciliacionService` | Orquesta los casos de uso de esta spec: `elegir(tipo)` (decide con `EleccionTipoHelper` y, si corresponde, fija el tipo), `descartarYAbrir(tipo)` y `tipoActivo()`. No lee archivos ni toca `CargaService`. | RF-4, RF-6, RF-7, RF-8, RF-9 |

### Controlador (`controller/`)
| Archivo | Responsabilidad | RF |
|---|---|---|
| **N** `InicioController` | Solo orquesta (regla 3). Rutas en §3. Redirige siempre después de un `POST` (PRG). Si Bean Validation rechaza el tipo, redirige a `/` con el mensaje P1. | RF-1, RF-4, RF-6, RF-7, RF-8, RF-9, RF-11 |
| **M** `CargaController` (T18–T19 de la 001) | `GET /` pasa a ser `GET /carga`; todos los `POST` redirigen a `/carga`. Si no hay tipo activo, `GET /carga` redirige a `/` sin mensaje. | RF-1, RF-4, caso límite "sin tipo" |

### DTOs (`dto/`) y mapper (`mapper/`)
| Archivo | Responsabilidad | RF |
|---|---|---|
| **N** `EleccionTipoForm` | Recibe el *slug* de la URL, anotado con `@TipoConciliacionValido`. | RF-11, regla 5 |
| **N** `InicioDto` / `TipoVistaDto` | Todo lo que `inicio.html` necesita ya listo: lista de tarjetas (slug, nombre, ayuda) y el mensaje P1 si viene. | RF-2, RF-3, RF-11 |
| **N** `ConfirmacionCambioDto` | Para `confirmar-cambio.html`: tipo activo, tipo elegido (nombre y slug) y el texto D1. | RF-7 |
| **N** `InicioMapper` | Convierte `TipoConciliacion` en `InicioDto` y `ConfirmacionCambioDto`. | RF-2, RF-3, RF-7 |
| **M** `PantallaCargaDto` / `PantallaCargaMapper` (T16 de la 001) | Suman `tituloTipo` (nombre del tipo activo). | RF-5 |

### Utils (`utils/validacion/`)
| Archivo | Responsabilidad | RF |
|---|---|---|
| **N** `TipoConciliacionValido` + `TipoConciliacionValidoValidator` | Restricción de Bean Validation: el texto es el *slug* de un tipo existente. Usa `TipoConciliacion.deSlug`. Mensaje P1. | RF-11, regla 5 |

### Recursos
| Archivo | Responsabilidad | RF |
|---|---|---|
| **N** `templates/inicio.html` | Página inicial: título, las cuatro tarjetas (cada una es un formulario con un botón) y el mensaje P1 con `role="alert"`. Sin lógica de negocio. | RF-1, RF-2, RF-3, RF-11, RNF-2, RNF-3 |
| **N** `templates/confirmar-cambio.html` | Pantalla de confirmación D1 con "Descartar y continuar" (botón) y "Cancelar" (enlace a `/`). | RF-7, RF-8, RF-9, RNF-2 |
| **M** `templates/carga.html` (T18 y T21 de la 001) | Muestra `tituloTipo` como título y un enlace "Volver al inicio" a `/`. | RF-5, RF-10 |
| **N** `static/css/inicio.css` | Estilos locales, sin CDN ni fuentes externas. | RNF-1 |
| `README.md` (sección Módulos) | Una línea por clase creada (regla de `CLAUDE.md`). | — |

## 2. Funciones puras de lógica

Ninguna recibe "hoy" (ver §0). Deterministas y sin efectos secundarios, probadas sin Spring.

| Función | Entrada → Salida | RF |
|---|---|---|
| `TipoConciliacion.deSlug` | texto → el tipo con ese *slug* (sin distinguir mayúsculas) o vacío si es nulo, en blanco o desconocido | RF-11 |
| `CargaEnCursoHelper.hayCargaEnCurso` | (estado de Propio, estado de Contraparte, confirmada) → verdadero si alguno es `Valido` o `PendienteDeHoja`, o si está confirmada. `Vacio` y `Rechazado` no cuentan | RF-6, RF-7 |
| `EleccionTipoHelper.decidir` | (tipo activo o nada, hay carga en curso, tipo elegido) → `PEDIR_CONFIRMACION` solo si hay carga en curso, hay tipo activo y es distinto del elegido; si no, `ABRIR_CARGA` | RF-4, RF-6, RF-7 |

## 3. Algoritmos (pseudocódigo)

### 3.1 Rutas
| Ruta | Qué hace | RF |
|---|---|---|
| `GET /` | Muestra `inicio.html`. Si llega el mensaje P1 por redirección, lo muestra. | RF-1, RF-2, RF-3, RF-11 |
| `POST /conciliacion/{slug}` | Elegir tipo (§3.2). | RF-4, RF-6, RF-7, RF-11 |
| `GET /conciliacion/{slug}/confirmar` | Muestra D1 si de verdad hace falta confirmar; si no, redirige a `/carga` o a `/` (§3.3). | RF-7 |
| `POST /conciliacion/{slug}/descartar` | Descarta y abre la carga vacía (§3.4). | RF-8 |
| Cancelar | Enlace `GET /`. No cambia nada. | RF-9 |
| `GET /carga` | Pantalla de la spec 001. Si no hay tipo activo, redirige a `/` sin mensaje. | RF-5, RF-10 |

### 3.2 Elegir un tipo (RF-4, RF-6, RF-7, RF-11)
```
controlador.elegir(form, errores):
    si errores:                                   # el slug no es un tipo existente
        redirigir a "/" con mensaje P1            # no se toca la carga
    tipo = TipoConciliacion.deSlug(form.slug)
    resultado = servicio.elegir(tipo)
    si resultado == ABRIR_CARGA:       redirigir a "/carga"
    si no:                             redirigir a "/conciliacion/" + slug + "/confirmar"

servicio.elegir(tipo):
    resultado = EleccionTipoHelper.decidir(repositorio.tipoActivo(),
                    CargaEnCursoHelper.hayCargaEnCurso(estados de la carga), tipo)
    si resultado == ABRIR_CARGA y tipo != tipoActivo:
        repositorio.reiniciar(tipo)               # sin carga en curso: carga vacía del tipo nuevo
    devolver resultado
```
Con el mismo tipo y carga en curso, `ABRIR_CARGA` no reinicia: la carga queda como estaba (RF-6).

### 3.3 Confirmar el cambio (RF-7, RF-9)
```
controlador.confirmar(form, errores):
    si errores: redirigir a "/" con P1
    tipo = deSlug(form.slug)
    si servicio.decidir(tipo) == PEDIR_CONFIRMACION:
        mostrar confirmar-cambio.html con ConfirmacionCambioDto(tipoActivo, tipo, D1)
    si no:
        redirigir a "/carga" si hay tipo activo, o a "/" si no   # por si abren el enlace a mano
```
Cancelar es un enlace a `/`: la carga y el tipo activo no se tocan (RF-9).

### 3.4 Descartar y abrir (RF-8)
```
controlador.descartar(form, errores):
    si errores: redirigir a "/" con P1
    servicio.descartarYAbrir(deSlug(form.slug))
    redirigir a "/carga"

servicio.descartarYAbrir(tipo):
    repositorio.reiniciar(tipo)          # ambos lugares vacíos, sin confirmación, tipo nuevo
```
`descartarYAbrir` no vuelve a decidir: el botón solo existe en la pantalla D1 y reiniciar es idempotente.

### 3.5 Abrir la carga (RF-5, RF-10 y casos límite)
```
cargaController.mostrar():
    si repositorio.tipoActivo() es nada: redirigir a "/"        # sin mensaje
    pantalla = mapper.aPantalla(carga)                           # incluye tituloTipo
    mostrar carga.html
```

## 4. Cómo se pinta en la interfaz

El diseño visual se hace con la skill `/impeccable` (regla de `CLAUDE.md`). Ese proyecto todavía no tiene `PRODUCT.md` ni `DESIGN.md`: la tarea visual empieza con `/impeccable init` para fijar el contexto y evitar que la 001 y la 002 terminen con estilos distintos.

```
┌─────────────────────── Conciliador ────────────────────────┐
│  ¿Qué querés conciliar?                                    │
│  ⚠ P1 (solo si el tipo no existía)                          │
│ ┌── IVA ──────────┐ ┌── Ventas ───────┐                    │
│ │ Cruzá tu libro… │ │ Cruzá tus reg…  │                    │
│ └─────────────────┘ └─────────────────┘                    │
│ ┌── Compras ──────┐ ┌── Ingresos Brutos ┐                  │
│ │ Cruzá tus reg…  │ │ Cruzá tus reg…    │                  │
│ └─────────────────┘ └───────────────────┘                  │
└────────────────────────────────────────────────────────────┘

Confirmación (D1):
┌────────────────────────────────────────────────────────────┐
│ Pasar de IVA a Compras                                     │
│ Si cambiás de tipo se descartan los archivos que ya        │
│ subiste. ¿Querés continuar?                                │
│ [Cancelar]   [Descartar y continuar]                       │
└────────────────────────────────────────────────────────────┘
```

- Cada tarjeta es un `<form method="post">` con un `<button>`: se alcanza y se activa con teclado sin JavaScript (RNF-2).
- En la confirmación, "Cancelar" es el primer elemento enfocable y la acción que descarta es un botón aparte, para que Enter sin querer no borre nada (RF-7, RF-9).
- Sin JavaScript de lógica. CSS local, sin CDN ni fuentes externas (RNF-1). Todos los textos salen de los enums (RNF-3).
- En la pantalla de carga se agregan el título del tipo (RF-5) y el enlace "Volver al inicio", un `<a href="/">` que no envía nada y por eso no descarta la carga (RF-10).

## 5. Decisiones técnicas

| # | Decisión | Alternativa descartada y por qué |
|---|---|---|
| D1 | El tipo activo vive dentro de `Carga`. | Guardarlo en un repositorio aparte: habría dos fuentes de verdad y se podría descartar la carga sin cambiar el tipo, o al revés. |
| D2 | Elegir un tipo es un `POST` (cambia el estado) con patrón PRG. | `GET /conciliacion/iva`: un enlace guardado o un prefetch del navegador podría descartar la carga. |
| D3 | La confirmación es una pantalla propia renderizada en el servidor. | Diálogo con JavaScript (`confirm()`): lleva lógica a la vista, depende del navegador y no es testeable con MockMvc (decisión D11 de la 001). |
| D4 | El tipo viaja como *slug* en la URL (`ingresos-brutos`). | Número u orden de lista: cambiaría al reordenar tipos. Nombre visible con tildes: complica la URL. |
| D5 | Validar el *slug* con una restricción propia de Bean Validation (`@TipoConciliacionValido`), enlazada al `EleccionTipoForm` por la variable de la URL. | Comparar a mano en el controlador: incumple la regla 5. `@PathVariable` con un enum: un tipo inexistente da error 400 y no el mensaje P1 que pide RF-11. |
| D6 | P1 se muestra por redirección con atributo *flash*. | Mostrarlo en la respuesta del `POST`: refrescar volvería a enviar el formulario. Parámetro en la URL: el mensaje quedaría pegado al enlace. Límite aceptado: al refrescar `/` el mensaje desaparece. |
| D7 | La pantalla de carga pasa de `/` a `/carga`. | Dejar la carga en `/` y la página inicial en otra ruta: contradice RF-1 (primera pantalla). |
| D8 | Lista de tipos fija en un enum. | Tabla o archivo de configuración: el contador no edita la lista (spec, "Definiciones") y no hay persistencia. |
| D9 | "Carga en curso" es una función pura sobre los estados. | Preguntarle a `Carga`: ata la regla a una clase con estado y obliga a testear con Spring. |

## 6. Estrategia de tests

Runner: **JUnit 5** con `./mvnw test` (MockMvc y AssertJ ya disponibles). Una sola clase: `./mvnw test -Dtest=NombreTest`. Cada tarea empieza con su test en rojo (regla 4). Datos ficticios (RNF-6 de la 001).

| Nivel | Qué se prueba | RF |
|---|---|---|
| Unitario puro | `TipoConciliacionTest`: exactamente 4 tipos en orden IVA, Ventas, Compras, Ingresos Brutos; nombre y frase de ayuda iguales al catálogo; `deSlug` con mayúsculas, nulo, en blanco y desconocido | RF-2, RF-3, RF-11 |
| Unitario puro | `MensajeInicioTest`: P1 y D1 con el texto exacto de la spec | RF-7, RF-11, RNF-3 |
| Unitario puro | `CargaEnCursoHelperTest`: vacío+vacío → no; válido+vacío → sí; solo pendiente de hoja → sí; rechazado+vacío → no; rechazado+válido → sí; confirmada → sí | RF-6, RF-7 |
| Unitario puro | `EleccionTipoHelperTest`: sin tipo activo → abrir; mismo tipo con carga en curso → abrir; otro tipo sin carga en curso → abrir; otro tipo con carga en curso → pedir confirmación | RF-4, RF-6, RF-7 |
| Validación | `TipoConciliacionValidoValidatorTest`: `iva` válido, `IVA` válido, `monotributo` y en blanco → P1 | RF-11 |
| Servicio | `ConciliacionServiceTest` con el repositorio real en memoria: elegir sin carga fija el tipo; otro tipo con carga en curso pide confirmación y deja la carga intacta; mismo tipo la deja intacta; descartar deja ambos lugares vacíos con el tipo nuevo; cancelar (no llamar a nada) la conserva | RF-4, RF-6, RF-7, RF-8, RF-9 |
| Web (MockMvc) | `InicioControllerTest`: `GET /` muestra las 4 tarjetas en orden con su frase; `POST /conciliacion/iva` → redirige a `/carga`; slug inexistente → redirige a `/` y se ve P1 sin tocar la carga; con carga en curso, otro tipo → redirige a la confirmación que muestra D1; descartar → `/carga` vacía; la confirmación tiene un enlace "Cancelar" a `/`; `GET /carga` sin tipo → redirige a `/` sin mensaje | RF-1–RF-4, RF-6–RF-9, RF-11 |
| Web (MockMvc) | `CargaControllerTipoTest` (tras T18 de la 001): `/carga` muestra el nombre del tipo y el enlace `href="/"`; subir un archivo y volver al inicio con el mismo tipo conserva el archivo | RF-5, RF-6, RF-10 |
| Web (MockMvc) | Las páginas nuevas no contienen `http://` ni `https://` en `href`/`src` | RNF-1 |
| Navegador (manual) | Diseño de tarjetas y de la confirmación; recorrer todo con la tecla Tab y Enter, sin ratón; funcionamiento con la red desactivada | RF-3 (presentación), RNF-1, RNF-2 |

El test `contextLoads` sigue pasando: no hay datasource ni configuración nueva.

## 7. Matriz de cobertura

| RF / RNF | Componentes | Test |
|---|---|---|
| RF-1 | `InicioController` (`GET /`), `CargaController` (`/carga`), `inicio.html` | Web |
| RF-2, RF-3 | `TipoConciliacion`, `InicioMapper`, `inicio.html` | Unitario, web, navegador |
| RF-4 | `EleccionTipoHelper`, `ConciliacionService.elegir`, `Carga.reiniciar` | Unitario, servicio, web |
| RF-5 | `PantallaCargaDto.tituloTipo`, `carga.html` | Web |
| RF-6 | `CargaEnCursoHelper`, `EleccionTipoHelper`, `ConciliacionService.elegir` | Unitario, servicio, web |
| RF-7 | `EleccionTipoHelper`, `MensajeInicio.D1`, `confirmar-cambio.html` | Unitario, web |
| RF-8 | `ConciliacionService.descartarYAbrir`, `Carga.reiniciar` | Servicio, web |
| RF-9 | Enlace a `/` en `confirmar-cambio.html`; ningún estado se toca | Servicio, web |
| RF-10 | Enlace en `carga.html` | Web |
| RF-11 | `TipoConciliacionValido`, `MensajeInicio.P1`, redirección con *flash* | Validación, web |
| RNF-1 | `inicio.css`, sin recursos externos | Web, navegador |
| RNF-2 | Formularios y botones nativos, sin JavaScript | Navegador |
| RNF-3 | Textos en enums, plantillas en español | Unitario |

## 8. Orden de trabajo y relación con la spec 001

Esta spec se puede avanzar en el worktree del front mientras la otra sesión sigue con la 001, siempre que se respete la dependencia:

- **Tramo 1 — sin depender de la 001:** `TipoConciliacion`, `MensajeInicio`, `ResultadoEleccion`, `EleccionTipoHelper`, `CargaEnCursoHelper` (usa `EstadoLugar`, que ya existe), la restricción `@TipoConciliacionValido` y el DTO/mapper. Después, la plantilla `inicio.html` con su CSS, sin acción real todavía.
- **Tramo 2 — después de T13, T14 y T18 de la 001:** `Carga.reiniciar`, los métodos nuevos del repositorio, `ConciliacionService`, `InicioController` completo, y los cambios a `CargaController`, `PantallaCargaMapper` y `carga.html`. Las tareas visuales de la 001 (T21) y de la 002 conviene hacerlas juntas, con un solo `/impeccable init`.

### Ajustes que esta spec le pide al plan y a las tareas de la 001
No se editan los archivos de la 001 desde este worktree, para no chocar con la otra sesión. Hay que aplicarlos ahí o al unir las ramas:

| Dónde (001) | Cambio |
|---|---|
| `plan.md` §1 y §4 | La pantalla es `GET /carga` y no `GET /`; el patrón PRG redirige a `/carga`. |
| `plan.md` §1, `Carga` | Suma el campo `tipo` y `reiniciar(tipo)`. |
| `plan.md` §1, `PantallaCargaDto` | Suma `tituloTipo`. |
| `plan.md` §3.1 | "redirigir a `/carga`" en lugar de "`/`". |
| `tasks.md` T13, T16 | Probar `tipo` y `reiniciar`; probar `tituloTipo`. |
| `tasks.md` T18, T19 | Los tests usan `GET /carga` y las redirecciones van a `/carga`. |
| `plan.md` §4, regla de Confirmar y M8 | Sin cambios: siguen igual. |

## 9. Riesgos y avisos
- **Dos sesiones sobre las mismas piezas:** `Carga`, el repositorio, el controlador y `carga.html` los escribe la sesión de la 001. Si se hacen los cambios del tramo 2 antes de que existan, se pisan. Por eso el tramo 2 espera a T13/T14/T18.
- **Mensaje P1 con *flash*:** usa la sesión HTTP solo para ese aviso. Si falta la cookie, el mensaje no se ve, pero la carga no se ve afectada.
- **Tamaño:** el tramo 1 tiene unas 6 tareas y el tramo 2 unas 5. Pasa de 10 en total; se propone agruparlas en dos bloques, como en la 001, y no partir la spec.
- **Contexto de diseño:** sin `PRODUCT.md` ni `DESIGN.md`, la skill `/impeccable` pide `init` antes de diseñar. Es una decisión a tomar junto con la sesión de la 001.
