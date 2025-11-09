# Error Handling and Resilience Implementation

## Overview
This document describes the comprehensive error handling and resilience system implemented in ScriptMine.

## Components Implemented

### 1. AppError (Sealed Class Hierarchy)
**Location**: `app/src/main/java/com/abdapps/scriptmine/error/AppError.kt`

**Features**:
- Type-safe error representation
- Hierarchical error categories
- User-friendly error messages
- Recoverability indicators
- User action requirements

**Error Categories**:

#### NetworkError
- `NoConnection` - No internet connection
- `Timeout` - Request timeout
- `ServerError` - HTTP server errors
- `UnknownError` - Unknown network issues

#### FirebaseError
- `AuthenticationFailed` - Firebase auth failure
- `PermissionDenied` - Access denied
- `DocumentNotFound` - Document not found
- `QuotaExceeded` - Quota limits reached
- `RateLimitExceeded` - Too many requests
- `FirestoreError` - General Firestore errors

#### DatabaseError
- `InsertFailed` - Insert operation failed
- `UpdateFailed` - Update operation failed
- `DeleteFailed` - Delete operation failed
- `QueryFailed` - Query execution failed
- `MigrationFailed` - Database migration failed
- `CorruptedData` - Data corruption detected

#### SyncError
- `SyncFailed` - General sync failure
- `ConflictDetected` - Sync conflict found
- `ConflictResolutionFailed` - Failed to resolve conflict
- `UploadFailed` - Upload operation failed
- `DownloadFailed` - Download operation failed
- `SyncTimeout` - Sync operation timeout

#### ValidationError
- `InvalidInput` - Invalid user input
- `RequiredFieldMissing` - Required field empty
- `InvalidFormat` - Wrong format
- `ValueOutOfRange` - Value out of bounds

#### AuthError
- `NotAuthenticated` - User not logged in
- `SessionExpired` - Session expired
- `InvalidCredentials` - Wrong credentials
- `AccountDisabled` - Account disabled
- `AccountDeleted` - Account deleted

#### SecurityError
- `EncryptionFailed` - Encryption failed
- `DecryptionFailed` - Decryption failed
- `KeyGenerationFailed` - Key generation failed
- `DangerousInputDetected` - Malicious input detected

#### StorageError
- `InsufficientSpace` - Not enough storage
- `FileNotFound` - File not found
- `ReadFailed` - File read failed
- `WriteFailed` - File write failed

**Usage**:
```kotlin
// Create error
val error = AppError.NetworkError.NoConnection()

// Get user message
val message = error.toUserMessage()

// Check if recoverable
if (error.isRecoverable()) {
    // Retry operation
}

// Check if requires user action
if (error.requiresUserAction()) {
    // Show dialog to user
}
```

### 2. ErrorHandler
**Location**: `app/src/main/java/com/abdapps/scriptmine/error/ErrorHandler.kt`

**Features**:
- Centralized exception handling
- Exception to AppError conversion
- Automatic logging
- Context-aware error handling
- Result wrapper functions

**Usage**:
```kotlin
@Inject lateinit var errorHandler: ErrorHandler

// Handle exception
try {
    riskyOperation()
} catch (e: Exception) {
    val error = errorHandler.handleException(e, "MyOperation")
    showError(error.toUserMessage())
}

// With error handling wrapper
val result = errorHandler.withErrorHandling("SaveScript") {
    saveScript(script)
}

// With AppError result
val errorResult = errorHandler.withAppErrorHandling("LoadData") {
    loadData()
}
```

**ErrorResult Type**:
```kotlin
sealed class ErrorResult<out T> {
    data class Success<T>(val data: T)
    data class Error(val error: AppError)
}

// Usage
errorResult
    .onSuccess { data -> 
        // Handle success
    }
    .onError { error ->
        // Handle error
    }
```

### 3. RetryManager
**Location**: `app/src/main/java/com/abdapps/scriptmine/error/RetryManager.kt`

**Features**:
- Exponential backoff retry logic
- Configurable retry strategies
- Jitter to prevent thundering herd
- Retryable error detection
- Pre-configured retry configs

**Retry Configuration**:
```kotlin
data class RetryConfig(
    val maxRetries: Int = 3,
    val initialDelayMs: Long = 1000L,
    val maxDelayMs: Long = 30000L,
    val backoffMultiplier: Double = 2.0,
    val retryableErrors: Set<Class<out AppError>>
)
```

**Usage**:
```kotlin
@Inject lateinit var retryManager: RetryManager

// Basic retry
val result = retryManager.withRetry(
    config = retryManager.networkRetryConfig(),
    context = "FetchData"
) { attemptNumber ->
    fetchDataFromServer()
}

// With ErrorResult
val errorResult = retryManager.withRetryErrorResult(
    config = retryManager.syncRetryConfig(),
    context = "SyncScript"
) { attemptNumber ->
    syncScriptToFirebase(script)
}
```

**Pre-configured Retry Strategies**:
- `networkRetryConfig()` - For network operations (3 retries, 1-10s delay)
- `syncRetryConfig()` - For sync operations (5 retries, 2-30s delay)
- `firebaseRetryConfig()` - For Firebase operations (3 retries, 1.5-15s delay)
- `databaseRetryConfig()` - For database operations (2 retries, 0.5-2s delay)
- `noRetryConfig()` - Fail fast, no retries

### 4. CircuitBreaker
**Location**: `app/src/main/java/com/abdapps/scriptmine/error/RetryManager.kt`

**Features**:
- Prevents repeated failures
- Automatic circuit opening/closing
- Configurable failure threshold
- Timeout-based recovery
- Per-operation circuit tracking

**States**:
- **CLOSED**: Normal operation
- **OPEN**: Rejecting requests after threshold
- **HALF-OPEN**: Testing if service recovered

**Usage**:
```kotlin
@Inject lateinit var circuitBreaker: CircuitBreaker

// Use circuit breaker
val result = circuitBreaker.withCircuitBreaker(
    circuitName = "FirebaseSync",
    failureThreshold = 5,
    timeoutMs = 60000L
) {
    syncToFirebase()
}

// Check circuit state
val state = circuitBreaker.getCircuitState("FirebaseSync")
// Returns: "CLOSED", "OPEN", or "HALF-OPEN"

// Reset circuit manually
circuitBreaker.resetCircuit("FirebaseSync")
```

## Integration Examples

### Repository with Error Handling
```kotlin
class ScriptRepository @Inject constructor(
    private val errorHandler: ErrorHandler,
    private val retryManager: RetryManager
) {
    suspend fun saveScript(script: SavedScript): ErrorResult<Unit> {
        return retryManager.withRetryErrorResult(
            config = retryManager.databaseRetryConfig(),
            context = "SaveScript"
        ) { attemptNumber ->
            dao.insertScript(script)
        }
    }
    
    suspend fun syncScript(script: SavedScript): ErrorResult<Unit> {
        return retryManager.withRetryErrorResult(
            config = retryManager.syncRetryConfig(),
            context = "SyncScript"
        ) { attemptNumber ->
            firebaseRepository.uploadScript(script)
        }
    }
}
```

### ViewModel with Error Handling
```kotlin
class ScriptViewModel @Inject constructor(
    private val repository: ScriptRepository,
    private val errorHandler: ErrorHandler
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    fun saveScript(script: SavedScript) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            repository.saveScript(script)
                .onSuccess { 
                    _uiState.value = UiState.Success
                }
                .onError { error ->
                    _uiState.value = UiState.Error(error.toUserMessage())
                    
                    if (error.requiresUserAction()) {
                        // Show dialog
                    } else if (error.isRecoverable()) {
                        // Schedule retry
                    }
                }
        }
    }
}
```

### Sync Manager with Circuit Breaker
```kotlin
class SyncManager @Inject constructor(
    private val retryManager: RetryManager,
    private val circuitBreaker: CircuitBreaker
) {
    suspend fun syncAll(): ErrorResult<Unit> {
        // Use circuit breaker to prevent repeated failures
        val circuitResult = circuitBreaker.withCircuitBreaker(
            circuitName = "FullSync",
            failureThreshold = 3,
            timeoutMs = 300000L // 5 minutes
        ) {
            // Use retry manager for individual operations
            retryManager.withRetryErrorResult(
                config = retryManager.syncRetryConfig(),
                context = "FullSync"
            ) { attemptNumber ->
                performFullSync()
            }
        }
        
        return circuitResult.toErrorResult(errorHandler, "FullSync")
    }
}
```

## Error Flow Diagram

```
User Action
    ↓
Try Operation
    ↓
Exception Thrown
    ↓
ErrorHandler.handleException()
    ↓
Convert to AppError
    ↓
Check if Retryable
    ↓
┌─────────────┬─────────────┐
│  Retryable  │ Not Retryable│
└─────────────┴─────────────┘
      ↓                ↓
RetryManager      Return Error
      ↓                ↓
Exponential      Show to User
Backoff
      ↓
Circuit Breaker
      ↓
┌─────────────┬─────────────┐
│   Success   │   Failure   │
└─────────────┴─────────────┘
      ↓                ↓
Return Result    Max Retries?
                      ↓
                 Show to User
```

## Best Practices

### 1. Always Use ErrorResult
```kotlin
// Good
suspend fun loadData(): ErrorResult<Data> {
    return errorHandler.withAppErrorHandling("LoadData") {
        // operation
    }
}

// Avoid
suspend fun loadData(): Data {
    return // operation that might throw
}
```

### 2. Configure Appropriate Retry Strategies
```kotlin
// Network operations - aggressive retry
retryManager.networkRetryConfig()

// Database operations - quick retry
retryManager.databaseRetryConfig()

// User actions - no retry
retryManager.noRetryConfig()
```

### 3. Use Circuit Breakers for External Services
```kotlin
// Protect against cascading failures
circuitBreaker.withCircuitBreaker("ExternalAPI") {
    callExternalAPI()
}
```

### 4. Provide Context in Error Handling
```kotlin
// Good - provides context
errorHandler.handleException(e, "SaveScript:UserAction")

// Less helpful
errorHandler.handleException(e)
```

### 5. Handle Errors at Appropriate Level
```kotlin
// Repository - Convert exceptions to ErrorResult
// ViewModel - Handle ErrorResult and update UI state
// UI - Display user-friendly messages
```

## Testing

### Unit Tests
```kotlin
class ErrorHandlerTest {
    @Test
    fun `converts network exception to NetworkError`() {
        val handler = ErrorHandler()
        val exception = UnknownHostException()
        
        val error = handler.handleException(exception)
        
        assertTrue(error is AppError.NetworkError.NoConnection)
    }
}

class RetryManagerTest {
    @Test
    fun `retries operation on failure`() = runTest {
        val manager = RetryManager(ErrorHandler())
        var attempts = 0
        
        val result = manager.withRetry(
            config = RetryManager.RetryConfig(maxRetries = 2)
        ) {
            attempts++
            if (attempts < 3) throw IOException()
            "success"
        }
        
        assertTrue(result is RetryManager.RetryResult.Success)
        assertEquals(3, attempts)
    }
}
```

## Performance Considerations

1. **Retry Delays**: Exponential backoff prevents overwhelming services
2. **Circuit Breakers**: Fail fast when service is down
3. **Jitter**: Prevents thundering herd problem
4. **Max Delays**: Caps retry delays at reasonable limits

## Monitoring and Logging

All errors are automatically logged with:
- Error type and message
- Context information
- Stack traces for debugging
- Attempt numbers for retries
- Circuit breaker states

## Future Enhancements

1. **Error Analytics**: Track error rates and patterns
2. **Custom Retry Strategies**: Per-operation configurations
3. **Adaptive Retry**: Adjust based on success rates
4. **Error Recovery UI**: Guided error recovery flows
5. **Offline Queue**: Queue operations when offline

## References

- [Android Error Handling Best Practices](https://developer.android.com/topic/architecture/data-layer#handle-errors)
- [Exponential Backoff](https://en.wikipedia.org/wiki/Exponential_backoff)
- [Circuit Breaker Pattern](https://martinfowler.com/bliki/CircuitBreaker.html)
- [Resilience Patterns](https://docs.microsoft.com/en-us/azure/architecture/patterns/category/resiliency)