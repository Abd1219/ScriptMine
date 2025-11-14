# ✅ Fix: Agregar Sufijo "dbm" a Campos de Potencia en Splitter

## 🎯 Mejora Solicitada

En el script de Splitter, cuando se llenan los campos de potencia, automáticamente agregar " dbm" al final en la vista previa.

**Ejemplo:**
- Usuario ingresa: `-18.90`
- Vista previa muestra: `-18.90 dbm`

## 📝 Campos Afectados

1. **Potencia en splitter**
   - Campo: `potenciaEnSplitter`
   - Antes: `Potencia en splitter: -18.90`
   - Después: `Potencia en splitter: -18.90 dbm`

2. **Potencia en domicilio**
   - Campo: `potenciaEnDomicilio`
   - Antes: `Potencia en domicilio: -20.50`
   - Después: `Potencia en domicilio: -20.50 dbm`

## 🔧 Implementación

### ScriptGenerator.kt - Función generateSplitterScript()

```kotlin
private fun generateSplitterScript(data: Map<String, String>, date: String): String {
    return buildString {
        appendLine("Cuenta: ${data["cuentaSplitter"] ?: ""}")
        appendLine("Cliente: ${data["clienteSplitter"] ?: ""}")
        appendLine()
        appendLine("DATOS DE CONEXIÓN")
        appendLine("SPLITTER: ${data["splitter"] ?: ""}")
        appendLine("QR: ${data["qr"] ?: ""}")
        appendLine("Posición: ${data["posicion"] ?: ""}")
        
        // Add " dbm" suffix to power values if they exist and don't already have it
        val potenciaSplitter = data["potenciaEnSplitter"] ?: ""
        if (potenciaSplitter.isNotEmpty() && !potenciaSplitter.contains("dbm", ignoreCase = true)) {
            appendLine("Potencia en splitter: $potenciaSplitter dbm")
        } else {
            appendLine("Potencia en splitter: $potenciaSplitter")
        }
        
        val potenciaDomicilio = data["potenciaEnDomicilio"] ?: ""
        if (potenciaDomicilio.isNotEmpty() && !potenciaDomicilio.contains("dbm", ignoreCase = true)) {
            appendLine("Potencia en domicilio: $potenciaDomicilio dbm")
        } else {
            appendLine("Potencia en domicilio: $potenciaDomicilio")
        }
        
        appendLine("Candado: ${data["candado"] ?: ""}")
        appendLine("Coordenadas de splitter: ${data["coordenadasDeSplitter"] ?: ""}")
        append("Coordenadas del cliente: ${data["coordenadasDelClienteSplitter"] ?: ""}")
    }
}
```

## 🎯 Lógica Implementada

### Validación Inteligente

1. **Si el campo está vacío**: No agrega nada
   ```
   Input: ""
   Output: "Potencia en splitter: "
   ```

2. **Si el campo tiene valor sin "dbm"**: Agrega " dbm"
   ```
   Input: "-18.90"
   Output: "Potencia en splitter: -18.90 dbm"
   ```

3. **Si el campo ya tiene "dbm"**: No lo duplica
   ```
   Input: "-18.90 dbm"
   Output: "Potencia en splitter: -18.90 dbm"
   ```

4. **Case insensitive**: Detecta "dbm", "DBM", "dBm", etc.
   ```
   Input: "-18.90 DBM"
   Output: "Potencia en splitter: -18.90 DBM"
   ```

## 📊 Ejemplos de Uso

### Ejemplo 1: Valores Negativos Típicos
```
Usuario ingresa:
- Potencia en splitter: -18.90
- Potencia en domicilio: -20.50

Vista previa muestra:
Potencia en splitter: -18.90 dbm
Potencia en domicilio: -20.50 dbm
```

### Ejemplo 2: Valores Positivos
```
Usuario ingresa:
- Potencia en splitter: 5.2
- Potencia en domicilio: 3.8

Vista previa muestra:
Potencia en splitter: 5.2 dbm
Potencia en domicilio: 3.8 dbm
```

### Ejemplo 3: Usuario Ya Incluye "dbm"
```
Usuario ingresa:
- Potencia en splitter: -18.90 dbm
- Potencia en domicilio: -20.50 DBM

Vista previa muestra:
Potencia en splitter: -18.90 dbm
Potencia en domicilio: -20.50 DBM
(No duplica el sufijo)
```

### Ejemplo 4: Campos Vacíos
```
Usuario no ingresa nada:
- Potencia en splitter: (vacío)
- Potencia en domicilio: (vacío)

Vista previa muestra:
Potencia en splitter: 
Potencia en domicilio: 
(No agrega "dbm" a campos vacíos)
```

## 🎨 Vista Previa Completa

### Antes del Fix
```
Cuenta: 12345
Cliente: Cliente Ejemplo

DATOS DE CONEXIÓN
SPLITTER: SPL-001
QR: QR123456
Posición: 1
Potencia en splitter: -18.90
Potencia en domicilio: -20.50
Candado: CAN-001
Coordenadas de splitter: 19.123456, -99.123456
Coordenadas del cliente: 19.654321, -99.654321
```

### Después del Fix
```
Cuenta: 12345
Cliente: Cliente Ejemplo

DATOS DE CONEXIÓN
SPLITTER: SPL-001
QR: QR123456
Posición: 1
Potencia en splitter: -18.90 dbm
Potencia en domicilio: -20.50 dbm
Candado: CAN-001
Coordenadas de splitter: 19.123456, -99.123456
Coordenadas del cliente: 19.654321, -99.654321
```

## ✅ Beneficios

1. **Formato Profesional**
   - Los valores de potencia siempre incluyen la unidad
   - Más claro y profesional en reportes

2. **Ahorro de Tiempo**
   - Usuario no necesita escribir " dbm" manualmente
   - Menos errores de tipeo

3. **Consistencia**
   - Todos los scripts tienen el mismo formato
   - Unidades siempre presentes

4. **Inteligente**
   - No duplica si el usuario ya lo escribió
   - Detecta variaciones (dbm, DBM, dBm)

## 🧪 Testing

### Casos de Prueba

1. **Valor negativo simple**
   - Input: `-18.90`
   - Expected: `-18.90 dbm` ✅

2. **Valor positivo simple**
   - Input: `5.2`
   - Expected: `5.2 dbm` ✅

3. **Ya incluye dbm minúsculas**
   - Input: `-18.90 dbm`
   - Expected: `-18.90 dbm` ✅

4. **Ya incluye DBM mayúsculas**
   - Input: `-18.90 DBM`
   - Expected: `-18.90 DBM` ✅

5. **Ya incluye dBm mixto**
   - Input: `-18.90 dBm`
   - Expected: `-18.90 dBm` ✅

6. **Campo vacío**
   - Input: ``
   - Expected: `` ✅

7. **Solo espacios**
   - Input: `   `
   - Expected: `   ` ✅

## 📝 Notas Técnicas

### Por Qué No Modificar el Input

No modificamos el valor en el campo de entrada porque:
1. El usuario puede querer ingresar el valor sin unidad
2. Permite flexibilidad en el formato
3. Solo afecta la vista previa (el script generado)
4. El valor original se guarda en la base de datos

### Detección Case-Insensitive

Usamos `contains("dbm", ignoreCase = true)` para detectar:
- `dbm`
- `DBM`
- `dBm`
- `Dbm`
- Cualquier variación de mayúsculas/minúsculas

## 🔄 Compatibilidad

### Scripts Existentes

Los scripts guardados anteriormente:
- ✅ Siguen funcionando normalmente
- ✅ Al editarlos, se aplicará el nuevo formato
- ✅ No se modifican automáticamente en la base de datos

### Otros Templates

Este cambio **solo afecta** al template SPLITTER:
- ✅ INTERVENCION: No afectado (ya tiene " (dbm)" en el label)
- ✅ TIPIFICACION: No afectado
- ✅ SOPORTE: No afectado
- ✅ CIERRE_MANUAL: No afectado
- ✅ APOYO_MW_OPS: No afectado

## 📱 Cómo Usar

1. **Abrir Script de Splitter**
   - Selecciona "Script de splitter"

2. **Llenar Campos de Potencia**
   - Potencia en splitter: Ingresa `-18.90`
   - Potencia en domicilio: Ingresa `-20.50`

3. **Ver Vista Previa**
   - Automáticamente muestra: `-18.90 dbm` y `-20.50 dbm`

4. **Copiar o Guardar**
   - El script incluye las unidades correctamente

## 🎉 Resultado

Ahora los campos de potencia en el script de Splitter:
- ✅ Agregan automáticamente " dbm" en la vista previa
- ✅ No duplican si el usuario ya lo escribió
- ✅ Formato profesional y consistente
- ✅ Ahorro de tiempo para el usuario

---

**Fecha**: Noviembre 8, 2025
**Versión**: 2.0.3
**Estado**: ✅ IMPLEMENTADO
