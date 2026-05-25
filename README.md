# UniPlan - Plataforma para la Gestión de Eventos Universitarios

## Equipo 20 - IHN

* Jose David Loaiza - A00404821
* Juan Jose Reyes
* Laura Sofia Buitrago
* Alejandro Vargas Sanchez - A00404840
* Sebastian Castillo

---

## Descripción General

UniPlan es una plataforma web desarrollada para centralizar la gestión de eventos universitarios dentro de la Universidad Icesi. El sistema permite a estudiantes, profesores, líderes estudiantiles y personal de Bienestar Universitario consultar, crear y administrar eventos académicos y extracurriculares.

La solución fue diseñada bajo un enfoque de **persistencia políglota**, utilizando:

* **PostgreSQL** como base de datos relacional
* **MongoDB** como base de datos NoSQL
* **Spring Boot** como framework backend principal

El objetivo principal es aprovechar las fortalezas de cada tecnología según el tipo de información y patrón de acceso requerido por el sistema.

---

## Arquitectura General

```text
Frontend
   |
   v
Spring Boot API
   |
   +--------------------+
   |                    |
   v                    v
PostgreSQL          MongoDB
(Relacional)        (NoSQL)
```

## Tecnologías Utilizadas

### Backend

* Java 17
* Spring Boot
* Spring Data MongoDB
* Spring Data PostgreSQL
* Spring Security
* Maven

## Bases de Datos

### PostgreSQL

Utilizado para:

* Información institucional estructurada
* Relaciones académicas
* Integridad referencial
* Usuarios institucionales
* Estadísticas agregadas

### MongoDB

Utilizado para:

* Eventos
* Usuarios dinámicos
* Diferentes tipos de usuarios
* Inscripciones embebidas
* Información flexible y dinámica
* Diferentes tipos de eventos
* Datos semiestructurados

---

## Persistencia Políglota

El sistema implementa una arquitectura híbrida donde cada base de datos resuelve problemas específicos.

### PostgreSQL (Relacional)

La base de datos relacional almacena la información institucional y estructurada de la universidad.

![Modelo PostgreSQL](docs\images\ModeloPostgreSQL.png)

#### Tablas principales

* countries
* departments
* cities
* campuses
* faculties
* areas
* programs
* subjects
* employees
* students
* groups
* enrollments
* users

#### Responsabilidades

##### Integridad referencial

PostgreSQL garantiza:

* Foreign Keys
* Constraints
* Validaciones
* Relaciones académicas

##### Validaciones académicas

La base relacional se utiliza para validar:

* Existencia de estudiantes
* Programas académicos
* Facultades
* Profesores
* Inscripciones
* Requisitos previos

##### Seguridad y consistencia

Se implementan:

* Roles
* Restricciones
* Checks
* Índices únicos
* Constraints de negocio

---

### MongoDB (NoSQL)

MongoDB almacena el núcleo transaccional de eventos y usuarios.

![Modelo MongoDB](docs\images\ModeloMongoDB.png)

#### Colecciones principales

##### usuarios

Contiene usuarios autenticados del sistema y atributos dinámicos según el tipo de usuario.

* nombre
* correo
* contraseña
* tipo
* datos específicos por tipo de usuario

##### eventos

Contiene:

* título
* descripción
* tipo
* fecha y hora de inicio
* fecha y hora de fin
* ubicación
* máximo de asistentes
* total inscritos (dato precalculado)
* cupos disponibles (dato precalculado)
* estado
* inscripciones (arreglo de objetos embebidos)
  * nombre
  * correo
* organizador (objeto embebido)
  * nombre
  * correo
  * tipo
* datos específicos según el tipo de evento

---

#### Modelo de Eventos Flexible

Uno de los principales motivos para usar MongoDB fue la necesidad de manejar múltiples tipos de eventos con estructuras diferentes.

---

##### Tipos de usuarios

###### Estudiante

* codigo

###### Profesor

* facultad
* departamento
* especializacion

###### Lider estudiantil

* programa
* semestre
* representacion

###### Personal de Bienestar Universitario

* area administrativa
* cargo

---

##### Tipos de eventos

###### Talleres

* materiales requeridos
* condiciones previas

###### Charlas

* conferencista
  * nombre
  * perfil
  * afiliacion
* enlaces
* descripción extendida

###### Torneos deportivos

* tipo de deporte
* reglas
* numero de equipos
* numero de participantes por equipo
* estructura del torneo

###### Actividades de voluntariado

* causa
* horas requeridas
* actividades
* puntos de encuentro
* responsables

###### Otros

Nuestro modelo es capaz de contener otros tipos de eventos (culturales, clubes, etc.) con surespectivos campos necesarios.

MongoDB permite manejar esta variabilidad sin modificar continuamente el esquema de la base de datos.

---

#### Estrategia de Embedding

Las inscripciones se almacenan embebidas dentro de cada documento de evento.

Esta decisión permite:

* Consultar un evento completo en una sola operación
* Reducir joins y consultas adicionales
* Mejorar el rendimiento de lectura
* Simplificar la gestión de cupos

Además, el arreglo tiene crecimiento controlado debido al límite máximo de asistentes.

## Requerimientos

### Requerimientos Funcionales

#### RF01 - Registro de estudiantes

El sistema deberá permitir que los estudiantes se registren proporcionando:

* Código estudiantil
* Correo institucional
* Contraseña

El sistema deberá validar:

* Que el estudiante exista en la base de datos institucional
* Que el estudiante no esté previamente registrado

---

#### RF02 - Autenticación de usuarios

El sistema deberá permitir el inicio de sesión de los usuarios registrados mediante:

* Correo institucional o username
* Contraseña

---

#### RF03 - Gestión independiente de usuarios

El sistema deberá gestionar de manera independiente:

* Registro
* Autenticación
* Usuarios del sistema

Sin modificar la base de datos institucional.

---

#### RF04 - Registro de organizadores

El administrador deberá poder registrar organizadores validando previamente su existencia en la base de datos institucional.

---

#### RF05 - Gestión de tipos de organizadores

El sistema deberá manejar diferentes tipos de organizadores:

* Profesores
* Líderes estudiantiles
* Personal de Bienestar Universitario

---

#### RF06 - Gestión de información específica de organizadores

El sistema deberá almacenar atributos específicos según el tipo de organizador:

##### Profesores

* Facultad
* Departamento académico
* Área de especialización

##### Líderes estudiantiles

* Programa académico
* Semestre
* Grupo o asociación representada

##### Personal de Bienestar

* Área administrativa
* Cargo

---

#### RF07 - Consulta de catálogo de eventos

El estudiante autenticado podrá consultar el catálogo de eventos disponibles.

---

#### RF08 - Visualización de información de eventos

El sistema deberá mostrar para cada evento:

* Título
* Tipo de actividad
* Fecha
* Hora
* Ubicación
* Descripción
* Cupos disponibles

---

#### RF09 - Filtrado de eventos

El sistema deberá permitir filtrar eventos por:

* Tipo
* Rango de fechas
* Estado:

  * Próximos
  * En curso
  * Finalizados

---

#### RF10 - Consulta detallada de eventos

El estudiante podrá visualizar el detalle completo de un evento.

---

#### RF11 - Inscripción a eventos

El estudiante podrá solicitar inscripción a un evento.

---

#### RF12 - Validación de cupos

El sistema deberá validar que existan cupos disponibles antes de registrar la inscripción.

---

#### RF13 - Validación de inscripción duplicada

El sistema deberá validar que el estudiante no esté previamente inscrito en el evento.

---

#### RF14 - Validación para talleres

Para talleres, el sistema deberá validar requisitos previos consultando información académica en la base de datos relacional.

---

#### RF15 - Validación para torneos deportivos

El sistema deberá validar que el estudiante no esté inscrito en otro torneo con horarios traslapados.

---

#### RF16 - Validación para voluntariado

El sistema deberá validar el cumplimiento del mínimo de horas requeridas.

---

#### RF17 - Validación para charlas

Las charlas únicamente requerirán validación de disponibilidad de cupos.

---

#### RF18 - Confirmación de inscripción

El sistema deberá mostrar una confirmación cuando la inscripción sea exitosa.

---

#### RF19 - Cancelación de inscripción

El estudiante podrá cancelar su inscripción desde su perfil.

---

#### RF20 - Liberación de cupos

El sistema deberá liberar automáticamente el cupo cuando una inscripción sea cancelada.

---

#### RF21 - Creación de eventos

Los organizadores podrán crear eventos registrando:

* Título
* Descripción
* Tipo
* Fecha
* Hora inicio
* Hora finalización
* Ubicación
* Número máximo de asistentes

---

#### RF22 - Gestión de talleres

El sistema deberá permitir registrar para talleres:

* Lista de materiales
* Condiciones previas
* Cursos requeridos
* Restricciones por semestre

---

#### RF23 - Gestión de charlas

El sistema deberá permitir registrar:

* Nombre del conferencista
* Perfil
* Afiliación
* Enlaces relacionados
* Descripción extendida

---

#### RF24 - Gestión de torneos deportivos

El sistema deberá permitir registrar:

* Tipo de deporte
* Reglas
* Número de equipos
* Participantes por equipo
* Estructura del torneo

---

#### RF25 - Gestión de actividades de voluntariado

El sistema deberá permitir registrar:

* Comunidad beneficiada
* Número de horas requeridas
* Actividades
* Información logística
* Responsables

---

#### RF26 - Gestión flexible de otros eventos

El sistema deberá permitir registrar información adicional no prevista inicialmente para eventos culturales, clubes y otros tipos de actividades.

---

#### RF27 - Validación de fechas de eventos

El sistema deberá validar que la fecha del evento no sea pasada.

---

#### RF28 - Validación de cupos de eventos

El sistema deberá validar que el número de cupos sea mayor a cero antes de publicar un evento.

---

#### RF29 - Generación de código único

El sistema deberá generar automáticamente un código único para cada evento.

---

#### RF30 - Consulta de inscritos

Los organizadores podrán consultar la lista de inscritos de sus eventos.

---

#### RF31 - Visualización de datos de inscritos

El sistema deberá mostrar:

* Nombre
* Código estudiantil
* Correo institucional

De cada inscrito.

---

#### RF32 - Exportación CSV

El sistema deberá permitir exportar la lista de inscritos en formato CSV.

---

#### RF33 - Gestión de estadísticas

El sistema deberá mantener estadísticas agregadas sobre eventos.

---

#### RF34 - Registro de métricas administrativas

El sistema deberá almacenar:

* Número de inscritos
* Cancelaciones
* Asistentes
* Porcentaje de ocupación

---

#### RF35 - Consistencia entre datos operacionales y estadísticas

El sistema deberá garantizar consistencia entre los datos transaccionales y las estadísticas almacenadas.

---

#### RF36 - Generación de reportes

El sistema deberá generar al menos dos reportes administrativos o informativos de valor para los usuarios.

---

### Requerimientos No Funcionales

#### RNF01 - Arquitectura web

La solución deberá implementarse como una aplicación web.

---

#### RNF02 - Persistencia políglota

El sistema deberá utilizar de forma complementaria:

* Base de datos relacional
* Base de datos NoSQL

---

#### RNF03 - Integración con base de datos institucional

La base de datos institucional deberá utilizarse únicamente como fuente de consulta.

---

#### RNF04 - No modificación de la BD institucional

El sistema no deberá modificar:

* Estructura
* Datos

De la base de datos institucional.

---

#### RNF05 - Flexibilidad del modelo de datos

La solución deberá soportar eventos con estructuras variables y dinámicas de forma eficiente.

---

#### RNF06 - Alta disponibilidad

El sistema deberá tener alta disponibilidad durante periodos académicos.

---

#### RNF07 - Rendimiento

Las consultas e inscripciones deberán ejecutarse sin demoras perceptibles para los usuarios.

---

#### RNF08 - Seguridad de la información

La información personal de los estudiantes deberá almacenarse de forma segura.

---

#### RNF09 - Control de acceso

La información personal no deberá ser visible para estudiantes no autorizados.

---

#### RNF10 - Acceso restringido

Solo los organizadores y personal autorizado podrán consultar información sensible de inscritos.

---

#### RNF11 - Integridad referencial

La base de datos relacional deberá mantener integridad referencial.

---

#### RNF12 - Escalabilidad

La solución deberá soportar crecimiento en:

* Número de eventos
* Usuarios
* Inscripciones

---

#### RNF13 - Exportación interoperable

El sistema deberá exportar información en formato CSV.

---

#### RNF14 - Validación de consistencia

La solución deberá garantizar consistencia entre datos operacionales y estadísticas.

---

#### RNF15 - Gestión de usuarios y roles

La base de datos deberá manejar usuarios, roles y permisos para conexión desde la aplicación.

---

#### RNF16 - Modelo NoSQL documentado

La solución deberá definir y documentar el modelo de datos MongoDB.

---

#### RNF17 - Compatibilidad académica

La solución deberá integrarse con la información académica existente de la universidad.

---

#### RNF18 - Mantenibilidad

La arquitectura deberá permitir extender fácilmente nuevos tipos de eventos y atributos.
