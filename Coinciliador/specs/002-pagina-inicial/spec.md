# Spec 002 — Página inicial
Estado: implementada

## Contexto y objetivo
El contador hace distintas conciliaciones (IVA, Ventas, Compras, Ingresos Brutos) y hoy la aplicación no le permite decir cuál quiere hacer. Esta spec cubre solo la página inicial: mostrar los tipos de conciliación disponibles, dejar elegir uno y llevar a la carga de archivos de la spec 001 con ese tipo a la vista. Las reglas propias de cada tipo se definen en specs posteriores.

Relación con la spec 001: la pantalla de carga deja de ser la primera pantalla de la aplicación y pasa a abrirse desde la página inicial. La carga sigue siendo una sola a la vez (RF-2 de la spec 001).

## Usuarios
- Contador que usa la aplicación en su propia computadora. Un solo usuario, sin conocimientos técnicos.

## Historias de usuario
- HU-1. Como contador, quiero ver los tipos de conciliación disponibles al abrir la aplicación, para elegir el que necesito.
- HU-2. Como contador, quiero una explicación corta de cada tipo, para no equivocarme de conciliación.
- HU-3. Como contador, quiero volver a la página inicial desde la carga sin perder mi trabajo, para revisar mi elección.
- HU-4. Como contador, quiero que me avisen antes de perder archivos ya subidos al cambiar de tipo, para no repetir la carga por error.

## Definiciones
- **Tipo de conciliación:** cada una de las cuatro conciliaciones que ofrece la aplicación: IVA, Ventas, Compras e Ingresos Brutos. La lista es fija; el contador no la edita.
- **Tipo activo:** el tipo de conciliación al que pertenece la carga actual.
- **Carga en curso:** carga en la que al menos un lugar tiene un archivo aceptado (aunque falte elegir hoja) o que ya fue confirmada. Un lugar vacío o con un archivo rechazado no cuenta.

## Requisitos funcionales
- RF-1: CUANDO el contador abre la aplicación, EL SISTEMA muestra la página inicial como primera pantalla.
- RF-2: EL SISTEMA muestra en la página inicial una opción por cada tipo de conciliación, en este orden: IVA, Ventas, Compras, Ingresos Brutos.
- RF-3: EL SISTEMA muestra en cada opción el nombre del tipo y una frase de ayuda que dice qué se concilia (ver catálogo de textos).
- RF-4: CUANDO el contador elige un tipo y no hay carga en curso, EL SISTEMA abre la pantalla de carga con ese tipo como tipo activo.
- RF-5: MIENTRAS haya un tipo activo, EL SISTEMA muestra su nombre como título de la pantalla de carga.
- RF-6: CUANDO el contador elige el tipo activo y hay carga en curso, EL SISTEMA abre la pantalla de carga tal como estaba.
- RF-7: CUANDO el contador elige un tipo distinto del activo y hay carga en curso, EL SISTEMA le pide confirmar y le avisa que se descartarán los archivos subidos.
- RF-8: CUANDO el contador confirma el descarte, EL SISTEMA descarta la carga y abre una pantalla de carga vacía del tipo elegido.
- RF-9: CUANDO el contador cancela el descarte, EL SISTEMA conserva la carga y el tipo activo, y deja al contador en la página inicial.
- RF-10: EL SISTEMA ofrece en la pantalla de carga un enlace para volver a la página inicial, sin descartar la carga.
- RF-11: SI se intenta abrir la carga con un tipo que no existe, ENTONCES EL SISTEMA muestra la página inicial con el mensaje P1 y no modifica la carga en curso.

### Catálogo de textos
| Código | Texto |
|---|---|
| P1 | Ese tipo de conciliación no existe. Elegí uno de la lista. |
| A-IVA | Cruzá tu libro de IVA con lo informado por AFIP. |
| A-VEN | Cruzá tus registros de ventas con los de tus clientes. |
| A-COM | Cruzá tus registros de compras con los de tus proveedores. |
| A-IIBB | Cruzá tus registros de Ingresos Brutos con los del organismo recaudador. |
| D1 | Si cambiás de tipo se descartan los archivos que ya subiste. ¿Querés continuar? |

## Requisitos no funcionales
- RNF-1: EL SISTEMA muestra la página inicial sin necesitar conexión a internet.
- RNF-2: EL SISTEMA permite elegir un tipo y responder la confirmación D1 usando solo el teclado.
- RNF-3: EL SISTEMA muestra todos los textos en español.

## Casos límite
- Carga en curso del mismo tipo y el contador vuelve a elegirlo: sigue donde estaba, sin aviso (RF-6).
- Carga con un solo archivo aceptado y otro lugar vacío: cuenta como carga en curso, así que cambiar de tipo pide confirmación.
- Carga con un archivo rechazado y el otro lugar vacío: no es carga en curso, así que cambiar de tipo no pide confirmación.
- Carga ya confirmada (M8 de la spec 001): cuenta como carga en curso, así que cambiar de tipo pide confirmación.
- Se abre la carga sin tipo y sin carga en curso: el contador vuelve a la página inicial sin mensaje.
- Se abre la carga sin tipo y con carga en curso: se muestra la carga en curso con su tipo activo.

## Fuera de alcance
- Reglas, columnas o criterios de cruce propios de cada tipo.
- Historial de conciliaciones anteriores y cualquier dato guardado entre cierres de la aplicación.
- Un acceso directo a la carga en curso desde la página inicial.
- Agregar, quitar, renombrar o reordenar tipos desde la aplicación.
- Usuarios, inicio de sesión o varias cargas a la vez.

## Criterios de finalización
- La página inicial muestra los cuatro tipos con su frase de ayuda y funciona sin conexión.
- Elegir un tipo sin carga en curso abre la carga con el título del tipo.
- Con carga en curso, elegir el mismo tipo vuelve a la carga intacta; elegir otro pide confirmar y, según la respuesta, descarta o conserva.
- Desde la carga se puede volver al inicio sin perder archivos.
- Un tipo inexistente muestra P1 en la página inicial.
- Todo el flujo se puede recorrer con teclado.
- Las tareas visuales se verifican en el navegador y las de datos con test (regla 4 de la constitución).

## Dudas abiertas
- Resuelta: las frases de ayuda A-IVA, A-VEN, A-COM y A-IIBB quedan como están; se pueden retocar después en el catálogo de textos.
- Resuelta: la página inicial es la primera pantalla porque el proyecto puede escalar. El plan de la spec 001, que asume que la carga es la primera pantalla, se ajusta al planificar la 002.
