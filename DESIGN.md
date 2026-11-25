# Design

Sistema visual de Conciliador. Lo comparten la página inicial (spec 002) y la pantalla de carga (spec 001, T21).
Los estilos viven en `Coinciliador/src/main/resources/static/css/`: `base.css` (tokens y componentes comunes), `inicio.css` y `carga.css`.

## Dirección
Moderno y aireado, claro, confiable y tranquilo. Modo Operate: el contador está haciendo una tarea, así que la familiaridad vale más que la expresión. Mucho espacio en blanco, superficies casi blancas con un tinte lila, un único acento lila profundo para lo accionable.

## Color (monocromático lila, matiz OKLCH ≈ 300)
| Token | Valor | Uso |
|---|---|---|
| `--fondo` | `oklch(98% 0.01 300)` | Fondo de la página |
| `--superficie` | `oklch(99.6% 0.004 300)` | Tarjetas y tabla |
| `--superficie-2` | `oklch(95.5% 0.02 300)` | Cabeceras, estado vacío, filas alternas |
| `--linea` | `oklch(88% 0.03 300)` | Bordes y divisores |
| `--texto` | `oklch(25% 0.045 300)` | Texto principal |
| `--texto-suave` | `oklch(42% 0.05 300)` | Ayudas y texto secundario |
| `--acento` | `oklch(47% 0.17 295)` | Botones primarios, enlaces, foco |
| `--acento-fuerte` | `oklch(40% 0.17 295)` | Hover y activo |
| `--acento-suave` | `oklch(93% 0.04 300)` | Fondo de hover y de avisos |

Los errores y avisos no cambian de matiz: se distinguen por borde completo más oscuro, fondo con más tinte y texto en negrita, y siempre llevan su texto. Decisión abierta: si hiciera falta un color de error aparte, se agrega un único token.
Solo modo claro (`color-scheme: light`).

## Tipografía
Una sola familia del sistema (`system-ui`), sin fuentes externas. Escala fija en rem con razón 1,2: 0,875 · 1 · 1,2 · 1,44 · 1,73 · 2,07. Cuerpo 1 rem con interlineado 1,6. Cifras tabulares en la tabla de vista previa.

## Espacio y forma
Espaciado en múltiplos de 0,5 rem; separación generosa entre bloques y compacta dentro de ellos. Radio 0,75 rem en tarjetas y 0,5 rem en controles. Sombra suave con desplazamiento y desenfoque, solo en las tarjetas.

## Componentes
- **Botón** (`.boton`): primario relleno de lila, secundario con borde. Alto mínimo 2,75 rem. Estados: hover, foco, activo y deshabilitado (apagado, sin sombra, cursor normal).
- **Tarjeta de tipo** (inicio): botón grande con nombre y frase de ayuda; el hover tiñe el fondo y oscurece el borde.
- **Tarjeta de lugar** (carga): una por lugar (Propio y Contraparte), lado a lado desde 56 rem y apiladas por debajo.
- **Tabla de vista previa**: región con desplazamiento horizontal y `tabindex="0"`, para recorrerla con el teclado; celdas en una sola línea.
- **Foco**: anillo de 3 px en `--acento` con separación de 2 px, siempre visible (`:focus-visible`).

## Movimiento
Solo transiciones de 150 ms en color, borde y sombra. Nada se anima al cargar. Se desactivan con `prefers-reduced-motion`.

## Reglas
- Sin CDN, sin fuentes externas, sin imágenes de terceros.
- Sin JavaScript de lógica.
- Sin tarjetas anidadas, sin degradados de texto, sin bordes de color a un solo lado.
