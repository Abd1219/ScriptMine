# 🔥 Configuración de Firebase Console - Guía Completa

## 📋 Índice
1. [Reglas de Seguridad de Firestore](#1-reglas-de-seguridad-de-firestore)
2. [Configuración de Authentication](#2-configuración-de-authentication)
3. [Índices de Firestore](#3-índices-de-firestore)
4. [Configuración de SHA-1](#4-configuración-de-sha-1)
5. [Verificación Final](#5-verificación-final)

---

## 1. Reglas de Seguridad de Firestore

### Paso 1: Acceder a Firestore Rules

1. Ve a [Firebase Console](https://console.firebase.google.com)
2. Selecciona tu proyecto **ScriptMine**
3. En el menú lateral, haz clic en **Firestore Database**
4. Ve a la pestaña **Rules** (Reglas)

### Paso 2: Copiar y Pegar las Reglas

Reemplaza todo el contenido con estas reglas:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Scripts collection - User can only access their own scripts
    match /scripts/{scriptId} {
      // Allow read and write only if user is authenticated and owns the script
      allow read, write: if request.auth != null && 
                        request.auth.uid == resource.data.userId;
      
      // Allow create only if user is authenticated and sets themselves as owner
      allow create: if request.auth != null && 
                   request.auth.uid == request.resource.data.userId &&
                   isValidScriptData(request.resource.data);
      
      // Allow update only if user owns the script and maintains ownership
      allow update: if request.auth != null && 
                   request.auth.uid == resource.data.userId &&
                   request.auth.uid == request.resource.data.userId &&
                   isValidScriptData(request.resource.data);
    }
    
    // User metadata collection (for future use)
    match /users/{userId} {
      allow read, write: if request.auth != null && 
                        request.auth.uid == userId;
    }
    
    // System metadata (read-only for authenticated users)
    match /metadata/{document=**} {
      allow read: if request.auth != null;
    }
  }
  
  // Validation functions
  function isValidScriptData(data) {
    return data.keys().hasAll(['templateType', 'clientName', 'formData', 'generatedScript', 'userId']) &&
           data.templateType is string &&
           data.clientName is string &&
           data.formData is map &&
           data.generatedScript is string &&
           data.userId is string &&
           data.version is int &&
           data.version > 0 &&
           data.isDeleted is bool;
  }
  
  // Additional security: Prevent excessive writes
  function isRateLimited() {
    return request.time > resource.data.updatedAt + duration.value(1, 's');
  }
}
```

### Paso 3: Publicar las Reglas

1. Haz clic en el botón **Publish** (Publicar)
2. Confirma la publicación
3. Espera a que se apliquen (toma unos segundos)

### ✅ Verificación de Reglas

Las reglas deben mostrar:
- ✅ Sin errores de sintaxis
- ✅ Estado: "Published" (Publicado)
- ✅ Fecha de última actualización

---

## 2. Configuración de Authentication

### Paso 1: Habilitar Google Sign-In

1. En Firebase Console, ve a **Authentication**
2. Haz clic en la pestaña **Sign-in method**
3. Busca **Google** en la lista de proveedores
4. Haz clic en **Google**
5. Activa el toggle **Enable** (Habilitar)
6. Configura:
   - **Project support email**: Tu email
   - **Project public-facing name**: ScriptMine
7. Haz clic en **Save** (Guardar)

### Paso 2: Configurar Email de Soporte (Opcional)

1. En la misma pantalla de Authentication
2. Ve a **Settings** (Configuración)
3. Configura el **Support email**
4. Guarda los cambios

### ✅ Verificación de Authentication

Deberías ver:
- ✅ Google: Enabled (Habilitado)
- ✅ Estado: Active
- ✅ Email de soporte configurado

---

## 3. Índices de Firestore

### Índices Necesarios

Los índices mejoran el rendimiento de las consultas. Crea estos índices:

#### Índice 1: userId + lastModified (DESC)

1. Ve a **Firestore Database** → **Indexes**
2. Haz clic en **Create Index**
3. Configura:
   - **Collection ID**: `scripts`
   - **Fields to index**:
     - Campo 1: `userId` - Ascending
     - Campo 2: `lastModified` - Descending
   - **Query scope**: Collection
4. Haz clic en **Create**

#### Índice 2: userId + syncStatus + lastModified

1. Haz clic en **Create Index** nuevamente
2. Configura:
   - **Collection ID**: `scripts`
   - **Fields to index**:
     - Campo 1: `userId` - Ascending
     - Campo 2: `syncStatus` - Ascending
     - Campo 3: `lastModified` - Descending
   - **Query scope**: Collection
3. Haz clic en **Create**

#### Índice 3: userId + isDeleted + lastModified

1. Haz clic en **Create Index** nuevamente
2. Configura:
   - **Collection ID**: `scripts`
   - **Fields to index**:
     - Campo 1: `userId` - Ascending
     - Campo 2: `isDeleted` - Ascending
     - Campo 3: `lastModified` - Descending
   - **Query scope**: Collection
3. Haz clic en **Create**

### ⏳ Tiempo de Creación

Los índices pueden tardar varios minutos en crearse. Verás:
- 🟡 **Building**: En proceso
- ✅ **Enabled**: Listo para usar

---

## 4. Configuración de SHA-1

### Paso 1: Obtener SHA-1 Certificate

#### Para Debug Keystore (Desarrollo):

**En Windows:**
```bash
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

**En Mac/Linux:**
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

#### Para Release Keystore (Producción):

```bash
keytool -list -v -keystore /path/to/your/release.keystore -alias your-alias
```

### Paso 2: Copiar SHA-1

Busca en la salida del comando:
```
Certificate fingerprints:
     SHA1: XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX
     SHA256: ...
```

Copia el valor del **SHA1** (los XX:XX:XX...)

### Paso 3: Agregar SHA-1 a Firebase

1. Ve a **Project Settings** (⚙️ en la esquina superior izquierda)
2. Baja hasta la sección **Your apps**
3. Selecciona tu app Android (com.abdapps.scriptmine)
4. En la sección **SHA certificate fingerprints**
5. Haz clic en **Add fingerprint**
6. Pega el SHA-1 que copiaste
7. Haz clic en **Save**

### Paso 4: Descargar Nuevo google-services.json

1. En la misma pantalla de Project Settings
2. Haz clic en el botón **Download google-services.json**
3. Reemplaza el archivo en tu proyecto:
   ```
   app/google-services.json
   ```
4. Sincroniza el proyecto en Android Studio

### ✅ Verificación de SHA-1

Deberías ver:
- ✅ SHA-1 fingerprint agregado
- ✅ google-services.json actualizado
- ✅ Sin errores de sincronización en Android Studio

---

## 5. Verificación Final

### Checklist de Configuración

Verifica que todo esté configurado correctamente:

#### Firestore Database
- [ ] Reglas de seguridad publicadas
- [ ] Índices creados (3 índices)
- [ ] Índices en estado "Enabled"

#### Authentication
- [ ] Google Sign-In habilitado
- [ ] Email de soporte configurado
- [ ] Sin errores en la configuración

#### App Configuration
- [ ] SHA-1 agregado (debug y/o release)
- [ ] google-services.json actualizado
- [ ] Proyecto sincronizado en Android Studio

#### Testing
- [ ] App compila sin errores
- [ ] App se ejecuta sin crashes
- [ ] (Opcional) Google Sign-In funciona

---

## 📊 Estructura de Datos en Firestore

### Colección: scripts

Cada documento de script tiene esta estructura:

```javascript
{
  // Identificadores
  "firebaseId": "string",           // ID del documento en Firestore
  "userId": "string",               // UID del usuario propietario
  
  // Datos del script
  "templateType": "string",         // Tipo de template (INTERVENTION, SOPORTE, etc.)
  "clientName": "string",           // Nombre del cliente
  "formData": {                     // Datos del formulario (map)
    "field1": "value1",
    "field2": "value2"
  },
  "generatedScript": "string",      // Script generado
  
  // Metadatos de sincronización
  "version": 1,                     // Versión del documento
  "syncStatus": "SYNCED",           // Estado: SYNCED, PENDING, ERROR
  "isDeleted": false,               // Soft delete flag
  
  // Timestamps
  "createdAt": Timestamp,           // Fecha de creación
  "lastModified": Timestamp,        // Última modificación
  "lastSyncAt": Timestamp           // Última sincronización
}
```

### Colección: users (Futura)

```javascript
{
  "userId": "string",
  "email": "string",
  "displayName": "string",
  "photoUrl": "string",
  "createdAt": Timestamp,
  "lastLoginAt": Timestamp,
  "preferences": {
    "syncFrequency": 15,
    "wifiOnly": true,
    "autoSync": true
  }
}
```

---

## 🔒 Seguridad Implementada

### Características de Seguridad

1. **Autenticación Requerida**
   - Solo usuarios autenticados pueden acceder a Firestore
   - Sin autenticación = sin acceso a datos

2. **Aislamiento de Datos**
   - Cada usuario solo puede ver sus propios scripts
   - No puede leer ni modificar scripts de otros usuarios

3. **Validación de Datos**
   - Todos los campos requeridos deben estar presentes
   - Tipos de datos validados
   - Versión debe ser > 0

4. **Prevención de Modificaciones Maliciosas**
   - No se puede cambiar el userId de un script
   - No se puede asignar scripts a otros usuarios

5. **Rate Limiting**
   - Previene escrituras excesivas
   - Mínimo 1 segundo entre actualizaciones

---

## 🚨 Troubleshooting

### Problema: "Permission Denied" en Firestore

**Causa**: Usuario no autenticado o reglas mal configuradas

**Solución**:
1. Verifica que el usuario esté autenticado
2. Revisa las reglas de Firestore
3. Asegúrate de que el userId coincida

### Problema: "Index Required" Error

**Causa**: Falta crear un índice para la consulta

**Solución**:
1. Firebase te dará un link en el error
2. Haz clic en el link para crear el índice automáticamente
3. Espera a que se complete la creación

### Problema: Google Sign-In No Funciona

**Causa**: SHA-1 no configurado o incorrecto

**Solución**:
1. Verifica que el SHA-1 esté agregado en Firebase
2. Descarga el nuevo google-services.json
3. Limpia y reconstruye el proyecto
4. Desinstala la app del dispositivo y reinstala

### Problema: "API Key Not Valid"

**Causa**: google-services.json desactualizado

**Solución**:
1. Descarga el google-services.json más reciente
2. Reemplaza el archivo en app/
3. Sincroniza el proyecto
4. Limpia y reconstruye

---

## 📱 Testing de la Configuración

### Test 1: Verificar Reglas de Firestore

1. Ve a Firestore Database → Rules
2. Haz clic en **Simulator**
3. Prueba estas operaciones:

**Test de Lectura (Sin Auth):**
```
Location: /scripts/test123
Type: get
Auth: Not signed in
Expected: ❌ Denied
```

**Test de Lectura (Con Auth):**
```
Location: /scripts/test123
Type: get
Auth: Signed in (tu UID)
Expected: ✅ Allowed (si userId coincide)
```

### Test 2: Verificar Authentication

1. Ejecuta la app en un dispositivo/emulador
2. Intenta hacer Google Sign-In
3. Verifica que:
   - ✅ Se abre el selector de cuenta de Google
   - ✅ Puedes seleccionar una cuenta
   - ✅ La app recibe el token de autenticación
   - ✅ No hay crashes

### Test 3: Verificar Sync

1. Crea un script en la app
2. Verifica en Firestore Console que aparece
3. Modifica el script
4. Verifica que se actualiza en Firestore
5. Elimina el script
6. Verifica que isDeleted = true en Firestore

---

## 🎯 Resumen de Configuración

### Configuración Mínima (Solo Offline)
- ✅ App funciona sin Firebase
- ✅ Datos guardados localmente
- ✅ No requiere configuración

### Configuración Completa (Con Sync)
1. ✅ Reglas de Firestore publicadas
2. ✅ Google Sign-In habilitado
3. ✅ SHA-1 configurado
4. ✅ google-services.json actualizado
5. ✅ Índices creados
6. ✅ Testing completado

---

## 📞 Soporte

Si tienes problemas con la configuración:

1. **Revisa los logs de Android Studio**
   - Busca errores de Firebase
   - Verifica mensajes de autenticación

2. **Consulta la documentación oficial**
   - [Firebase Android Setup](https://firebase.google.com/docs/android/setup)
   - [Firestore Security Rules](https://firebase.google.com/docs/firestore/security/get-started)
   - [Google Sign-In](https://firebase.google.com/docs/auth/android/google-signin)

3. **Verifica el estado de Firebase**
   - [Firebase Status Dashboard](https://status.firebase.google.com/)

---

**Última actualización**: Noviembre 8, 2025
**Versión de la app**: 2.0.0 (Hybrid Sync)
**Estado**: Production Ready 🚀
