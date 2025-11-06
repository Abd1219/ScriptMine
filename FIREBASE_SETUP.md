# 🔥 Firebase Setup Guide for ScriptMine

## 📋 Prerequisites

1. **Google Account** - Para acceder a Firebase Console
2. **Android Studio** - Con el proyecto ScriptMine abierto

## 🚀 Firebase Project Setup

### 1. Create Firebase Project

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Haz clic en **"Add project"**
3. Nombre del proyecto: `ScriptMine` (o el que prefieras)
4. Habilita Google Analytics (opcional pero recomendado)
5. Selecciona tu cuenta de Analytics
6. Haz clic en **"Create project"**

### 2. Add Android App to Firebase

1. En el dashboard del proyecto, haz clic en el ícono de Android
2. **Package name**: `com.abdapps.scriptmine`
3. **App nickname**: `ScriptMine Android`
4. **Debug signing certificate SHA-1**: (opcional por ahora)
5. Haz clic en **"Register app"**

### 3. Download Configuration File

1. Descarga el archivo `google-services.json`
2. **IMPORTANTE**: Reemplaza el archivo placeholder en `app/google-services.json`
3. El archivo debe estar exactamente en: `ScriptMine/app/google-services.json`

### 4. Enable Firebase Services

#### 🔐 Authentication
1. Ve a **Authentication** > **Sign-in method**
2. Habilita **Google** como proveedor
3. Configura el email de soporte del proyecto
4. Guarda los cambios

#### 🗄️ Firestore Database
1. Ve a **Firestore Database**
2. Haz clic en **"Create database"**
3. Selecciona **"Start in test mode"** (cambiaremos las reglas después)
4. Selecciona la ubicación más cercana (ej: `us-central1`)
5. Haz clic en **"Done"**

#### 📊 Analytics (Opcional)
1. Ve a **Analytics** > **Dashboard**
2. Verifica que esté habilitado
3. Configura eventos personalizados si es necesario

## 🔒 Security Rules Setup

### Firestore Security Rules

Ve a **Firestore Database** > **Rules** y reemplaza con:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Scripts collection - solo el usuario propietario puede acceder
    match /scripts/{scriptId} {
      allow read, write: if request.auth != null && 
                        request.auth.uid == resource.data.userId;
      allow create: if request.auth != null && 
                   request.auth.uid == request.resource.data.userId;
    }
    
    // Permitir lectura de metadatos para usuarios autenticados
    match /metadata/{document=**} {
      allow read: if request.auth != null;
    }
  }
}
```

## 🔧 Google Sign-In Configuration

### 1. Get OAuth 2.0 Client ID

1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Selecciona tu proyecto Firebase
3. Ve a **APIs & Services** > **Credentials**
4. Busca el **OAuth 2.0 Client ID** para Android
5. Copia el **Client ID** (lo necesitarás para la configuración)

### 2. Configure SHA-1 (Para Release)

Para builds de release, necesitarás agregar el SHA-1:

```bash
# Generar SHA-1 para debug
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android

# Para release keystore
keytool -list -v -keystore path/to/your/release.keystore -alias your_alias
```

Agrega el SHA-1 en Firebase Console > Project Settings > Your apps > SHA certificate fingerprints

## 📱 Test Firebase Connection

### 1. Build and Run

```bash
./gradlew assembleDebug
```

### 2. Check Logs

Busca en los logs de Android Studio:
- `Firebase initialized successfully`
- `Firestore initialized`
- `Auth initialized`

### 3. Test Authentication

1. Ejecuta la app
2. Intenta hacer sign-in con Google
3. Verifica en Firebase Console > Authentication > Users

## 🚨 Troubleshooting

### Common Issues

1. **"google-services.json not found"**
   - Verifica que el archivo esté en `app/google-services.json`
   - Haz clean y rebuild del proyecto

2. **"SHA-1 mismatch"**
   - Agrega el SHA-1 correcto en Firebase Console
   - Descarga nuevo `google-services.json`

3. **"Permission denied"**
   - Verifica las reglas de Firestore
   - Asegúrate de que el usuario esté autenticado

4. **"Network error"**
   - Verifica permisos de INTERNET en AndroidManifest
   - Comprueba la conectividad de red

### Debug Commands

```bash
# Limpiar y reconstruir
./gradlew clean assembleDebug

# Ver logs de Firebase
adb logcat | grep -i firebase

# Verificar dependencias
./gradlew app:dependencies
```

## ✅ Verification Checklist

- [ ] Proyecto Firebase creado
- [ ] App Android agregada al proyecto
- [ ] `google-services.json` descargado y colocado correctamente
- [ ] Authentication habilitado con Google Sign-In
- [ ] Firestore Database creado
- [ ] Security Rules configuradas
- [ ] SHA-1 agregado (para release)
- [ ] App compila sin errores
- [ ] Firebase se inicializa correctamente

## 🔄 Next Steps

Una vez completado el setup:

1. **Test Authentication**: Implementar y probar Google Sign-In
2. **Test Firestore**: Crear, leer, actualizar documentos
3. **Setup Sync**: Implementar sincronización offline-first
4. **Monitor Usage**: Configurar alertas y monitoreo

## 📞 Support

Si encuentras problemas:
1. Revisa la [documentación oficial de Firebase](https://firebase.google.com/docs/android/setup)
2. Verifica los logs de Android Studio
3. Consulta el troubleshooting arriba