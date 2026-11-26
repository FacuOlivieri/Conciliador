# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Acerca del Proyecto

Aplicación para contadores que les ayuda a realizar conciliaciones de todo tipo: IVA, Ventas, Compras, Ingresos Brutos, etc.

## Estado del proyecto

Scaffold de Spring Initializr sin lógica propia: solo `CoinciliadorApplication` y su test `contextLoads`. `application.properties` define únicamente el nombre de la app. Es una app local, sin base de datos. El proyecto Maven está en `Coinciliador/`.

## Comandos

Requiere JDK 25 (`java.version` en `pom.xml`). Ejecutar desde `Coinciliador/`:

```bash
./mvnw test                                                      # todos los tests (mvnw.cmd desde PowerShell)
./mvnw test -Dtest=CoinciliadorApplicationTests                  # una clase
./mvnw test -Dtest=CoinciliadorApplicationTests#contextLoads     # un método
./mvnw package                                                   # genera el jar
./mvnw spring-boot:run                                           # ejecuta la aplicación
```

No hay linter ni formateador configurados.

## Arquitectura

- **Stack actual:** Spring Boot 4.2.0 con Spring MVC renderizado en servidor y Thymeleaf, Bean Validation, Actuator, Lombok y DevTools. No hay persistencia: la constitución exige aprobación explícita antes de agregar dependencias (por ejemplo JPA o un driver).
- **Los starters de Spring Boot 4 son por funcionalidad:** el starter web es `spring-boot-starter-webmvc`, y cada starter tiene su equivalente de test (por ejemplo `spring-boot-starter-thymeleaf-test`). Agregar un starter de una funcionalidad normalmente implica agregar también su starter `-test`.
- **Lombok:** el procesador de anotaciones está declarado por separado en las ejecuciones `default-compile` y `default-testCompile` de `pom.xml`. Hay que mantener ambas al cambiar la configuración del compilador.
- **Paquete base:** `com.proyecto.Coinciliador` (con mayúscula). Los componentes nuevos tienen que estar dentro de él para que los detecte el escaneo de componentes.
- **Thymeleaf:** interfaces escritas en español. Usar la skill `/impeccable` para desarrollarlas.

## Convenciones

- Código, comentarios y texto explicativo en español
- Código limpio y escalable, métodos simples y chicos, de no más de 15 líneas si es posible, funcionalidades divididas en partes chicas
- Respetar la arquitectura de carpetas implementada de MVC + Helpers + Exceptions + Utils
- Respetar la nomenclatura de los archivos Controller, Service, Repository, DTOs, Exceptions
- Respetar el uso correcto de Clases e Interfaces ("ClaseService" como Clase, "IClaseService" como Interfaz)

## Forma de Trabajo

- Analizar las especificaciones hechas y su confección, planificar el código general antes de implementar. Utilizar las skills `sdd`
- Cambios chicos, no generar grandes cantidades de código de una sola vez, para un mejor control
- Explicar la teoría y la elección de los cambios hechos una vez generado el código

## Memoria

- Al empezar, leer `MEMORY.md` para conocer el estado del proyecto y las decisiones tomadas
- Al terminar una tarea, actualizarlo: estado actual, decisiones importantes (con su porqué) y errores a evitar.
- Mantenerlo breve (máximo ~50 líneas): resumir o eliminar lo que ya no aporte.
- Si algo se convierte en una regla permanente, proponer moverlo a `CLAUDE.md` en lugar de dejarlo en la memoria.
- No guardar nunca datos sensibles (claves, tokens, datos personales).

## Límites

- Antes de agregar dependencias nuevas, aclarar para qué sirven, qué resuelven y preguntar por confirmación
- Cada dependencia nueva lleva un mini tutorial en `Coinciliador/docs/librerias/<libreria>.md` (qué es, para qué la usamos, versión, ejemplos de uso, errores comunes), verificado contra la documentación de la versión fijada
- Leer `docs/constitution.md` y la spec activa (`specs/NNN-*/`) antes de tocar código.
- Actualizar el `README` con los cambios generados, con una breve y concisa explicación de lo que hace cada clase y módulo creado
- Una vez terminado la implementacion de la tarea de la specificacion, sugeri un commit. Formato: "feat: spec Sx-Tx, (nombre descriptivo para la spec)", este nombre descriptivo se refiere al problema que resuelve

## Verificación

- Cada tarea termina con `./mvnw test` en verde (ver la constitución, regla 4).
