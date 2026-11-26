# Spec 003 — Tipos de comprobante
Estado: aprobada

## Contexto y objetivo
La conciliación de ventas compara los comprobantes que registra la empresa con los que informa ARCA en el Libro IVA Digital. Un comprobante se identifica por su tipo, su punto de venta y su número. ARCA escribe el tipo con un código numérico (por ejemplo, 001 para Facturas A), pero cada empresa usa su propia nomenclatura ("FC A", "Fact. A", "FA"). Para poder comparar, hay que llevar las dos formas a un mismo idioma: el código de ARCA.

Esta spec cubre solo el primer paso: un catálogo interno con los tipos de comprobante y el reconocimiento del tipo a partir del valor que trae un archivo. El punto de venta, el número, la comparación y la forma de mostrarle el resultado al contador van en specs posteriores.

## Usuarios
- El contador que usa la aplicación en su propia computadora. No ve una pantalla nueva: el reconocimiento lo usarán las próximas specs de la conciliación de ventas.

## Historias de usuario
- HU-1. Como contador, quiero que la aplicación entienda el tipo de comprobante tanto si viene con el código de ARCA como con una abreviatura habitual, para comparar sin reescribir mis archivos.
- HU-2. Como contador, quiero que lo que la aplicación no entiende quede marcado como no reconocido junto con lo que decía el archivo, para que las próximas pantallas me lo muestren y no se acepte un cruce equivocado.

## Definiciones
- **Tipo de comprobante:** cada tipo de la tabla de comprobantes del Libro IVA Digital de ARCA, identificado por un código numérico y un nombre oficial.
- **Nombre oficial:** el nombre del tipo escrito tal como figura en esa tabla (por ejemplo, "FACTURAS A").
- **Catálogo:** la lista fija de todos los tipos de esa tabla, cada uno con su código, su nombre oficial y sus abreviaturas del anexo A. Un tipo puede no tener abreviaturas. El catálogo solo cambia con un cambio de esta spec.
- **Abreviatura:** una forma corta o alternativa con que una empresa escribe un tipo, por ejemplo "FC A", "Fact. A" o "Factura A".
- **Valor de tipo:** el texto completo que muestra el archivo en el lugar del tipo de comprobante, no el recortado de la vista previa de la spec 001. Si es una celda numérica de Excel, es el texto que muestra la celda con su formato.
- **Forma normalizada:** el valor pasado a minúsculas y sin tildes ni diéresis (la ñ se mantiene), con todo lo que no sea una letra o un dígito tomado como espacio (puntos, guiones, rayas, barras, paréntesis, tabulaciones, espacios duros, saltos de línea), con un solo espacio entre palabras y sin espacios en los extremos. Ejemplo: "Fact. A" queda "fact a".
- **Resultado:** lo que devuelve el reconocimiento: el tipo reconocido, con su código y su nombre oficial, o "no reconocido" junto con el valor de tipo original.

## Requisitos funcionales
- RF-1: EL SISTEMA tiene un catálogo con todos los tipos de la tabla de comprobantes del Libro IVA Digital vigente al aprobar esta spec, cada uno con su código, su nombre oficial y las abreviaturas del anexo A.
- RF-2: EL SISTEMA mantiene el catálogo de modo que cada nombre oficial y cada abreviatura tenga al menos una letra, y que ninguna abreviatura, en forma normalizada, se repita en otro tipo ni sea igual al nombre oficial de otro tipo.
- RF-3: CUANDO el valor de tipo tiene solo dígitos del 0 al 9, sin contar los espacios de los extremos, y su número es un código del catálogo, EL SISTEMA lo reconoce como ese tipo, tenga o no ceros adelante.
- RF-4: CUANDO el valor de tipo empieza con un código, seguido de un guion o una raya y de un texto, EL SISTEMA lo reconoce si el código es del catálogo y la forma normalizada del texto coincide con el nombre oficial o una abreviatura de ese mismo tipo.
- RF-5: CUANDO el valor de tipo no tiene la forma de RF-3 ni la de RF-4 y su forma normalizada coincide con el nombre oficial o una abreviatura de un solo tipo, EL SISTEMA lo reconoce como ese tipo.
- RF-6: SI el valor de tipo tiene solo dígitos y su número no es un código del catálogo, ENTONCES EL SISTEMA lo da como no reconocido.
- RF-7: SI el valor de tipo tiene la forma de RF-4 y el código no es del catálogo o el texto no corresponde a ese mismo tipo, ENTONCES EL SISTEMA lo da como no reconocido.
- RF-8: SI la forma normalizada del valor de tipo no coincide con ningún nombre oficial ni abreviatura, ENTONCES EL SISTEMA lo da como no reconocido.
- RF-9: SI la forma normalizada del valor de tipo coincide con los nombres oficiales de más de un tipo, ENTONCES EL SISTEMA lo da como no reconocido, sin elegir uno.
- RF-10: SI el valor de tipo está vacío, su forma normalizada queda vacía o tiene más de 100 caracteres, ENTONCES EL SISTEMA lo da como no reconocido.
- RF-11: EL SISTEMA devuelve como resultado de cada reconocimiento el tipo reconocido, con su código y su nombre oficial, o "no reconocido" junto con el valor de tipo original sin cambios.

## Requisitos no funcionales
- RNF-1: EL SISTEMA reconoce tipos sin conexión a internet, porque el catálogo viene incluido en la aplicación.
- RNF-2: EL SISTEMA guarda los nombres oficiales tal como figuran en la tabla de ARCA, y las abreviaturas en español.
- RNF-3: EL SISTEMA no escribe valores de tipo en los registros de la aplicación.

## Casos límite
- "001", "1" y " 1 " se reconocen como 001 – Facturas A (RF-3).
- "000000000000000000001" se reconoce como 001: los ceros adelante no tienen límite mientras el valor no pase de 100 caracteres.
- "0", "00" o un código que la tabla no tiene: no reconocido (RF-6).
- Un código que la tabla ya no tiene, en un archivo de un período viejo: no reconocido (RF-6).
- "+1" y "-1": no tienen solo dígitos y su forma normalizada ("1") no tiene letras, así que no coincide con ningún nombre ni abreviatura: no reconocido (RF-8).
- "1.0", "1,5" y "1,00" (una celda numérica de Excel con formato decimal): no tienen la forma de RF-3 ni la de RF-4: no reconocido (RF-8).
- "1 - Factura A", "001-FC A" y "1 – Facturas A" se reconocen como 001 (RF-4), si "Factura A" y "FC A" están en el anexo A.
- "1 - Factura B": el código y el texto son de tipos distintos: no reconocido (RF-7).
- "1A" y "FC A 0001-00001234": el tipo pegado a otro dato no se separa: no reconocido (RF-8).
- "Fact. A", "FACT A", "fact-a", "Fáct A" y "Fact.A" quedan "fact a" y se reconocen como el mismo tipo.
- "FC E" y "FCE" son distintas: quedan "fc e" y "fce".
- "FC A" y "FCA" son distintas: se reconocen las dos solo si las dos están en el anexo A.
- "N/C A" queda "n c a": se reconoce solo si esa forma está en el anexo A.
- "Facturas A" (nombre oficial) se reconoce; "Factura A", en singular, solo si está en el anexo A.
- "NC" o "Factura" sin letra no son abreviatura de ningún tipo (RF-2): no reconocido (RF-8).
- Dos tipos con el mismo nombre oficial en forma normalizada: ese nombre da no reconocido (RF-9), pero el código de cada uno sigue sirviendo.
- "-" o "...": la forma normalizada queda vacía: no reconocido (RF-10).
- Un valor de 101 caracteres: no reconocido (RF-10).
- Un valor de más de 50 caracteres se reconoce con el texto completo, no con el recortado de la vista previa.
- Un código válido de ARCA que no es habitual en ventas se reconoce igual: el catálogo no filtra por tipo de conciliación.

## Fuera de alcance
- El punto de venta, el número de comprobante y la comparación entre archivos.
- Mostrarle el resultado al contador (pantallas y mensajes).
- Una pantalla para ver o editar el catálogo.
- Abreviaturas propias de cada empresa, cargadas por el contador.
- Guardar cualquier dato entre cierres de la aplicación.
- Detectar en qué columna del archivo está el tipo.
- Actualizar el catálogo automáticamente desde ARCA y conservar códigos dados de baja.
- Tipos cuya letra (A, B, C…) viene en otra columna, o el tipo pegado a otro dato en la misma celda.
- Reglas de plural y singular: cada forma aceptada tiene que figurar en el anexo A.
- Un límite de tiempo para el reconocimiento.

## Criterios de finalización
- El catálogo coincide con la tabla de comprobantes del Libro IVA Digital vigente al aprobar la spec, con la fuente y la fecha de descarga anotadas en el anexo A.
- El anexo A está revisado y aprobado por el contador, y cumple RF-2.
- Cada RF del 2 al 11 y cada caso límite tiene su test, y `./mvnw test` pasa en verde.

## Anexo A — Catálogo de tipos y abreviaturas
Fuente: sección "3. Tipo de Comprobante" del documento "Tablas del SISTEMA" del Libro IVA Digital, descargado el 2026-11-26 de https://www.arca.gob.ar/libro-iva-digital/documentos/Libro-IVA-Digital-Tablas-del-Sistema.pdf. El documento no muestra una fecha de versión.

Criterios con que se armó:
- Los nombres oficiales se copian tal como están, con sus tildes, comillas y abreviaturas. Los que en el documento ocupan dos renglones se unen con un espacio.
- Solo tienen abreviaturas los tipos de uso común en ventas: facturas, notas de débito y de crédito, recibos y notas de venta al contado (A, B, C, M, E y T), FCE MiPyMEs y tiques. Los demás se reconocen por su código o su nombre oficial.
- Quedan afuera las formas que sirven para más de un tipo: "FC" (Factura C o factura en general), "FE", "FCE", "NCE", "NDE", "NC", "ND" y "Factura" sin letra.
- En los tiques, "Ticket" vale como "Tique".
- Cumple RF-2: todos los nombres y abreviaturas tienen al menos una letra, y ninguna forma normalizada se repite entre tipos distintos.
- En el documento de ARCA algunas filas están resaltadas en color, sin una leyenda que lo explique. El catálogo las incluye igual que al resto.

Total: 121 tipos, 50 con abreviaturas, 125 abreviaturas.

| Código | Nombre oficial | Abreviaturas |
|---|---|---|
| 001 | FACTURAS A | Factura A · Fact. A · Fac A · FC A · FCA · FA |
| 002 | NOTAS DE DEBITO A | Nota de débito A · ND A · NDA · N/D A |
| 003 | NOTAS DE CREDITO A | Nota de crédito A · NC A · NCA · N/C A |
| 004 | RECIBOS A | Recibo A · Rec. A |
| 005 | NOTAS DE VENTA AL CONTADO A | Nota de venta al contado A · NVC A |
| 006 | FACTURAS B | Factura B · Fact. B · Fac B · FC B · FCB · FB |
| 007 | NOTAS DE DEBITO B | Nota de débito B · ND B · NDB · N/D B |
| 008 | NOTAS DE CREDITO B | Nota de crédito B · NC B · NCB · N/C B |
| 009 | RECIBOS B | Recibo B · Rec. B |
| 010 | NOTAS DE VENTA AL CONTADO B | Nota de venta al contado B · NVC B |
| 011 | FACTURAS C | Factura C · Fact. C · Fac C · FC C · FCC |
| 012 | NOTAS DE DEBITO C | Nota de débito C · ND C · NDC · N/D C |
| 013 | NOTAS DE CREDITO C | Nota de crédito C · NC C · NCC · N/C C |
| 015 | RECIBOS C | Recibo C · Rec. C |
| 016 | NOTAS DE VENTA AL CONTADO C | Nota de venta al contado C · NVC C |
| 017 | LIQUIDACION DE SERVICIOS PUBLICOS CLASE A | — |
| 018 | LIQUIDACION DE SERVICIOS PUBLICOS CLASE B | — |
| 019 | FACTURAS DE EXPORTACION | Factura E · Fact. E · FC E · Factura de exportación |
| 020 | NOTAS DE DEBITO POR OPERACIONES CON EL EXTERIOR | Nota de débito E · ND E |
| 021 | NOTAS DE CREDITO POR OPERACIONES CON EL EXTERIOR | Nota de crédito E · NC E |
| 022 | FACTURAS - PERMISO EXPORTACION SIMPLIFICADO - DTO. 855/97 | — |
| 023 | COMPROBANTES “A” DE COMPRA PRIMARIA PARA EL SECTOR PESQUERO MARITIMO | — |
| 024 | COMPROBANTES “A” DE CONSIGNACION PRIMARIA PARA EL SECTOR PESQUERO MARITIMO | — |
| 025 | COMPROBANTES “B” DE COMPRA PRIMARIA PARA EL SECTOR PESQUERO MARITIMO | — |
| 026 | COMPROBANTES “B” DE CONSIGNACION PRIMARIA PARA EL SECTOR PESQUERO MARITIMO | — |
| 027 | LIQUIDACION UNICA COMERCIAL IMPOSITIVA CLASE A | — |
| 028 | LIQUIDACION UNICA COMERCIAL IMPOSITIVA CLASE B | — |
| 029 | LIQUIDACION UNICA COMERCIAL IMPOSITIVA CLASE C | — |
| 030 | COMPROBANTES DE COMPRA DE BIENES USADOS | — |
| 032 | COMPROBANTES PARA RECICLAR MATERIALES | — |
| 033 | LIQUIDACION PRIMARIA DE GRANOS | — |
| 034 | COMPROBANTES A DEL APARTADO A INCISO F R G N 1415 | — |
| 035 | COMPROBANTES B DEL ANEXO I, APARTADO A, INC. F), RG N° 1415 | — |
| 036 | COMPROBANTES C DEL ANEXO I, APARTADO A, INC.F), R.G. N° 1415 | — |
| 037 | NOTAS DE DEBITO O DOCUMENTO EQUIVALENTE QUE CUMPLAN CON LA R.G. N° 1415 | — |
| 038 | NOTAS DE CREDITO O DOCUMENTO EQUIVALENTE QUE CUMPLAN CON LA R.G. N° 1415 | — |
| 039 | OTROS COMPROBANTES A QUE CUMPLEN CON LA R G 1415 | — |
| 040 | OTROS COMPROBANTES B QUE CUMPLAN CON LA R.G. 1415 | — |
| 041 | OTROS COMPROBANTES C QUE CUMPLAN CON LA R.G. 1415 | — |
| 043 | NOTA DE CREDITO LIQUIDACION UNICA COMERCIAL IMPOSITIVA CLASE B | — |
| 044 | NOTA DE CREDITO LIQUIDACION UNICA COMERCIAL IMPOSITIVA CLASE C | — |
| 045 | NOTA DE DEBITO LIQUIDACION UNICA COMERCIAL IMPOSITIVA CLASE A | — |
| 046 | NOTA DE DEBITO LIQUIDACION UNICA COMERCIAL IMPOSITIVA CLASE B | — |
| 047 | NOTA DE DEBITO LIQUIDACION UNICA COMERCIAL IMPOSITIVA CLASE C | — |
| 048 | NOTA DE CREDITO LIQUIDACION UNICA COMERCIAL IMPOSITIVA CLASE A | — |
| 049 | COMPROBANTES DE COMPRA DE BIENES NO REGISTRABLES A CONSUMIDORES FINALES | — |
| 050 | RECIBO FACTURA A REGIMEN DE FACTURA DE CREDITO | — |
| 051 | FACTURAS M | Factura M · Fact. M · Fac M · FC M · FCM · FM |
| 052 | NOTAS DE DEBITO M | Nota de débito M · ND M · NDM · N/D M |
| 053 | NOTAS DE CREDITO M | Nota de crédito M · NC M · NCM · N/C M |
| 054 | RECIBOS M | Recibo M · Rec. M |
| 055 | NOTAS DE VENTA AL CONTADO M | Nota de venta al contado M · NVC M |
| 056 | COMPROBANTES M DEL ANEXO I APARTADO A INC F R G N 1415 | — |
| 057 | OTROS COMPROBANTES M QUE CUMPLAN CON LA R G N 1415 | — |
| 058 | CUENTAS DE VENTA Y LIQUIDO PRODUCTO M | — |
| 059 | LIQUIDACIONES M | — |
| 060 | CUENTAS DE VENTA Y LIQUIDO PRODUCTO A | — |
| 061 | CUENTAS DE VENTA Y LIQUIDO PRODUCTO B | — |
| 063 | LIQUIDACIONES A | — |
| 064 | LIQUIDACIONES B | — |
| 066 | DESPACHO DE IMPORTACION | — |
| 068 | LIQUIDACION C | — |
| 070 | RECIBOS FACTURA DE CREDITO | — |
| 081 | TIQUE FACTURA A CONTROLADORES FISCALES | Tique factura A · Ticket factura A |
| 082 | TIQUE - FACTURA B | Ticket factura B |
| 083 | TIQUE | Ticket |
| 090 | NOTA DE CREDITO OTROS COMP QUE NO CUMPLEN CON LA R G 1415 Y SUS MODIF | — |
| 099 | OTROS COMP QUE NO CUMPLEN CON LA R G 1415 Y SUS MODIF | — |
| 109 | TIQUE C | Ticket C |
| 110 | TIQUE NOTA DE CREDITO | Ticket nota de crédito |
| 111 | TIQUE FACTURA C | Ticket factura C |
| 112 | TIQUE NOTA DE CREDITO A | Ticket nota de crédito A |
| 113 | TIQUE NOTA DE CREDITO B | Ticket nota de crédito B |
| 114 | TIQUE NOTA DE CREDITO C | Ticket nota de crédito C |
| 115 | TIQUE NOTA DE DEBITO A | Ticket nota de débito A |
| 116 | TIQUE NOTA DE DEBITO B | Ticket nota de débito B |
| 117 | TIQUE NOTA DE DEBITO C | Ticket nota de débito C |
| 118 | TIQUE FACTURA M | Ticket factura M |
| 119 | TIQUE NOTA DE CREDITO M | Ticket nota de crédito M |
| 120 | TIQUE NOTA DE DEBITO M | Ticket nota de débito M |
| 150 | LIQUIDACION DE COMPRA PRIMARIA PARA EL SECTOR TABACALERO A | — |
| 151 | LIQUIDACION DE COMPRA PRIMARIA PARA EL SECTOR TABACALERO B | — |
| 157 | CUENTA DE VENTA Y LÍQUIDO PRODUCTO A – SECTOR AVÍCOLA | — |
| 158 | CUENTA DE VENTA Y LÍQUIDO PRODUCTO B – SECTOR AVÍCOLA | — |
| 159 | LIQUIDACIÓN DE COMPRA A – SECTOR AVÍCOLA | — |
| 160 | LIQUIDACIÓN DE COMPRA B – SECTOR AVÍCOLA | — |
| 161 | LIQUIDACIÓN DE COMPRA DIRECTA A - SECTOR AVÍCOLA | — |
| 162 | LIQUIDACIÓN DE COMPRA DIRECTA B - SECTOR AVÍCOLA | — |
| 163 | LIQUIDACIÓN DE COMPRA DIRECTA C - SECTOR AVÍCOLA | — |
| 164 | LIQUIDACIÓN DE VENTA DIRECTA A - SECTOR AVÍCOLA | — |
| 165 | LIQUIDACIÓN DE VENTA DIRECTA B - SECTOR AVÍCOLA | — |
| 166 | LIQUIDACIÓN DE CONTRATACIÓN DE CRIANZA POLLOS PARRILLEROS A | — |
| 167 | LIQUIDACIÓN DE CONTRATACIÓN DE CRIANZA POLLOS PARRILLEROS B | — |
| 168 | LIQUIDACIÓN DE CONTRATACIÓN DE CRIANZA POLLOS PARRILLEROS C | — |
| 169 | LIQUIDACIÓN DE CRIANZA POLLOS PARRILLEROS A | — |
| 170 | LIQUIDACIÓN DE CRIANZA POLLOS PARRILLEROS B | — |
| 171 | LIQUIDACIÓN DE COMPRA DE CAÑA DE AZÚCAR A | — |
| 172 | LIQUIDACIÓN DE COMPRA DE CAÑA DE AZÚCAR B | — |
| 180 | CUENTA DE VENTA Y LÍQUIDO PRODUCTO A - SECTOR PECUARIO | — |
| 182 | CUENTA DE VENTA Y LÍQUIDO PRODUCTO B - SECTOR PECUARIO | — |
| 183 | LIQUIDACIÓN DE COMPRA A - SECTOR PECUARIO | — |
| 185 | LIQUIDACIÓN DE COMPRA B - SECTOR PECUARIO | — |
| 186 | LIQUIDACIÓN DE COMPRA DIRECTA A - SECTOR PECUARIO | — |
| 188 | LIQUIDACIÓN DE COMPRA DIRECTA B - SECTOR PECUARIO | — |
| 189 | LIQUIDACIÓN DE COMPRA DIRECTA C - SECTOR PECUARIO | — |
| 190 | LIQUIDACIÓN DE VENTA DIRECTA A - SECTOR PECUARIO | — |
| 191 | LIQUIDACIÓN DE VENTA DIRECTA B - SECTOR PECUARIO | — |
| 195 | FACTURA CLASE “T” | Factura T · Fact. T · FC T · FCT |
| 196 | NOTA DE DÉBITO CLASE “T” | Nota de débito T · ND T · NDT · N/D T |
| 197 | NOTA DE CRÉDITO CLASE “T” | Nota de crédito T · NC T · NCT · N/C T |
| 201 | FACTURA DE CRÉDITO ELECTRÓNICA MIPYMES (FCE) A | FCE A · Factura de crédito electrónica A |
| 202 | NOTA DE DÉBITO ELECTRÓNICA MIPYMES (FCE) A | ND FCE A · Nota de débito FCE A |
| 203 | NOTA DE CRÉDITO ELECTRÓNICA MIPYMES (FCE) A | NC FCE A · Nota de crédito FCE A |
| 206 | FACTURA DE CRÉDITO ELECTRÓNICA MIPYMES (FCE) B | FCE B · Factura de crédito electrónica B |
| 207 | NOTA DE DÉBITO ELECTRÓNICA MIPYMES (FCE) B | ND FCE B · Nota de débito FCE B |
| 208 | NOTA DE CRÉDITO ELECTRÓNICA MIPYMES (FCE) B | NC FCE B · Nota de crédito FCE B |
| 211 | FACTURA DE CRÉDITO ELECTRÓNICA MIPYMES (FCE) C | FCE C · Factura de crédito electrónica C |
| 212 | NOTA DE DÉBITO ELECTRÓNICA MIPYMES (FCE) C | ND FCE C · Nota de débito FCE C |
| 213 | NOTA DE CRÉDITO ELECTRÓNICA MIPYMES (FCE) C | NC FCE C · Nota de crédito FCE C |
| 331 | LIQUIDACION SECUNDARIA DE GRANOS | — |
| 332 | CERTIFICACION ELECTRONICA (GRANOS) | — |

## Dudas abiertas
- Resuelta (2026-11-26): el contador revisó y aprobó el anexo A tal como está.
- Resuelta (2026-11-26): la fuente es la tabla de comprobantes del Libro IVA Digital vigente al aprobar la spec.
- Resuelta (2026-11-26): las abreviaturas las propone Claude y el contador las revisa.
- Resuelta (2026-11-26): los signos separan palabras en vez de borrarse, para que "FC E" y "FCE" no choquen.
- Resuelta (2026-11-26): en "1 - Factura A", el código y el texto tienen que corresponder al mismo tipo.
- Resuelta (2026-11-26): se quita el límite de tiempo del reconocimiento; si en el uso real resulta lento, va en otra spec.
