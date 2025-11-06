# Design Document

## Overview

La implementación híbrida offline-first para ScriptMine integra Firebase Firestore como backend en la nube manteniendo Room como base de datos local primaria. El diseño prioriza la disponibilidad offline, sincronización inteligente y experiencia de usuario fluida.

## Architecture

### High-Level Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   UI Layer      │    │  ViewModel       │    │ Repository      │
│  (Compose)      │◄──►│  (StateFlow)     │◄──►│ (Hybrid)        │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                                         │
                                    ┌────────────────────┼────────────────────┐
                                    │                    │                    │
                            ┌───────▼────────┐  ┌────────▼────────┐  ┌───────▼────────┐
                            │ Local Database │  │ Firebase Repo   │  │ Sync Manager   │
                            │    (Room)      │  │  (Firestore)    │  │ (WorkManager)  │
                            └────────────────┘  └─────────────────┘  └────────────────┘
```

### Data Flow Architecture

```
┌─────────────┐     ┌─────────────────┐     ┌──────────────┐     ┌─────────────────┐
│    User     │────►│ Hybrid          │────►│    Local     │────►│   Background    │
│  Interaction│     │ Repository      │     │   Database   │     │ Sync (Worker)   │
└─────────────┘     └─────────────────┘     └──────────────┘     └─────────────────┘
                             │                                             │
                             │                                             │
                             ▼                                             ▼
                    ┌─────────────────┐                          ┌─────────────────┐
                    │   Firebase      │◄─────────────────────────│   Conflict      │
                    │   Firestore     │                          │   Resolution    │
                    └─────────────────┘                          └─────────────────┘
```

## Components and Interfaces

### 1. Enhanced SavedScript Entity

```kotlin
@Entity(tableName = "saved_scripts")
data class SavedScript(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val firebaseId: String? = null,           // Firebase document ID
    val templateType: String,
    val clientName: String,
    val formData: String,
    val generatedScript: String,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val userId: String? = null,               // Firebase user ID
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val lastSyncAt: Date? = null,
    val version: Int = 1,                     // For conflict resolution
    val isDeleted: Boolean = false            // Soft delete for sync
)

enum class SyncStatus {
    PENDING,      // Needs to be synced
    SYNCING,      // Currently syncing
    SYNCED,       // Successfully synced
    CONFLICT,     // Conflict detected
    ERROR         // Sync failed
}
```

### 2. Firebase Script Model

```kotlin
data class FirebaseScript(
    val id: String = "",
    val templateType: String = "",
    val clientName: String = "",
    val formData: Map<String, Any> = emptyMap(),
    val generatedScript: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val userId: String = "",
    val version: Int = 1,
    val isDeleted: Boolean = false
) {
    fun toSavedScript(localId: Long = 0): SavedScript {
        return SavedScript(
            id = localId,
            firebaseId = id,
            templateType = templateType,
            clientName = clientName,
            formData = Json.encodeToString(formData),
            generatedScript = generatedScript,
            createdAt = createdAt?.toDate() ?: Date(),
            updatedAt = updatedAt?.toDate() ?: Date(),
            userId = userId,
            syncStatus = SyncStatus.SYNCED,
            lastSyncAt = Date(),
            version = version,
            isDeleted = isDeleted
        )
    }
}
```

### 3. Enhanced DAO Interface

```kotlin
@Dao
interface ScriptDao {
    // Existing methods...
    
    // Sync-specific methods
    @Query("SELECT * FROM saved_scripts WHERE syncStatus = :status")
    suspend fun getScriptsByStatus(status: SyncStatus): List<SavedScript>
    
    @Query("SELECT * FROM saved_scripts WHERE syncStatus IN (:statuses)")
    suspend fun getUnsyncedScripts(statuses: List<SyncStatus> = listOf(SyncStatus.PENDING, SyncStatus.ERROR)): List<SavedScript>
    
    @Query("UPDATE saved_scripts SET syncStatus = :status, lastSyncAt = :syncTime WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, status: SyncStatus, syncTime: Date = Date())
    
    @Query("SELECT * FROM saved_scripts WHERE firebaseId = :firebaseId")
    suspend fun getScriptByFirebaseId(firebaseId: String): SavedScript?
    
    @Query("UPDATE saved_scripts SET isDeleted = 1, updatedAt = :deleteTime WHERE id = :id")
    suspend fun softDelete(id: Long, deleteTime: Date = Date())
}
```

### 4. Firebase Repository

```kotlin
interface FirebaseScriptRepository {
    suspend fun uploadScript(script: SavedScript): Result<String>
    suspend fun downloadScript(firebaseId: String): Result<FirebaseScript>
    suspend fun downloadUserScripts(userId: String): Result<List<FirebaseScript>>
    suspend fun deleteScript(firebaseId: String): Result<Unit>
    suspend fun listenToUserScripts(userId: String): Flow<List<FirebaseScript>>
}

class FirebaseScriptRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : FirebaseScriptRepository {
    
    private val scriptsCollection = firestore.collection("scripts")
    
    override suspend fun uploadScript(script: SavedScript): Result<String> {
        return try {
            val firebaseScript = script.toFirebaseScript()
            val docRef = if (script.firebaseId != null) {
                scriptsCollection.document(script.firebaseId)
            } else {
                scriptsCollection.document()
            }
            
            docRef.set(firebaseScript).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun downloadUserScripts(userId: String): Result<List<FirebaseScript>> {
        return try {
            val snapshot = scriptsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isDeleted", false)
                .get()
                .await()
            
            val scripts = snapshot.documents.mapNotNull { doc ->
                doc.toObject<FirebaseScript>()?.copy(id = doc.id)
            }
            
            Result.success(scripts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### 5. Hybrid Repository

```kotlin
class HybridScriptRepository(
    private val localDao: ScriptDao,
    private val firebaseRepo: FirebaseScriptRepository,
    private val syncManager: SyncManager,
    private val conflictResolver: ConflictResolver,
    private val networkMonitor: NetworkMonitor
) : ScriptRepository {
    
    override suspend fun insertScript(script: SavedScript): Long {
        // 1. Always save locally first (offline-first)
        val localId = localDao.insertScript(
            script.copy(syncStatus = SyncStatus.PENDING)
        )
        
        // 2. Attempt immediate sync if online
        if (networkMonitor.isOnline()) {
            syncManager.syncScript(localId)
        }
        
        return localId
    }
    
    override suspend fun updateScript(script: SavedScript) {
        // Update locally with pending sync status
        localDao.updateScript(
            script.copy(
                syncStatus = SyncStatus.PENDING,
                updatedAt = Date(),
                version = script.version + 1
            )
        )
        
        // Attempt sync if online
        if (networkMonitor.isOnline()) {
            syncManager.syncScript(script.id)
        }
    }
    
    override fun getAllScripts(): Flow<List<SavedScript>> {
        return localDao.getAllScripts().map { scripts ->
            scripts.filter { !it.isDeleted }
        }
    }
    
    suspend fun syncWithFirebase(): Result<Unit> {
        return syncManager.performFullSync()
    }
}
```

### 6. Sync Manager

```kotlin
class SyncManager(
    private val localDao: ScriptDao,
    private val firebaseRepo: FirebaseScriptRepository,
    private val conflictResolver: ConflictResolver,
    private val auth: FirebaseAuth
) {
    
    suspend fun syncScript(localId: Long): Result<Unit> {
        return try {
            val localScript = localDao.getScriptById(localId) ?: return Result.failure(Exception("Script not found"))
            
            // Update sync status
            localDao.updateSyncStatus(localId, SyncStatus.SYNCING)
            
            // Upload to Firebase
            val result = firebaseRepo.uploadScript(localScript)
            
            if (result.isSuccess) {
                val firebaseId = result.getOrThrow()
                localDao.updateScript(
                    localScript.copy(
                        firebaseId = firebaseId,
                        syncStatus = SyncStatus.SYNCED,
                        lastSyncAt = Date()
                    )
                )
                Result.success(Unit)
            } else {
                localDao.updateSyncStatus(localId, SyncStatus.ERROR)
                result.map { }
            }
        } catch (e: Exception) {
            localDao.updateSyncStatus(localId, SyncStatus.ERROR)
            Result.failure(e)
        }
    }
    
    suspend fun performFullSync(): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            
            // 1. Upload pending local changes
            val pendingScripts = localDao.getUnsyncedScripts()
            pendingScripts.forEach { script ->
                syncScript(script.id)
            }
            
            // 2. Download remote changes
            val remoteResult = firebaseRepo.downloadUserScripts(userId)
            if (remoteResult.isSuccess) {
                val remoteScripts = remoteResult.getOrThrow()
                
                remoteScripts.forEach { remoteScript ->
                    val localScript = localDao.getScriptByFirebaseId(remoteScript.id)
                    
                    when {
                        localScript == null -> {
                            // New remote script
                            localDao.insertScript(remoteScript.toSavedScript())
                        }
                        localScript.version < remoteScript.version -> {
                            // Remote is newer
                            localDao.updateScript(remoteScript.toSavedScript(localScript.id))
                        }
                        localScript.version > remoteScript.version -> {
                            // Local is newer, upload it
                            syncScript(localScript.id)
                        }
                        else -> {
                            // Same version, check for conflicts
                            if (conflictResolver.hasConflict(localScript, remoteScript)) {
                                val resolved = conflictResolver.resolve(localScript, remoteScript)
                                localDao.updateScript(resolved)
                            }
                        }
                    }
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### 7. Conflict Resolution

```kotlin
class ConflictResolver {
    
    fun hasConflict(local: SavedScript, remote: FirebaseScript): Boolean {
        return local.version == remote.version && 
               (local.formData != Json.encodeToString(remote.formData) ||
                local.generatedScript != remote.generatedScript)
    }
    
    fun resolve(local: SavedScript, remote: FirebaseScript): SavedScript {
        return when {
            // Most recent wins
            local.updatedAt > (remote.updatedAt?.toDate() ?: Date(0)) -> {
                local.copy(syncStatus = SyncStatus.PENDING) // Will be uploaded
            }
            
            // Remote is more recent
            (remote.updatedAt?.toDate() ?: Date(0)) > local.updatedAt -> {
                remote.toSavedScript(local.id)
            }
            
            // Same timestamp - merge or mark as conflict
            else -> {
                local.copy(syncStatus = SyncStatus.CONFLICT)
            }
        }
    }
}
```

### 8. Background Sync Worker

```kotlin
class SyncWorker(
    context: Context,
    params: WorkerParameters,
    private val syncManager: SyncManager
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            val syncResult = syncManager.performFullSync()
            
            if (syncResult.isSuccess) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
    
    companion object {
        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
            
            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "script_sync",
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }
    }
}
```

## Data Models

### Local Database Schema Updates

```sql
-- Add new columns to existing table
ALTER TABLE saved_scripts ADD COLUMN firebaseId TEXT;
ALTER TABLE saved_scripts ADD COLUMN userId TEXT;
ALTER TABLE saved_scripts ADD COLUMN syncStatus INTEGER DEFAULT 0;
ALTER TABLE saved_scripts ADD COLUMN lastSyncAt INTEGER;
ALTER TABLE saved_scripts ADD COLUMN version INTEGER DEFAULT 1;
ALTER TABLE saved_scripts ADD COLUMN isDeleted INTEGER DEFAULT 0;

-- Create indexes for performance
CREATE INDEX idx_sync_status ON saved_scripts(syncStatus);
CREATE INDEX idx_firebase_id ON saved_scripts(firebaseId);
CREATE INDEX idx_user_id ON saved_scripts(userId);
```

### Firebase Firestore Structure

```
scripts/ (collection)
├── {scriptId}/ (document)
│   ├── templateType: string
│   ├── clientName: string
│   ├── formData: map
│   ├── generatedScript: string
│   ├── createdAt: timestamp
│   ├── updatedAt: timestamp
│   ├── userId: string
│   ├── version: number
│   └── isDeleted: boolean
```

## Error Handling

### Network Error Handling

```kotlin
class NetworkErrorHandler {
    fun handleFirebaseError(exception: Exception): SyncError {
        return when (exception) {
            is FirebaseNetworkException -> SyncError.NETWORK_ERROR
            is FirebaseAuthException -> SyncError.AUTH_ERROR
            is FirebaseFirestoreException -> {
                when (exception.code) {
                    FirebaseFirestoreException.Code.PERMISSION_DENIED -> SyncError.PERMISSION_ERROR
                    FirebaseFirestoreException.Code.QUOTA_EXCEEDED -> SyncError.QUOTA_ERROR
                    else -> SyncError.UNKNOWN_ERROR
                }
            }
            else -> SyncError.UNKNOWN_ERROR
        }
    }
}

enum class SyncError {
    NETWORK_ERROR,
    AUTH_ERROR,
    PERMISSION_ERROR,
    QUOTA_ERROR,
    CONFLICT_ERROR,
    UNKNOWN_ERROR
}
```

### Retry Strategy

```kotlin
class RetryStrategy {
    suspend fun <T> retryWithBackoff(
        maxRetries: Int = 3,
        initialDelay: Long = 1000,
        maxDelay: Long = 10000,
        factor: Double = 2.0,
        operation: suspend () -> T
    ): Result<T> {
        var currentDelay = initialDelay
        
        repeat(maxRetries) { attempt ->
            try {
                return Result.success(operation())
            } catch (e: Exception) {
                if (attempt == maxRetries - 1) {
                    return Result.failure(e)
                }
                
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
            }
        }
        
        return Result.failure(Exception("Max retries exceeded"))
    }
}
```

## Testing Strategy

### Unit Tests

1. **Repository Tests**
   - Test offline-first behavior
   - Test sync logic
   - Test conflict resolution
   - Mock Firebase dependencies

2. **Sync Manager Tests**
   - Test upload/download scenarios
   - Test error handling
   - Test retry mechanisms

3. **Conflict Resolver Tests**
   - Test various conflict scenarios
   - Test resolution strategies

### Integration Tests

1. **Database Migration Tests**
   - Test schema updates
   - Test data preservation

2. **Firebase Integration Tests**
   - Test authentication flow
   - Test Firestore operations
   - Test real-time listeners

### End-to-End Tests

1. **Offline Scenarios**
   - Create/edit scripts offline
   - Sync when coming online

2. **Multi-device Scenarios**
   - Sync between devices
   - Conflict resolution

## Security Considerations

### Data Encryption

```kotlin
class DataEncryption {
    private val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    
    fun encrypt(data: String, key: SecretKey): String {
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedData = cipher.doFinal(data.toByteArray())
        val iv = cipher.iv
        return Base64.encodeToString(iv + encryptedData, Base64.DEFAULT)
    }
    
    fun decrypt(encryptedData: String, key: SecretKey): String {
        val data = Base64.decode(encryptedData, Base64.DEFAULT)
        val iv = data.sliceArray(0..11)
        val encrypted = data.sliceArray(12 until data.size)
        
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        
        return String(cipher.doFinal(encrypted))
    }
}
```

### Firebase Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /scripts/{scriptId} {
      allow read, write: if request.auth != null && 
                        request.auth.uid == resource.data.userId;
      allow create: if request.auth != null && 
                   request.auth.uid == request.resource.data.userId;
    }
  }
}
```

## Performance Optimizations

### Batch Operations

```kotlin
class BatchSyncManager {
    suspend fun batchUpload(scripts: List<SavedScript>): Result<Unit> {
        return try {
            val batch = firestore.batch()
            
            scripts.forEach { script ->
                val docRef = scriptsCollection.document(script.firebaseId ?: UUID.randomUUID().toString())
                batch.set(docRef, script.toFirebaseScript())
            }
            
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Caching Strategy

```kotlin
class CacheManager {
    private val memoryCache = LruCache<String, FirebaseScript>(50)
    
    fun getCachedScript(firebaseId: String): FirebaseScript? {
        return memoryCache.get(firebaseId)
    }
    
    fun cacheScript(script: FirebaseScript) {
        memoryCache.put(script.id, script)
    }
}
```

Este diseño proporciona una base sólida para la implementación híbrida offline-first, priorizando la experiencia del usuario y la integridad de los datos mientras mantiene la escalabilidad y el rendimiento.