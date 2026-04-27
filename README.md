# AppMovil-Recetas

Este repositorio alberga el proyecto final de la asignatura de aplicaciones móviles, centrado en una plataforma integral para la gestión técnica y social de recetas culinarias. El sistema permite centralizar la administración de preparaciones mediante la integración de persistencia de datos, protocolos de seguridad biométrica y herramientas analíticas de actividad.

---

## Especificaciones del Sistema

La arquitectura de la aplicación se basa en un ecosistema diseñado para garantizar la integridad de la información y la experiencia del usuario:

| Componente | Descripción Técnica |
| :--- | :--- |
| **Gestión de Contenido (CRUD)** | Operaciones completas para la creación, consulta, actualización y eliminación de registros de recetas. |
| **Seguridad Biométrica** | Autenticación mediante huella digital para el acceso a "Recetas Secretas" y directorios restringidos. |
| **Persistencia de Datos** | Implementación de almacenamiento híbrido con soporte local y sincronización en la nube. |
| **Análisis y Estadística** | Visualización de datos mediante gráficas de actividad mensual y métricas de privacidad. |
| **Interfaz Dinámica** | Soporte nativo para esquemas de visualización en modo claro y modo oscuro. |

---

## Arquitectura de Usuario y Flujos

### Autenticación y Gestión de Identidad
* **Acceso Seguro:** Interfaz de entrada con recuperación de credenciales y validación biométrica integrada.
* **Registro de Entidades:** Formulario de alta de cuenta con validación estricta de campos obligatorios.
* **Perfil de Usuario:** Modificación de metadatos personales y personalización de la identidad visual del autor.

### Gestión y Socialización de Recetas
* **Visualización Dinámica:** Panel de control con filtrado avanzado por etiquetas, categorías y motores de búsqueda por nombre.
* **Editor Técnico:** Sistema dinámico para el ingreso de ingredientes con unidades de medida y flujo secuencial de pasos de preparación.
* **Interacción Comunitaria:** Módulo para compartir recetas en el ecosistema público y sistema de valoración mediante promedio de estrellas.

---

## Stack Tecnológico

* **Lenguaje de Programación:** Kotlin 100%.
* **Entorno de Desarrollo:** Android Studio.
* **Seguridad:** API de Biometría de Android.
* **Gestión Multimedia:** Integración de CameraX y acceso a galería de medios.

---
> Proyecto final desarrollado por el equipo de Aplicaciones Móviles.
