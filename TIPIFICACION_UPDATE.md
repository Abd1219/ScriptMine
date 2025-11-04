# Actualización del Script de Tipificación - ScriptMine

## 🔄 Cambios Implementados

Se ha actualizado completamente el **Script de Tipificación** para adaptarlo a las necesidades específicas de gestión de incidencias técnicas de fibra óptica y servicios de telecomunicaciones.

## 📋 Nuevos Campos del Formulario

### Campos Básicos
1. **Folio** (Obligatorio)
   - Identificador único del caso
   - Campo de texto libre

2. **OT** (Obligatorio) 
   - Número de Orden de Trabajo
   - Campo de texto libre

3. **Cliente** (Obligatorio)
   - Nombre del cliente afectado
   - Campo de texto libre

### Campo Principal: Tipo de Incidencia
**Dropdown con 13 opciones especializadas:**

1. **Corte o Atenuación de FO (Acometida)**
   - Cambio de acometida, cambio, limpieza o rearmado de conectores

2. **Servicio Activo (Sin intervención técnica)**
   - Validación de servicio / Pruebas

3. **Incidente atribuible al cliente**
   - Reconexión de equipo(s) / Eliminación de Dobleces

4. **Bloqueo de equipo / Puerto / HSU**
   - Reset de equipo y/o Actualización de Firmware

5. **Robo de puerto / Vandalismo / Sabotaje**
   - Validación de splitter y asignación de puerto

6. **Configuración local de ONT / CPE / HSU**
   - Se modifica configuración de equipo en sitio

7. **Aprovisionamiento de ONT / CPE / HSU**
   - Se reaprovisiona el servicio por medio de un INC

8. **Falla de cableado en site (Lado LAN)**
   - Cambio de cable, jumper o conector

9. **Falla de cableado en site (Lado WAN)**
   - Limpieza de conector / Eliminación de Dobleces / Cambio de Jumper

10. **Daño físico en puerto/equipo (Hardware)**
    - Reemplazo de equipo o cambio de puerto

11. **Infraestructura de MW**
    - Se reinstala o se corrige infraestructura

12. **Saturación en puerto PON**
    - Migración de puerto PON/Metro

13. **Splitter atenuado / Sin Potencia**
    - Reparación de potencia en splitter

### Campo Opcional
4. **Observaciones** (Opcional)
   - Campo de texto libre para comentarios adicionales

## 📄 Formato del Script Generado

### Estructura del Script
```
=== SCRIPT DE TIPIFICACIÓN ===
Fecha: [DD/MM/YYYY HH:MM]

INFORMACIÓN DEL CASO:
• Folio: [Número de folio]
• OT: [Número de OT]
• Cliente: [Nombre del cliente]

TIPO DE INCIDENCIA:
• [Categoría principal]
  [Subcategoría/Descripción detallada]

OBSERVACIONES:
[Comentarios adicionales si los hay]

--- Fin del script ---
```

### Ejemplo de Script Generado
```
=== SCRIPT DE TIPIFICACIÓN ===
Fecha: 03/11/2025 14:30

INFORMACIÓN DEL CASO:
• Folio: FO-2025-001234
• OT: OT-789456
• Cliente: Juan Pérez García

TIPO DE INCIDENCIA:
• Corte o Atenuación de FO (Acometida)
  Cambio de acometida, cambio, limpieza o rearmado de conectores

OBSERVACIONES:
Se detectó conector sucio en acometida principal. Se realizó limpieza y pruebas de potencia.

--- Fin del script ---
```

## 🔧 Mejoras Técnicas Implementadas

### Generador de Scripts Mejorado
- **Formato legible**: Las incidencias largas se dividen en múltiples líneas
- **Estructura clara**: Separación visual entre categoría principal y descripción
- **Información completa**: Todos los campos relevantes incluidos

### Validación de Campos
- **Campos obligatorios**: Folio, OT, Cliente y Tipo de Incidencia
- **Campo opcional**: Observaciones
- **Validación visual**: Indicadores de error para campos requeridos

### Interfaz de Usuario
- **Dropdown optimizado**: Lista desplegable con todas las opciones de incidencia
- **Etiquetas claras**: Nombres descriptivos para cada campo
- **Estilo futurista**: Mantiene la estética cyberpunk de la aplicación

## 🎯 Beneficios de la Actualización

### Para Técnicos
- **Clasificación precisa**: 13 tipos específicos de incidencias
- **Información completa**: Folio y OT para trazabilidad
- **Proceso estandarizado**: Formato consistente para todos los casos

### Para Gestión
- **Trazabilidad**: Números de folio y OT para seguimiento
- **Categorización**: Tipos específicos para análisis estadístico
- **Documentación**: Scripts formateados profesionalmente

### Para el Sistema
- **Compatibilidad**: Funciona con la arquitectura existente
- **Escalabilidad**: Fácil agregar nuevos tipos de incidencia
- **Mantenimiento**: Código limpio y bien estructurado

## 📊 Tipos de Incidencia por Categoría

### Infraestructura Física
- Corte o Atenuación de FO (Acometida)
- Falla de cableado en site (LAN/WAN)
- Infraestructura de MW

### Equipos y Hardware
- Bloqueo de equipo / Puerto / HSU
- Daño físico en puerto/equipo (Hardware)
- Configuración local de ONT / CPE / HSU

### Red y Conectividad
- Saturación en puerto PON
- Splitter atenuado / Sin Potencia
- Aprovisionamiento de ONT / CPE / HSU

### Incidentes Externos
- Robo de puerto / Vandalismo / Sabotaje
- Incidente atribuible al cliente

### Validación y Pruebas
- Servicio Activo (Sin intervención técnica)

## ✅ Estado de Implementación

### Completado
- ✅ **Campos actualizados** en ScriptTemplate.kt
- ✅ **Generador de scripts** modificado en ScriptGenerator.kt
- ✅ **Compilación exitosa** sin errores
- ✅ **Interfaz futurista** mantenida
- ✅ **Validación de campos** implementada

### Funcionalidades
- ✅ **Formulario dinámico** con nuevos campos
- ✅ **Dropdown especializado** con 13 opciones
- ✅ **Vista previa en tiempo real** del script
- ✅ **Guardado y edición** de scripts
- ✅ **Formato profesional** del output

La actualización del Script de Tipificación convierte a **ScriptMine** en una herramienta especializada para la gestión de incidencias técnicas en servicios de fibra óptica y telecomunicaciones, proporcionando un sistema de clasificación detallado y profesional.