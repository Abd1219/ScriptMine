# 🔥 Crear Firestore Database - Guía Visual

## 📋 Pasos para Crear la Base de Datos

### 1️⃣ Acceder a Firebase Console

```
🌐 https://console.firebase.google.com
```

1. Inicia sesión con tu cuenta de Google
2. Selecciona el proyecto **ScriptMine**

---

### 2️⃣ Ir a Firestore Database

En el menú lateral izquierdo:

```
📁 Build (Compilación)
   └─ 🔥 Firestore Database  ← HAZ CLIC AQUÍ
```

---

### 3️⃣ Crear Base de Datos

Verás una pantalla que dice:

```
┌─────────────────────────────────────────┐
│                                         │
│   Cloud Firestore                       │
│                                         │
│   Store and sync data for client-       │
│   and server-side development           │
│                                         │
│   [Create database]  ← HAZ CLIC AQUÍ   │
│                                         │
└─────────────────────────────────────────┘
```

---

### 4️⃣ Elegir Modo de Seguridad

Se abrirá un modal con dos opciones:

```
┌─────────────────────────────────────────────────┐
│  Secure rules for Cloud Firestore               │
├─────────────────────────────────────────────────┤
│                                                  │
│  ○ Start in production mode                     │
│    Good for production apps                     │
│    Denies all reads and writes by default       │
│    ✅ SELECCIONA ESTA OPCIÓN                    │
│                                                  │
│  ○ Start in test mode                           │
│    Good for getting started                     │
│    Allows all reads and writes for 30 days      │
│    ⚠️ No recomendado para producción            │
│                                                  │
│                          [Next]                  │
└─────────────────────────────────────────────────┘
```

**Selecciona: ○ Start in production mode** ✅

Haz clic en **[Next]**

---

### 5️⃣ Elegir Ubicación del Servidor

```
┌─────────────────────────────────────────────────┐
│  Set Cloud Firestore location                   │
├─────────────────────────────────────────────────┤
│                                                  │
│  Choose where to store your data                │
│                                                  │
│  ⚠️ This setting is permanent and cannot be     │
│     changed later                                │
│                                                  │
│  Location: [▼ Select location]                  │
│                                                  │
│  Opciones recomendadas:                         │
│  • us-central1 (Iowa)                           │
│  • southamerica-east1 (São Paulo)               │
│  • europe-west1 (Belgium)                       │
│                                                  │
│                          [Enable]                │
└─────────────────────────────────────────────────┘
```

**Selecciona la ubicación más cercana a tus usuarios**

Haz clic en **[Enable]**

---

### 6️⃣ Esperar Creación

Verás un mensaje de progreso:

```
┌─────────────────────────────────────────┐
│                                         │
│   ⏳ Creating database...               │
│                                         │
│   This may take a few moments           │
│                                         │
└─────────────────────────────────────────┘
```

Espera 1-2 minutos...

---

### 7️⃣ Base de Datos Creada ✅

Una vez creada, verás la interfaz de Firestore:

```
┌─────────────────────────────────────────────────────────┐
│  Cloud Firestore                                        │
├─────────────────────────────────────────────────────────┤
│  [Data] [Rules] [Indexes] [Usage] [Monitoring]         │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  No collections yet                                      │
│                                                          │
│  [+ Start collection]                                    │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

---

## 🔒 Configurar Reglas de Seguridad

### Paso 1: Ir a la Pestaña Rules

Haz clic en la pestaña **[Rules]**

### Paso 2: Ver Reglas por Defecto

Verás algo como esto:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

**Estas reglas bloquean TODO el acceso** ❌

### Paso 3: Reemplazar con Nuestras Reglas

1. **Selecciona TODO** el contenido (Ctrl+A / Cmd+A)
2. **Borra** todo
3. **Copia** el contenido del archivo `FIRESTORE_RULES_QUICK.txt`
4. **Pega** en el editor

### Paso 4: Publicar Reglas

1. Haz clic en el botón **[Publish]** (arriba a la derecha)
2. Confirma la publicación
3. Verás un mensaje: "Rules published successfully" ✅

---

## 📊 Crear Colección de Scripts (Opcional)

Para verificar que todo funciona:

### Paso 1: Crear Colección

1. Ve a la pestaña **[Data]**
2. Haz clic en **[+ Start collection]**

```
┌─────────────────────────────────────────┐
│  Start a collection                     │
├─────────────────────────────────────────┤
│                                         │
│  Collection ID:                         │
│  [scripts]  ← ESCRIBE ESTO             │
│                                         │
│                    [Next] [Cancel]      │
└─────────────────────────────────────────┘
```

Haz clic en **[Next]**

### Paso 2: Agregar Documento de Prueba

```
┌─────────────────────────────────────────────────┐
│  Add its first document                         │
├─────────────────────────────────────────────────┤
│                                                  │
│  Document ID: [test]  ← Auto-ID o escribe "test"│
│                                                  │
│  Field         Type      Value                  │
│  ────────────────────────────────────────────   │
│  templateType  string    TEST                   │
│  userId        string    test-user-123          │
│  clientName    string    Test Client            │
│  version       number    1                      │
│  isDeleted     boolean   false                  │
│                                                  │
│  [+ Add field]                                   │
│                                                  │
│                    [Save] [Cancel]               │
└─────────────────────────────────────────────────┘
```

Haz clic en **[Save]**

### Paso 3: Verificar Documento Creado

Verás tu documento en la lista:

```
┌─────────────────────────────────────────────────┐
│  scripts                                        │
├─────────────────────────────────────────────────┤
│  📄 test                                        │
│     templateType: "TEST"                        │
│     userId: "test-user-123"                     │
│     clientName: "Test Client"                   │
│     version: 1                                  │
│     isDeleted: false                            │
└─────────────────────────────────────────────────┘
```

✅ **¡Base de datos funcionando!**

---

## 🎯 Checklist de Verificación

Marca cada paso cuando lo completes:

- [ ] 1. Accedí a Firebase Console
- [ ] 2. Seleccioné el proyecto ScriptMine
- [ ] 3. Fui a Firestore Database
- [ ] 4. Hice clic en "Create database"
- [ ] 5. Seleccioné "Production mode"
- [ ] 6. Elegí la ubicación del servidor
- [ ] 7. Esperé a que se creara la base de datos
- [ ] 8. Fui a la pestaña "Rules"
- [ ] 9. Copié y pegué las reglas de seguridad
- [ ] 10. Publiqué las reglas
- [ ] 11. (Opcional) Creé la colección "scripts"
- [ ] 12. (Opcional) Agregué un documento de prueba

---

## 🔍 Verificar que Todo Funciona

### Verificación 1: Reglas Publicadas

En la pestaña **Rules**:
- ✅ Debe decir "Published" con fecha reciente
- ✅ No debe haber errores de sintaxis

### Verificación 2: Base de Datos Activa

En la pestaña **Data**:
- ✅ Puedes ver la interfaz de Firestore
- ✅ Puedes crear colecciones
- ✅ No hay mensajes de error

### Verificación 3: Ubicación Configurada

En **Project Settings** → **General**:
- ✅ Debe mostrar la ubicación que seleccionaste
- ✅ Debe decir "Cloud Firestore location"

---

## 🚨 Problemas Comunes

### Problema: "Create database" no aparece

**Causa**: Ya existe una base de datos

**Solución**: 
- Verifica si ya hay una base de datos creada
- Busca en la pestaña "Data"
- Si existe, solo necesitas configurar las reglas

### Problema: "Location cannot be changed"

**Causa**: Ya se seleccionó una ubicación antes

**Solución**:
- La ubicación es permanente
- No se puede cambiar
- Continúa con la ubicación existente

### Problema: Error al publicar reglas

**Causa**: Error de sintaxis en las reglas

**Solución**:
1. Copia exactamente el contenido de `FIRESTORE_RULES_QUICK.txt`
2. No modifiques nada
3. Asegúrate de copiar TODO el contenido
4. Intenta publicar de nuevo

### Problema: "Permission denied" al crear colección

**Causa**: Las reglas de producción bloquean acceso sin autenticación

**Solución**:
- Esto es normal y correcto
- Las reglas requieren autenticación
- La app creará las colecciones automáticamente cuando un usuario autenticado las use

---

## 📱 Próximos Pasos

Una vez creada la base de datos:

1. ✅ **Configurar Authentication**
   - Habilitar Google Sign-In
   - Agregar SHA-1 certificate

2. ✅ **Crear Índices**
   - userId + lastModified
   - userId + syncStatus + lastModified
   - userId + isDeleted + lastModified

3. ✅ **Probar la App**
   - Ejecutar la app
   - Autenticarse con Google
   - Crear un script
   - Verificar que aparece en Firestore

---

## 📞 Ayuda Adicional

Si tienes problemas:

1. **Documentación oficial de Firebase**:
   - [Get started with Cloud Firestore](https://firebase.google.com/docs/firestore/quickstart)

2. **Video tutorial**:
   - Busca "Firebase Firestore setup" en YouTube

3. **Soporte de Firebase**:
   - [Firebase Support](https://firebase.google.com/support)

---

## ✅ Resumen

Una vez completados todos los pasos:

```
✅ Firestore Database creada
✅ Modo: Production
✅ Ubicación: [tu ubicación seleccionada]
✅ Reglas de seguridad publicadas
✅ Colección "scripts" lista (opcional)
✅ Listo para usar con la app
```

**¡Tu base de datos está lista para ScriptMine!** 🎉

---

**Última actualización**: Noviembre 8, 2025
**Versión**: 1.0
**Estado**: Production Ready 🚀
