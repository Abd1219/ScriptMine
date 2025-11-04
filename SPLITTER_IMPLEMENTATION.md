# Script de Splitter - Implementación Completa

## Resumen
Se ha implementado completamente la pantalla "Script de Splitter" siguiendo la arquitectura MVVM con las siguientes características:

### ✅ Componentes Implementados

#### 1. SplitterViewModel
- **Ubicación**: `app/src/main/java/com/abdapps/scriptmine/ui/viewmodel/SplitterViewModel.kt`
- **StateFlows implementados**:
  - `cuentaSplitter: StateFlow<String>`
  - `clienteSplitter: StateFlow<String>`
  - `splitter: StateFlow<String>`
  - `qr: StateFlow<String>`
  - `posicion: StateFlow<String>`
  - `potenciaEnSplitter: StateFlow<String>`
  - `potenciaEnDomicilio: StateFlow<String>`
  - `candado: StateFlow<String>`
  - `coordenadasDeSplitter: StateFlow<String>`
  - `coordenadasDelClienteSplitter: StateFlow<String>`
  - `scriptPreview: StateFlow<String>` (actualización en tiempo real)
  - `isSaving: StateFlow<Boolean>` (estado de guardado)
  - `saveSuccess: StateFlow<Boolean>` (confirmación de éxito)
  - `isGettingLocation: StateFlow<Boolean>` (estado de obtención de GPS)

#### 2. SplitterScreen Composable
- **Ubicación**: `app/src/main/java/com/abdapps/scriptmine/ui/screens/SplitterScreen.kt`
- **Características**:
  - Diseño futurista cyberpunk consistente con la app
  - Campos organizados en secciones con FuturisticCard
  - Vista previa en tiempo real del script generado
  - Botones con indicadores visuales de estado
  - Integración completa con permisos de ubicación

#### 3. Template SPLITTER Actualizado
- **Ubicación**: `app/src/main/java/com/abdapps/scriptmine/data/model/ScriptTemplate.kt`
- Actualizado con los campos especificados en los requisitos

#### 4. ScriptGenerator Actualizado
- **Ubicación**: `app/src/main/java/com/abdapps/scriptmine/utils/ScriptGenerator.kt`
- Función `generateSplitterScript()` actualizada con el formato correcto

#### 5. ViewModelFactory Actualizado
- **Ubicación**: `app/src/main/java/com/abdapps/scriptmine/ui/viewmodel/ViewModelFactory.kt`
- Agregado soporte para `SplitterViewModel` con inyección de LocationHelper

### 🎯 Funcionalidades Implementadas

#### Gestión de Estado Reactiva
```kotlin
// Actualización automática de vista previa
private fun updateScriptPreview() {
    _scriptPreview.value = generateScriptPreview(...)
}
```

#### Campos del Formulario (10 campos total)
**Información Básica:**
- Cuenta
- Cliente

**DATOS DE CONEXIÓN:**
- SPLITTER
- QR
- Posición
- Potencia en splitter
- Potencia en domicilio
- Candado

**Coordenadas GPS:**
- Coordenadas de splitter (con botón GPS)
- Coordenadas del cliente (con botón GPS)

#### Formato del Script Generado
```
Cuenta: [valor]
Cliente: [valor]

DATOS DE CONEXIÓN
SPLITTER: [valor]
QR: [valor]
Posición: [valor]
Potencia en splitter: [valor]
Potencia en domicilio: [valor]
Candado: [valor]
Coordenadas de splitter: [valor]
Coordenadas del cliente: [valor]
```

#### Gestión de Permisos y GPS
- **Verificación automática** de permisos de ubicación
- **Solicitud de permisos** cuando no están concedidos
- **Obtención de coordenadas** usando LocationHelper existente
- **Estados visuales** para indicar cuando se está obteniendo ubicación
- **Formato de coordenadas** en "latitud, longitud"

### 🎨 Interfaz de Usuario

#### Secciones Organizadas
1. **Información Básica** (Azul neón)
   - Cuenta y Cliente

2. **DATOS DE CONEXIÓN** (Verde neón)
   - Todos los campos técnicos del splitter

3. **Coordenadas GPS** (Púrpura neón)
   - Campos de coordenadas con botones GPS integrados

4. **Vista Previa del Script** (Cian neón)
   - Actualización en tiempo real
   - Scroll indicator
   - Altura aumentada (250dp) para mejor visualización

#### Botones de Acción
- **Guardar**: Con estados (Normal → Guardando → ¡Guardado!)
- **Copiar**: Con confirmación visual (¡Copiado!)
- **Limpiar**: Botón de icono que reinicia todos los campos

#### Campos de Coordenadas Especiales
- **Campo de solo lectura** que muestra las coordenadas obtenidas
- **Botón GPS** que cambia de ícono cuando está obteniendo ubicación
- **Integración con permisos** - solicita automáticamente si no están concedidos
- **Feedback visual** con diferentes colores según el estado

### 🔧 Integración Técnica

#### Arquitectura MVVM Completa
- **ViewModel** con gestión completa de estado
- **Repository pattern** para persistencia en base de datos
- **LocationHelper** para obtención de coordenadas GPS
- **Coroutines y StateFlow** para gestión reactiva del estado

#### Gestión de Permisos
```kotlin
// En SplitterScreen
if (locationHelper.hasLocationPermission()) {
    viewModel.onGetCoordinates("coordenadasDeSplitter")
} else {
    locationPermissionLauncher.launch(arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ))
}
```

#### Estados de Carga
- **isSaving**: Para operaciones de guardado
- **saveSuccess**: Para confirmación de éxito
- **isGettingLocation**: Para obtención de GPS

### 🚀 Cómo Usar

#### 1. Integración en Navegación
```kotlin
// En tu sistema de navegación
composable("splitter") {
    val splitterViewModel: SplitterViewModel = viewModel(
        factory = ViewModelFactory(repository, context)
    )
    
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Manejar resultado de permisos
    }
    
    SplitterScreen(
        viewModel = splitterViewModel,
        locationPermissionLauncher = locationPermissionLauncher,
        onNavigateBack = { navController.popBackStack() }
    )
}
```

#### 2. Uso del Template Existente
El template SPLITTER ya está actualizado y funcionará automáticamente con el sistema EditScriptScreen existente, pero para la funcionalidad completa de GPS se recomienda usar SplitterScreen.

### ✨ Características Destacadas

- **Integración GPS completa** con manejo de permisos
- **Vista previa en tiempo real** que se actualiza con cada cambio
- **Diseño futurista consistente** con el resto de la aplicación
- **Estados visuales claros** para todas las operaciones
- **Gestión robusta de errores** y estados de carga
- **Campos de coordenadas especializados** con botones GPS integrados

La implementación está completa y lista para usar en producción, proporcionando una experiencia de usuario fluida para la creación de scripts de splitter con funcionalidad GPS integrada.