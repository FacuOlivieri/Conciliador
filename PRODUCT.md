# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users
Contadores y estudios contables que concilian registros (IVA, Ventas, Compras, Ingresos Brutos) contra los de un tercero. Trabajan en una PC de escritorio, con teclado y mouse, en sesiones largas y frente a tablas de datos. Usan la aplicación de forma local, sin cuenta ni conexión a servicios externos.

## Product Purpose
Aplicación local que recibe dos archivos (CSV o Excel), los cruza en memoria y deja descargar el resultado. No guarda historial ni usa base de datos. El éxito es que el contador llegue de "tengo dos archivos" a "tengo la conciliación" sin dudar de qué hizo la aplicación con sus datos.

## Positioning
Herramienta de trabajo pensada para el contador argentino: entiende los CSV con `;` y coma decimal, habla en español rioplatense (voseo) y no requiere instalar nada más que la aplicación.

## Operating Context
- Dos lugares de carga por conciliación: Propio y Contraparte.
- Flujo: elegir el tipo de conciliación en la página inicial, cargar los dos archivos, revisar el resumen y la vista previa de cada uno y confirmar.
- Cambiar de tipo con archivos ya subidos los descarta, y la aplicación pide confirmación antes.

## Capabilities and Constraints
- Interfaz renderizada en el servidor (Thymeleaf), sin JavaScript de lógica.
- Sin CDN ni fuentes externas: debe verse igual sin conexión a internet.
- Todos los textos en español; los mensajes salen de catálogos (enums), no de las plantillas.
- Los datos del contador nunca se muestran en logs ni se envían fuera de la máquina.

## Brand Commitments
Sin marca previa: no hay logo, colores ni tipografía definidos. El nombre en pantalla es "Conciliador" y se escribe como texto. Dirección indicada por la persona dueña del producto: moderno y aireado, tirando a claro, paleta monocromática de lila, sensación confiable y tranquila.

## Evidence on Hand
No hay capturas, clientes ni testimonios. Los textos de ayuda y de error están en las specs 001 y 002 y no se reescriben desde el diseño.

## Product Principles
1. Los datos del contador son lo importante; la interfaz se mantiene en segundo plano.
2. Cada pantalla deja claro qué falta para seguir y por qué.
3. Una acción destructiva (descartar archivos) nunca se activa por accidente.
4. Todo se puede hacer solo con el teclado.
5. Calma antes que énfasis: nada parpadea, salta ni compite por la atención.

## Accessibility & Inclusion
Se puede recorrer completo con Tab y Enter. Contraste de texto de al menos 4,5:1 y foco siempre visible. Respeta la preferencia de movimiento reducido y el tamaño de texto del sistema.
