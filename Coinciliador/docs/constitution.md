# Constitución de Conciliador

1. **Stack mínimo.** Spring Boot, Thymeleaf, Apache POI (Excel) y Apache Commons CSV. No se agrega ninguna dependencia al `pom.xml` sin aprobación explícita del usuario.
2. **La spec manda.** Todo código cubre un RF de `specs/NNN-*/`. Sin RF no se implementa. Un cambio va primero a la spec, luego al plan y las tareas, y al final al código.
3. **Lógica fuera de la vista.** Los Controller solo orquestan. Las reglas y los cálculos viven en Service o Helper. Las plantillas Thymeleaf no tienen lógica de negocio ni acceden a Repository. Los importes usan `BigDecimal`, nunca `double`.
4. **Tests primero.** Cada tarea empieza con un test en rojo y termina con `./mvnw test` en verde. Todo cálculo de gastos o ganancias tiene test unitario. Si hay persistencia, los tests corren con un datasource propio, sin tocar datos reales. Las tareas solo visuales (estilos, animaciones) se verifican en el navegador en lugar de con un test; si una tarea mezcla datos y presentación, los datos llevan test.
5. **Datos del usuario protegidos.** No se commitean credenciales ni datos financieros reales, y no van a los logs. La configuración sensible sale de variables de entorno. Ningún `ddl-auto` destructivo (`create`, `create-drop`) fuera de tests. Toda entrada se valida con Bean Validation.
6. **Idioma.** Todo en Espaniol

