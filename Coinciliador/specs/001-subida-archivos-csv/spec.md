# Spec 001 — Subida de archivos
Estado: aprobada

## Contexto y objetivo
El contador concilia información de dos fuentes: su propio registro (por ejemplo, el libro de IVA) contra uno externo (AFIP, banco, cliente). Antes de conciliar necesita cargar ambos archivos y confirmar que son los correctos. Esta spec cubre solo esa carga: recibir los dos archivos CSV o Excel, rechazar los que no sirven con un motivo claro, mostrar un resumen de cada uno y dejar la carga lista para conciliar. La conciliación y la descarga del resultado se definen en specs posteriores.

## Usuarios
- Contador que usa la aplicación en su propia computadora. Un solo usuario, sin conocimientos técnicos.

## Historias de usuario
- HU-1. Como contador, quiero subir mi archivo propio y el de la contraparte, en CSV o Excel, para poder conciliarlos después.
- HU-2. Como contador, quiero elegir la hoja de un Excel que tiene varias, para usar la que contiene los datos.
- HU-3. Como contador, quiero ver un resumen y las primeras filas de cada archivo para confirmar que subí los correctos.
- HU-4. Como contador, quiero saber por qué se rechazó un archivo para poder corregirlo.

## Definiciones
- **Propio / Contraparte:** los dos lugares de carga. Propio es el registro del contador; Contraparte, el externo.
- **Carga:** el par de archivos Propio y Contraparte con el que se trabaja. Hay una sola carga a la vez.
- **Celda con datos:** celda con al menos un carácter distinto de espacio. En una fórmula cuenta su resultado. La marca BOM al inicio de un CSV no es dato.
- **Fila con datos:** fila con al menos una celda con datos. Las filas sin datos se ignoran en el conteo y en la vista previa.
- **Hoja listable:** hoja de un Excel que es visible, contiene celdas (no es una hoja de gráfico) y tiene al menos una fila con datos.
- **Archivo vacío:** archivo de 0 bytes, CSV sin filas con datos o Excel sin ninguna hoja listable.
- **Cantidad de filas:** cantidad de filas con datos del CSV o de la hoja usada.
- **Vista previa:** las primeras 10 filas con datos. En un CSV se muestran separadas en columnas; en un Excel, con el valor que se ve en Excel (resultado de fórmulas, números y fechas con su formato). Se muestran hasta 20 columnas y cada celda hasta 50 caracteres.
- **Archivo válido:** archivo que pasó todas las validaciones y, si es Excel, tiene una hoja determinada.
- **10 MB:** 10.485.760 bytes.
- **Mismo archivo:** dos archivos con contenido idéntico byte a byte, sin importar el nombre.

## Requisitos funcionales
- RF-1: EL SISTEMA ofrece dos lugares de carga, Propio y Contraparte, de un archivo cada uno.
- RF-2: EL SISTEMA mantiene una sola carga a la vez.
- RF-3: EL SISTEMA acepta archivos con extensión `.csv`, `.xlsx` o `.xls`, sin distinguir mayúsculas de minúsculas. La extensión es el texto después del último punto del nombre.
- RF-4: SI un archivo no tiene extensión o tiene otra, ENTONCES EL SISTEMA lo rechaza con el mensaje M1.
- RF-5: SI un archivo supera los 10 MB, ENTONCES EL SISTEMA lo rechaza con el mensaje M2.
- RF-6: SI un archivo está vacío, ENTONCES EL SISTEMA lo rechaza con el mensaje M3.
- RF-7: SI un archivo tiene más de 100.000 filas con datos, ENTONCES EL SISTEMA lo rechaza con el mensaje M4.
- RF-8: SI un `.csv` tiene contenido que no es texto o comillas sin cerrar, ENTONCES EL SISTEMA lo rechaza con el mensaje M5.
- RF-9: SI un `.xls` o `.xlsx` tiene contenido que no es Excel o está dañado, ENTONCES EL SISTEMA lo rechaza con el mensaje M5.
- RF-10: SI un Excel está cifrado con contraseña de apertura, ENTONCES EL SISTEMA lo rechaza con el mensaje M6.
- RF-11: CUANDO un `.xls` o `.xlsx` tiene contenido de cualquiera de los dos formatos de Excel, EL SISTEMA lo lee, aunque la extensión no coincida con el formato.
- RF-12: CUANDO se sube un CSV, EL SISTEMA detecta el separador (`;`, `,` o tabulación) y la codificación (UTF-8 o Windows-1252).
- RF-13: CUANDO se rechaza un archivo, EL SISTEMA muestra el mensaje junto al lugar afectado (Propio o Contraparte) y conserva el archivo del otro lugar.
- RF-14: CUANDO un Excel tiene una sola hoja listable, EL SISTEMA usa esa hoja.
- RF-15: CUANDO un Excel tiene varias hojas listables, EL SISTEMA muestra sus nombres, sin ninguna preseleccionada, y pide elegir una.
- RF-16: MIENTRAS un Excel con varias hojas listables no tenga hoja elegida, EL SISTEMA no lo considera válido.
- RF-17: CUANDO el usuario elige otra hoja de un Excel ya cargado, EL SISTEMA actualiza su resumen sin volver a subir el archivo.
- RF-18: CUANDO un archivo es válido, EL SISTEMA muestra su resumen: nombre con extensión, hoja usada (si es Excel), cantidad de filas y vista previa.
- RF-19: CUANDO el usuario reemplaza el archivo de un lugar, EL SISTEMA lo valida de nuevo, descarta la hoja elegida antes y actualiza el resumen.
- RF-20: CUANDO Propio y Contraparte son el mismo archivo, EL SISTEMA muestra la advertencia M7 sin impedir continuar.
- RF-21: EL SISTEMA permite que Propio y Contraparte tengan formatos distintos (por ejemplo, un CSV y un Excel).
- RF-22: MIENTRAS Propio y Contraparte no sean ambos válidos, EL SISTEMA mantiene deshabilitada la acción Confirmar e indica qué lugar falta o no es válido.
- RF-23: CUANDO el usuario confirma, EL SISTEMA muestra el mensaje M8.

### Catálogo de mensajes
| Código | Mensaje |
|---|---|
| M1 | Formato no permitido. Subí un archivo `.csv`, `.xlsx` o `.xls`. |
| M2 | El archivo supera el límite de 10 MB. |
| M3 | El archivo no contiene datos. |
| M4 | El archivo supera el máximo de 100.000 filas con datos. |
| M5 | No se pudo leer el archivo: está dañado o no es un CSV o Excel válido. |
| M6 | El archivo está protegido con contraseña. Quitale la contraseña desde Excel y volvé a subirlo. |
| M7 | Propio y Contraparte son el mismo archivo. Revisá si es lo que querés conciliar. |
| M8 | Archivos listos para conciliar. |

## Requisitos no funcionales
- RNF-1: Los archivos existen solo mientras la aplicación está abierta; no se escriben en disco y se pierden al cerrarla.
- RNF-2: Refrescar la página conserva la carga; dos pestañas abiertas comparten la misma carga.
- RNF-3: Ni el nombre ni el contenido de los archivos se escriben en los registros de la aplicación.
- RNF-4: Todos los textos y mensajes están en español.
- RNF-5: Funciona sin conexión a internet: no depende de recursos externos.
- RNF-6: Los archivos usados en pruebas son siempre ficticios.

## Casos límite
- Extensión en mayúsculas (`.CSV`, `.XLSX`, `.XLS`): se acepta (RF-3).
- Archivo de 10.485.760 bytes: se acepta; con 10.485.761, se rechaza (RF-5).
- Archivo sin extensión, `.ods`, `.xlsm`, `.xlsb` o `.tsv`: se rechaza por extensión (RF-4).
- Doble extensión (`datos.csv.xlsx`): vale la última (RF-3).
- `.xlsx` renombrado a `.xls` o al revés: se lee (RF-11).
- `.csv` con contenido de Excel: se rechaza como ilegible (RF-8).
- Excel con contraseña de apertura: M6 (RF-10). Hoja protegida solo contra edición: se lee.
- Excel cuya única hoja con datos está oculta: es un archivo vacío (RF-6).
- Excel con hojas de gráfico u ocultas: no aparecen en la lista para elegir (definición de hoja listable).
- Filas o columnas ocultas dentro de la hoja: se cuentan y se muestran.
- Celdas combinadas: el valor aparece en la primera celda del rango.
- Fórmulas con error (`#REF!`, `#DIV/0!`): se muestran tal cual.
- CSV que solo tiene BOM, líneas en blanco o separadores (`;;;`): es un archivo vacío (RF-6).
- CSV con filas de distinta cantidad de columnas: se acepta.
- Archivo con 1 a 9 filas con datos: la vista previa muestra todas.
- Filas con más de 20 columnas o celdas de más de 50 caracteres: la vista previa las recorta.
- Excel de menos de 10 MB con más de 100.000 filas con datos: se rechaza (RF-7).
- Mismo archivo en Propio y Contraparte: advertencia M7, no bloquea (RF-20).
- Nombre de archivo con acentos o espacios: se muestra tal cual.
- El usuario abandona la elección de hoja: el archivo queda sin hoja y no es válido (RF-16).

## Fuera de alcance
- Conciliar los archivos y descargar el resultado.
- Validar columnas, encabezados, fechas o importes.
- Formatos distintos de `.csv`, `.xlsx` y `.xls`.
- CSV en codificaciones distintas de UTF-8 y Windows-1252 (por ejemplo, UTF-16).
- Más de una carga a la vez o más de dos archivos por carga.
- Historial de cargas o archivos guardados.
- Varios usuarios.

## Criterios de finalización
- Cada RF está cubierto por un test automatizado. Si un RF mezcla datos y presentación, los datos llevan test y la presentación se verifica en el navegador.
- Todos los tests pasan.
- No quedan dudas abiertas.

## Dudas abiertas
- Ninguna. Resueltas en la clarificación del 2026-11-24.
