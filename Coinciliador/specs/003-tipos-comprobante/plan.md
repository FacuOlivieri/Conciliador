# Plan 003 — Tipos de comprobante
Spec: `specs/003-tipos-comprobante/spec.md` (Estado: aprobada, sin dudas abiertas)
Estado del plan: aprobado

## 0. Precondiciones verificadas
- La spec está aprobada, con 11 RF, 3 RNF y el anexo A aprobado (121 tipos y 125 abreviaturas).
- Reglas de la constitución que aplican:
  - Regla 1 (stack mínimo): no hace falta ninguna dependencia nueva. Todo sale del JDK y de Spring, que ya están en el `pom.xml`.
  - Regla 3 (lógica fuera de la vista): las reglas viven en Helper y Service.
  - Regla 4 (tests primero).
  - Regla 5 (entrada validada y nada a los logs).
  - Regla 6 (español).
- **"Hoy" como parámetro:** ningún RF depende de la fecha, así que ninguna función recibe "hoy".
- **Sin interfaz:** la spec no tiene pantalla. Lo que se construye lo van a usar las próximas specs de la conciliación de ventas.
- **Sin persistencia:** el catálogo viene dentro de la aplicación y se lee una sola vez, al arrancar (app local sin base de datos).
- Patrones del código que se reutilizan:
  - Interfaz sellada con records, como `model/EstadoLugar`.
  - Helpers `final` con métodos estáticos puros, como `helper/HuellaHelper`.
  - Pares `IXRepository` / `XRepository` y `IXService` / `XService`.
  - Tests con `@ParameterizedTest`, que ya se usan en `TipoConciliacionTest`.

## 1. Archivos y responsabilidades

Rutas relativas a `src/main/java/com/proyecto/Coinciliador/`. Todos los archivos son nuevos: no se modifica nada de las specs 001 y 002.

### Modelo (`model/`)
| Archivo | Responsabilidad | RF |
|---|---|---|
| `TipoComprobante` | Record con `codigo` (int), `nombreOficial` (texto tal como figura en ARCA) y `abreviaturas` (lista inmutable, puede estar vacía). | RF-1, RNF-2 |
| `CatalogoTiposComprobante` | Record inmutable con dos índices ya armados: código sin ceros adelante → tipo, y forma normalizada → lista de tipos. Expone `buscarCodigo(String)` y `buscarForma(String)`, que solo consultan los mapas. | RF-1, RF-3, RF-5, RF-9 |
| `ResultadoReconocimiento` | Interfaz sellada con `Reconocido(TipoComprobante tipo)` y `NoReconocido(String valorOriginal)`. | RF-11 |

### Helpers (`helper/`): funciones puras, sin estado
| Archivo | Responsabilidad | RF |
|---|---|---|
| `NormalizacionHelper` | `normalizar(texto)`: la forma normalizada de la spec. `recortar(texto)`: quita los espacios de los extremos, incluido el espacio duro. | RF-3, RF-5, RF-10 |
| `CatalogoHelper` | `deLinea(linea)`: convierte una línea del archivo del catálogo en `TipoComprobante`. `validar(tipos)`: devuelve la lista de errores de RF-2. `armar(tipos)`: valida y arma los índices; si hay errores, lanza `IllegalArgumentException`. | RF-1, RF-2, RF-9 |

### Repositorio (`repository/`)
| Archivo | Responsabilidad | RF |
|---|---|---|
| `ICatalogoTipoComprobanteRepository` | Un solo método, `catalogo()`. Por tener un solo método, en los tests se puede reemplazar con una lambda. | RF-1 |
| `CatalogoTipoComprobanteArchivoRepository` | `@Repository`. Al construirse, lee el catálogo del classpath en UTF-8, lo arma con `CatalogoHelper.armar` y lo guarda. Si el archivo falta o no cumple RF-2, lanza `IllegalStateException` y la aplicación no arranca. Tiene un constructor de paquete que recibe un `Reader`, para poder probarlo. | RF-1, RF-2, RNF-1, RNF-2 |

### Servicio (`service/`)
| Archivo | Responsabilidad | RF |
|---|---|---|
| `ITipoComprobanteService` / `TipoComprobanteService` | `reconocer(valor)` → `ResultadoReconocimiento`. Aplica el algoritmo de §3. No tiene logger y no pone el valor en mensajes de excepción. | RF-3 a RF-11, RNF-3 |

### Recursos y documentación
| Archivo | Responsabilidad | RF |
|---|---|---|
| `src/main/resources/catalogo/tipos-comprobante.tsv` | 121 líneas, una por tipo: `código⇥nombre oficial⇥abreviaturas separadas por "\|"`. Es una copia literal del anexo A, en UTF-8. | RF-1, RNF-1, RNF-2 |
| `README.md` (sección Módulos) | Una línea por cada clase nueva (regla de `CLAUDE.md`). | — |

## 2. Funciones puras de lógica

Son deterministas y no tienen efectos secundarios. Se prueban sin Spring y ninguna recibe "hoy".

| Función | Entrada → Salida | RF |
|---|---|---|
| `NormalizacionHelper.normalizar` | texto → minúsculas (`Locale.ROOT`), sin tildes ni diéresis pero con la ñ, todo lo que no sea `a-z`, `ñ` o `0-9` pasado a espacio, un solo espacio entre palabras y sin espacios en los extremos. Si recibe nulo, devuelve `""`. | RF-5, RF-10 |
| `NormalizacionHelper.recortar` | texto → el mismo texto sin los espacios de los extremos, contando también el espacio duro y los demás espacios Unicode. `String.strip()` no sirve para esto porque no quita el espacio duro. | RF-3, RF-4 |
| `CatalogoHelper.deLinea` | `"001⇥FACTURAS A⇥Factura A\|FC A"` → `TipoComprobante(1, "FACTURAS A", [Factura A, FC A])`. Si la tercera columna está vacía, la lista queda vacía. | RF-1 |
| `CatalogoHelper.validar` | lista de tipos → lista de errores. Busca: código repetido; nombre o abreviatura sin letras; abreviatura cuya forma normalizada esté en otro tipo, como abreviatura o como nombre oficial. Los nombres oficiales sí pueden repetirse entre tipos, porque RF-9 cubre ese caso. | RF-2 |
| `CatalogoHelper.armar` | lista de tipos → `CatalogoTiposComprobante`. Valida primero y después arma los dos índices. | RF-2, RF-5, RF-9 |

## 3. Algoritmo (pseudocódigo)

### 3.1 Cargar el catálogo al arrancar (RF-1, RF-2)
```
CatalogoTipoComprobanteArchivoRepository():
    lector = recurso "catalogo/tipos-comprobante.tsv" en UTF-8
    si no existe: lanzar IllegalStateException("falta el catálogo")
    tipos = cada línea no vacía → CatalogoHelper.deLinea
    catalogo = CatalogoHelper.armar(tipos)   # si no cumple RF-2: IllegalStateException con la lista de errores
                                             # (son datos del catálogo, no del usuario)

CatalogoHelper.armar(tipos):
    errores = validar(tipos); si hay errores: lanzar IllegalArgumentException
    porCodigo = { String.valueOf(t.codigo) → t }
    porForma  = { normalizar(t.nombreOficial) → [t, …] } + { normalizar(a) → [t] para cada abreviatura }
```

### 3.2 Reconocer un valor (RF-3 a RF-11)
Las comprobaciones van en este orden. La primera que corresponde decide el resultado.

```
reconocer(valor):
    original = valor ?? ""                                     # nulo se trata como vacío
    si original tiene más de 100 caracteres (code points): NoReconocido(original)     # RF-10
    v = recortar(original)

    si v coincide con ^[0-9]+$:                                # RF-3 / RF-6
        tipo = catalogo.buscarCodigo(sinCeros(v))              # "000" → "0", "001" → "1"
        devolver tipo ? Reconocido(tipo) : NoReconocido(original)

    si v coincide con ^([0-9]+)\s*[-–—]\s*(.+)$:               # RF-4 / RF-7 (espacios incluyen el duro)
        tipo = catalogo.buscarCodigo(sinCeros(grupo 1))
        si tipo existe y catalogo.buscarForma(normalizar(grupo 2)) contiene tipo:
            devolver Reconocido(tipo)
        devolver NoReconocido(original)

    forma = normalizar(original)
    si forma está vacía: NoReconocido(original)                # RF-10
    candidatos = catalogo.buscarForma(forma)
    si candidatos tiene exactamente 1: Reconocido(candidato)   # RF-5
    devolver NoReconocido(original)                            # 0 → RF-8, más de 1 → RF-9
```

En todos los casos el valor original se devuelve sin cambios (RF-11).

Cómo resuelve este algoritmo los casos límite de la spec:
- `" 1 "`: se recorta, queda solo con dígitos y entra por RF-3.
- `"0000…1"` con hasta 100 caracteres: se reconoce. El código se compara como texto sin ceros, así que no hay desborde.
- `"1.0"`, `"1,00"`, `"+1"`, `"-1"`, `"1A"` y `"1 -"` no tienen solo dígitos ni la forma código-guion-texto. Su forma normalizada no tiene letras, así que no coincide con nada y termina en RF-8.
- `"1 - Factura B"` entra por RF-4, pero "factura b" no es una forma de 001. Resultado: RF-7.
- `"FC A 0001-00001234"` no empieza con dígitos. Su forma normalizada no está en el catálogo: RF-8.
- `"-"` y `"..."` tienen una forma normalizada vacía: RF-10.

## 4. Cómo se pinta en la interfaz

No se pinta nada: la spec deja fuera de alcance mostrar el resultado. `ResultadoReconocimiento.NoReconocido` guarda el valor original para que una spec posterior lo muestre (HU-2).

## 5. Decisiones técnicas

| # | Decisión | Alternativa descartada y por qué |
|---|---|---|
| D1 | El catálogo es un archivo de recursos `.tsv` que se lee al arrancar. | **Un enum con 121 constantes:** mezcla datos con código, deja un archivo enorme y es difícil compararlo con el anexo. **Leer el `spec.md` en tiempo de ejecución:** la spec no viaja dentro del jar, y la documentación no debe ser una entrada del programa. |
| D2 | Separar por tabulación y por `\|` con `String.split`. | **Commons CSV (ya instalada):** el archivo es nuestro, sin comillas ni escapes, así que configurarla no aporta nada. Ni los nombres ni las abreviaturas del anexo tienen tabulaciones o `\|`. |
| D3 | RF-2 se valida al arrancar: si falla, la aplicación no arranca. Además lo cubre un test. | **Validarlo solo en un test:** un catálogo editado a mano después del build no se detectaría. **Validar en cada reconocimiento:** es un costo repetido sin ningún beneficio. |
| D4 | El índice de códigos usa como clave el texto sin ceros adelante (`"1"`, `"332"`). | **`Integer.parseInt`:** con 100 dígitos lanza `NumberFormatException`, y habría que atraparla para devolver RF-6. |
| D5 | El resultado es una interfaz sellada `Reconocido` / `NoReconocido`, con el mismo patrón que `EstadoLugar`. | **`Optional<TipoComprobante>`:** pierde el valor original que pide RF-11. **Lanzar una excepción:** un valor no reconocido es un resultado normal, no un error, y en un archivo de 100.000 filas sería caro. |
| D6 | El límite de 100 caracteres (RF-10) se aplica en el servicio y devuelve `NoReconocido`. | **`@Size` de Bean Validation sobre el parámetro:** lanzaría `ConstraintViolationException`, y RF-10 pide devolver un resultado. La regla 5 de la constitución ya se cumple en la entrada web: el archivo lo valida la spec 001 al subirlo. Este servicio recibe celdas de ese archivo ya aceptado. |
| D7 | Un test compara el `.tsv` con la tabla del anexo A, leyendo `specs/003-tipos-comprobante/spec.md`. Maven corre los tests desde `Coinciliador/`. | **Copiar la tabla dentro del test:** quedarían tres copias (spec, recurso y test) que se pueden desalinear sin que nadie lo note. |
| D8 | Para quitar tildes se usa `java.text.Normalizer` (NFD) y se borran las marcas, protegiendo antes la ñ. | **Una tabla propia de reemplazos (á→a…):** es más código y se pueden olvidar letras. |
| D9 | El catálogo se arma una vez y se guarda como record inmutable. El repositorio no necesita `synchronized`. | **Leer el archivo en cada consulta:** es lento y no hace falta, porque el catálogo es fijo (spec, "Catálogo"). |

## 6. Estrategia de tests

Runner: **JUnit 5** con `./mvnw test` (AssertJ y `@ParameterizedTest` ya están disponibles). Para correr una sola clase: `./mvnw test -Dtest=NombreTest`. Cada tarea empieza con su test en rojo (regla 4). Los valores de prueba son tipos de comprobante, que no son datos financieros reales.

| Nivel | Clase de test | Qué prueba | RF |
|---|---|---|---|
| Unitario puro | `NormalizacionHelperTest` | Que estos valores den `"fact a"`: `"Fact. A"`, `"FACT  A"`, `"fact-a"`, `"Fáct A"`, `"Fact.A"`, con tabulación y con espacio duro. Además: `"Caña"` → `"caña"`, `"N/C A"` → `"n c a"`, la raya `–` como separador, `"..."` → `""`, nulo → `""`, `"FC E"` ≠ `"FCE"`. Para `recortar`: espacio duro en los extremos. | RF-5, RF-10 |
| Unitario puro | `CatalogoHelperTest` | `deLinea` con y sin abreviaturas. `validar` encuentra cada error: código repetido, nombre sin letras, abreviatura sin letras, abreviatura repetida en otro tipo, abreviatura igual al nombre de otro tipo. Acepta nombres oficiales repetidos. `armar` lanza la excepción si hay errores y arma bien los índices, incluida una lista de 2 tipos para un nombre repetido. | RF-1, RF-2, RF-9 |
| Repositorio | `CatalogoTipoComprobanteArchivoRepositoryTest` | El catálogo real tiene 121 tipos. Coincide fila por fila con el anexo A de `spec.md` en código, nombre exacto (tildes y comillas “T” incluidas) y abreviaturas. Cumple RF-2. Un `Reader` inválido lanza `IllegalStateException`. | RF-1, RF-2, RNF-1, RNF-2 |
| Servicio | `TipoComprobanteServiceTest` | Con el catálogo real: un caso parametrizado por cada RF del 3 al 11 y por cada caso límite de la spec, con el resultado esperado y el valor original intacto. RF-9 usa un catálogo inventado de 2 tipos con el mismo nombre, pasado como lambda. | RF-3 a RF-11 |
| Servicio | `TipoComprobanteServiceTest` (con `OutputCaptureExtension`) | Reconocer un valor marcado (`"VALOR-SECRETO-123"`) no deja ese texto en la salida ni en los registros. | RNF-3 |
| Contexto | `CoinciliadorApplicationTests.contextLoads` (ya existe) | La aplicación arranca, lo que prueba que el recurso está en el classpath y cumple RF-2. | RF-1, RF-2 |

## 7. Matriz de cobertura

| RF / RNF | Componentes | Test |
|---|---|---|
| RF-1 | `tipos-comprobante.tsv`, `CatalogoHelper.deLinea`, repositorio | Repositorio (contra el anexo), contexto |
| RF-2 | `CatalogoHelper.validar` / `armar`, repositorio al arrancar | Unitario, repositorio, contexto |
| RF-3 | `recortar`, índice por código, `reconocer` | Servicio |
| RF-4 | Patrón código-guion-texto en `reconocer` | Servicio |
| RF-5 | `normalizar`, índice por forma | Unitario, servicio |
| RF-6, RF-7, RF-8 | Ramas de `reconocer` | Servicio |
| RF-9 | Índice por forma con varios tipos; `reconocer` | Unitario, servicio (catálogo inventado) |
| RF-10 | Largo máximo, forma vacía | Unitario, servicio |
| RF-11 | `ResultadoReconocimiento` | Servicio |
| RNF-1 | Catálogo en recursos, sin red | Repositorio, contexto |
| RNF-2 | `.tsv` en UTF-8 con los nombres literales | Repositorio (contra el anexo) |
| RNF-3 | Sin logger en el servicio ni en el repositorio | Servicio (captura de salida) |

## 8. Orden de trabajo
1. `NormalizacionHelper`: no depende de nada.
2. `TipoComprobante` y `CatalogoHelper`, con su modelo `CatalogoTiposComprobante`.
3. El `.tsv` y el repositorio, con el test contra el anexo A.
4. `ResultadoReconocimiento` y el servicio, por partes: primero los códigos (RF-3 y RF-6), después código con texto (RF-4 y RF-7), y al final nombres, abreviaturas y bordes (RF-5 y RF-8 a RF-11).
5. RNF-3 y el README.

Salen alrededor de 6 tareas, por debajo del límite de 10 de la skill, así que no hace falta partir la spec.

## 9. Riesgos y avisos
- **Vista previa de la spec 001:** el reconocimiento debe recibir el texto completo de la celda. Hoy el lector de la spec 001 solo guarda las 10 primeras filas (recortadas a 50 caracteres) para la vista previa. La spec que compare comprobantes va a tener que leer todas las filas sin recortar. No afecta a esta spec, pero conviene anotarlo para la próxima.
- **Celdas numéricas de Excel:** la spec define el valor de tipo como el texto que muestra la celda con su formato. El lector de la 001 usa `DataFormatter(es-AR)`, así que un 1 con formato `0` llega como `"1"`, y con formato `0,00` llega como `"1,00"` y no se reconoce. Es lo que pide la spec, pero puede sorprender con archivos reales.
- **Test contra el anexo:** depende de la ruta `specs/003-tipos-comprobante/spec.md`. Si la spec se mueve o cambia el formato de la tabla, el test falla. Es a propósito: obliga a mantener alineados la spec y el recurso.
- **Pendiente a futuro, fuera de esta spec: proteger el `.tsv` de ataques externos.** Pedido del usuario el 2026-11-26.
  - Hoy el archivo va dentro del jar y fuera de `static/`, así que la aplicación no lo publica por HTTP.
  - Lo único que se valida al arrancar es RF-2. Si alguien de afuera lograra reemplazarlo, por ejemplo tocando el jar o poniendo antes en el classpath otro archivo con el mismo nombre, un cambio que respete RF-2 pasaría sin aviso.
  - Está relacionado con el pendiente de Spring Security de `MEMORY.MD`: hoy la aplicación escucha en todas las interfaces de red.
  - Necesita su propia spec y su aprobación, y no se implementa en esta.
