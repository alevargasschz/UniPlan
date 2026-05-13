# UniPlan - Plataforma para la Gestión de Eventos Universitarios

## Equipo 20 - IHN

* Jose David Loaiza
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
* detalles específicos según el tipo de evento

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
* responsabilidades

###### Otros

Nuestro modelo es capaz de contener otros tipos de eventos (culturales, clubes, etc.) con surespectivos campos necesarios.

MongoDB permite manejar esta variabilidad sin modificar continuamente el esquema de la base de datos.

---

#### Estrategia de Embedding

Las inscripciones se almacenan embebidas dentro de cada documento de evento.

##### Ejemplo

```json
{
  "_id": "...",
  "title": "Taller Spring Boot",
  "max_attendees": 50,
  "available_slots": 20,
  "registrations": [
    {
      "student_id": "2001",
      "name": "Laura Hernández",
      "email": "laura@icesi.edu.co"
    }
  ]
}
```

##### Razones de diseño

Esta decisión permite:

* Consultar un evento completo en una sola operación
* Reducir joins y consultas adicionales
* Mejorar el rendimiento de lectura
* Simplificar la gestión de cupos

Además, el arreglo tiene crecimiento controlado debido al límite máximo de asistentes.
