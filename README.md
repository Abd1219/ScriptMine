# ScriptMine - Aplicación Android

ScriptMine es una aplicación Android nativa desarrollada en Kotlin que permite crear, gestionar y generar scripts personalizados para diferentes tipos de operaciones técnicas y de soporte.

## Características Principales

### 🎯 Plantillas de Script
- **Script de tipificación**: Para clasificar y documentar llamadas de soporte
- **Script de intervención**: Para gestionar intervenciones técnicas
- **Script de soporte**: Para casos de soporte al cliente
- **Script de splitter**: Para instalaciones de fibra óptica
- **Script de cierre manual**: Para cerrar tickets manualmente

### 📱 Funcionalidades
- **Interfaz intuitiva**: Diseño Material Design 3 con colores personalizados
- **Formularios dinámicos**: Campos que se adaptan según la plantilla seleccionada
- **Vista previa en tiempo real**: Visualización del script generado mientras se completa el formulario
- **Geolocalización**: Obtención automática de coordenadas GPS para campos de ubicación
- **Persistencia local**: Almacenamiento de scripts usando Room Database
- **Historial organizado**: Scripts guardados agrupados por tipo
- **Copiar al portapapeles**: Funcionalidad para copiar scripts generados
- **Edición de scripts**: Posibilidad de editar scripts guardados

## Tecnologías Utilizadas

- **Lenguaje**: Kotlin
- **UI Framework**: Jetpack Compose
- **Arquitectura**: MVVM (Model-View-ViewModel)
- **Base de datos**: Room Database
- **Navegación**: Navigation Compose
- **Asincronía**: Coroutines y Flow
- **Ubicación**: Google Play Services Location
- **Serialización**: Kotlinx Serialization

## Estructura del Proyecto

```
app/src/main/java/com/abdapps/scriptmine/
├── data/
│   ├── database/          # Room Database, DAO, Converters
│   ├── model/            # Modelos de datos
│   └── repository/       # Repositorio para acceso a datos
├── navigation/           # Configuración de navegación
├── ui/
│   ├── screens/         # Pantallas de la aplicación
│   ├── theme/           # Tema y colores Material Design 3
│   └── viewmodel/       # ViewModels para MVVM
├── utils/               # Utilidades (GPS, Clipboard, Generador de scripts)
├── MainActivity.kt      # Actividad principal
└── ScriptMineApplication.kt  # Clase de aplicación
```

## Pantallas de la Aplicación

### 1. Pantalla de Plantillas (TemplatesScreen)
- Muestra una cuadrícula con las 5 plantillas disponibles
- Cada plantilla tiene un ícono distintivo y nombre
- Navegación al historial de scripts

### 2. Pantalla de Edición (EditScriptScreen)
- Formulario dinámico basado en la plantilla seleccionada
- Campos de texto, dropdowns y coordenadas GPS
- Vista previa del script en tiempo real
- Botones para guardar, copiar y limpiar

### 3. Pantalla de Historial (HistoryScreen)
- Lista de scripts guardados agrupados por tipo
- Opciones para editar o eliminar scripts
- Navegación de vuelta a edición con datos precargados

## Permisos Requeridos

- `ACCESS_FINE_LOCATION`: Para obtener coordenadas GPS precisas
- `ACCESS_COARSE_LOCATION`: Para obtener ubicación aproximada

## Instalación y Configuración

1. Clona el repositorio
2. Abre el proyecto en Android Studio
3. Sincroniza las dependencias de Gradle
4. Ejecuta la aplicación en un dispositivo o emulador

## Paleta de Colores

- **Primary**: #3498DB (Azul principal)
- **Primary Variant**: #2980B9 (Azul más oscuro)
- **Surface**: #EAF2FF (Fondo azul claro)
- **Background**: #EAF2FF

## Requisitos del Sistema

- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34

## Arquitectura MVVM

La aplicación sigue el patrón MVVM para separar la lógica de negocio de la interfaz de usuario:

- **Model**: Entidades de Room, modelos de datos
- **View**: Composables de Jetpack Compose
- **ViewModel**: Gestión de estado y lógica de negocio

## Base de Datos

La aplicación utiliza Room Database para almacenamiento local:

- **Entidad**: `SavedScript` - Almacena scripts guardados
- **DAO**: `ScriptDao` - Operaciones de base de datos
- **Database**: `ScriptDatabase` - Configuración de la base de datos

## Generación de Scripts

El sistema genera scripts formateados automáticamente basándose en:
- Plantilla seleccionada
- Datos ingresados en el formulario
- Fecha y hora actual
- Formato específico para cada tipo de script

La aplicación está diseñada para ser robusta, intuitiva y seguir las mejores prácticas de desarrollo Android moderno.