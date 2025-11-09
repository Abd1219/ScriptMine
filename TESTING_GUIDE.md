# Testing Guide - ScriptMine Firebase Hybrid Sync

## Overview
This document provides a comprehensive testing guide for the Firebase Hybrid Sync implementation in ScriptMine.

## Testing Strategy

### 1. Unit Tests
Focus on individual components in isolation with mocked dependencies.

### 2. Integration Tests
Test interactions between components (e.g., Repository + Database).

### 3. End-to-End Tests
Test complete user flows from UI to data persistence.

## Test Coverage Goals

- **Critical Components**: 80%+ coverage
- **Business Logic**: 90%+ coverage
- **UI Components**: 60%+ coverage
- **Overall Project**: 70%+ coverage

## Key Components to Test

### Security Components
- ✅ EncryptionManager
- ✅ DataValidator
- ✅ SecureDataStore

### Error Handling
- ✅ ErrorHandler
- ✅ RetryManager
- ✅ CircuitBreaker

### Performance
- ✅ BatchOperationManager
- ✅ CacheManager
- ✅ DataCompressionManager

### Sync System
- ✅ SyncManager
- ✅ ConflictResolver
- ✅ HybridScriptRepository

### Authentication
- ✅ AuthenticationManager
- ✅ SessionManager

## Test Examples

### Security Tests

```kotlin
class EncryptionManagerTest {
    private lateinit var encryptionManager: EncryptionManager
    
    @Before
    fun setup() {
        encryptionManager = EncryptionManager()
    }
    
    @Test
    fun `encrypt and decrypt returns original value`() {
        val original = "sensitive data"
        val encrypted = encryptionManager.encrypt(original)
        val decrypted = encryptionManager.decrypt(encrypted)
        
        assertEquals(original, decrypted)
        assertNotEquals(original, encrypted)
    }
    
    @Test
    fun `encrypted data format is valid`() {
        val data = "test data"
        val encrypted = encryptionManager.encrypt(data)
        
        assertTrue(encryptionManager.isEncrypted(encrypted))
        assertTrue(encrypted.contains("]")) // IV separator
    }
    
    @Test
    fun `hash produces consistent results`() {
        val input = "test input"
        val hash1 = encryptionManager.hash(input)
        val hash2 = encryptionManager.hash(input)
        
        assertEquals(hash1, hash2)
    }
}

class DataValidatorTest {
    private lateinit var validator: DataValidator
    
    @Before
    fun setup() {
        validator = DataValidator()
    }
    
    @Test
    fun `validates email format correctly`() {
        val valid = validator.validateEmail("user@example.com")
        assertTrue(valid.isValid())
        
        val invalid = validator.validateEmail("invalid-email")
        assertTrue(invalid.isInvalid())
    }
    
    @Test
    fun `detects dangerous patterns`() {
        val dangerous = "<script>alert('xss')</script>"
        assertTrue(validator.containsDangerousPatterns(dangerous))
        
        val safe = "normal text"
        assertFalse(validator.containsDangerousPatterns(safe))
    }
    
    @Test
    fun `validates script title length`() {
        val tooLong = "a".repeat(201)
        val result = validator.validateScriptTitle(tooLong)
        
        assertTrue(result.isInvalid())
        assertTrue(result.getErrorOrNull()?.contains("too long") == true)
    }
}
```

### Error Handling Tests

```kotlin
class ErrorHandlerTest {
    private lateinit var errorHandler: ErrorHandler
    
    @Before
    fun setup() {
        errorHandler = ErrorHandler()
    }
    
    @Test
    fun `converts network exception to NetworkError`() {
        val exception = UnknownHostException()
        val error = errorHandler.handleException(exception)
        
        assertTrue(error is AppError.NetworkError.NoConnection)
    }
    
    @Test
    fun `converts timeout exception to Timeout error`() {
        val exception = SocketTimeoutException()
        val error = errorHandler.handleException(exception)
        
        assertTrue(error is AppError.NetworkError.Timeout)
    }
    
    @Test
    fun `provides user-friendly messages`() {
        val error = AppError.NetworkError.NoConnection()
        val message = error.toUserMessage()
        
        assertTrue(message.contains("internet"))
        assertFalse(message.contains("Exception"))
    }
}

class RetryManagerTest {
    private lateinit var retryManager: RetryManager
    
    @Before
    fun setup() {
        retryManager = RetryManager(ErrorHandler())
    }
    
    @Test
    fun `retries operation on failure`() = runTest {
        var attempts = 0
        val config = RetryManager.RetryConfig(maxRetries = 2)
        
        val result = retryManager.withRetry(config) {
            attempts++
            if (attempts < 3) throw IOException()
            "success"
        }
        
        assertTrue(result is RetryManager.RetryResult.Success)
        assertEquals(3, attempts)
    }
    
    @Test
    fun `respects max retries`() = runTest {
        var attempts = 0
        val config = RetryManager.RetryConfig(maxRetries = 2)
        
        val result = retryManager.withRetry(config) {
            attempts++
            throw IOException()
        }
        
        assertTrue(result is RetryManager.RetryResult.MaxRetriesExceeded)
        assertEquals(3, attempts) // Initial + 2 retries
    }
    
    @Test
    fun `applies exponential backoff`() = runTest {
        val config = RetryManager.RetryConfig(
            maxRetries = 3,
            initialDelayMs = 100L,
            backoffMultiplier = 2.0
        )
        
        val startTime = System.currentTimeMillis()
        var attempts = 0
        
        retryManager.withRetry(config) {
            attempts++
            if (attempts < 4) throw IOException()
            "success"
        }
        
        val duration = System.currentTimeMillis() - startTime
        // Should take at least 100 + 200 + 400 = 700ms
        assertTrue(duration >= 700)
    }
}
```

### Performance Tests

```kotlin
class CacheManagerTest {
    private lateinit var cacheManager: CacheManager
    
    @Before
    fun setup() {
        cacheManager = CacheManager()
    }
    
    @After
    fun tearDown() = runTest {
        cacheManager.clearAll()
    }
    
    @Test
    fun `caches and retrieves data correctly`() = runTest {
        val key = "test_key"
        val value = "test_value"
        
        cacheManager.putScript(key, value)
        val retrieved = cacheManager.getScript<String>(key)
        
        assertEquals(value, retrieved)
    }
    
    @Test
    fun `expired entries are not returned`() = runTest {
        val key = "test_key"
        val value = "test_value"
        
        cacheManager.putScript(key, value, ttl = 100L)
        delay(150)
        
        val retrieved = cacheManager.getScript<String>(key)
        assertNull(retrieved)
    }
    
    @Test
    fun `cache stats are accurate`() = runTest {
        cacheManager.putScript("key1", "value1")
        cacheManager.putScript("key2", "value2")
        
        cacheManager.getScript<String>("key1") // Hit
        cacheManager.getScript<String>("key3") // Miss
        
        val stats = cacheManager.getCacheStats()
        assertTrue(stats.scriptCacheHitCount > 0)
        assertTrue(stats.scriptCacheMissCount > 0)
    }
}

class DataCompressionManagerTest {
    private lateinit var compressionManager: DataCompressionManager
    
    @Before
    fun setup() {
        compressionManager = DataCompressionManager()
    }
    
    @Test
    fun `compresses and decompresses string correctly`() {
        val original = "a".repeat(2000) // Large enough to compress
        val result = compressionManager.compressString(original)
        val decompressed = compressionManager.decompressString(result.compressed)
        
        assertEquals(original, decompressed)
        assertTrue(result.compressedSize < result.originalSize)
    }
    
    @Test
    fun `skips compression for small data`() {
        val small = "small"
        val result = compressionManager.compressString(small)
        
        assertEquals(1.0f, result.compressionRatio)
    }
    
    @Test
    fun `compression ratio is calculated correctly`() {
        val data = "a".repeat(2000)
        val result = compressionManager.compressString(data)
        
        val expectedRatio = result.compressedSize.toFloat() / result.originalSize
        assertEquals(expectedRatio, result.compressionRatio, 0.01f)
    }
}
```

### Sync System Tests

```kotlin
class ConflictResolverTest {
    private lateinit var conflictResolver: ConflictResolver
    
    @Before
    fun setup() {
        conflictResolver = ConflictResolver()
    }
    
    @Test
    fun `detects conflict when versions differ`() {
        val local = createScript(version = 1, lastModified = 1000L)
        val remote = createScript(version = 2, lastModified = 2000L)
        
        val hasConflict = conflictResolver.hasConflict(local, remote)
        assertTrue(hasConflict)
    }
    
    @Test
    fun `resolves conflict using latest timestamp`() {
        val local = createScript(version = 1, lastModified = 2000L)
        val remote = createScript(version = 2, lastModified = 1000L)
        
        val resolution = conflictResolver.resolveConflict(local, remote)
        
        assertTrue(resolution is ConflictResolution.UseLocal)
    }
    
    @Test
    fun `no conflict when versions match`() {
        val local = createScript(version = 1, lastModified = 1000L)
        val remote = createScript(version = 1, lastModified = 1000L)
        
        val hasConflict = conflictResolver.hasConflict(local, remote)
        assertFalse(hasConflict)
    }
}
```

## Running Tests

### Run All Tests
```bash
./gradlew test
```

### Run Specific Test Class
```bash
./gradlew test --tests EncryptionManagerTest
```

### Run Tests with Coverage
```bash
./gradlew testDebugUnitTestCoverage
```

### View Coverage Report
```bash
open app/build/reports/coverage/test/debug/index.html
```

## Test Configuration

### build.gradle.kts
```kotlin
android {
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    // Unit Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.0.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    
    // Android Testing
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
```

## Continuous Integration

### GitHub Actions Workflow
```yaml
name: Run Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Run tests
        run: ./gradlew test
      - name: Upload coverage
        uses: codecov/codecov-action@v3
```

## Best Practices

### 1. Test Naming
- Use descriptive names: `should_returnError_when_inputIsInvalid`
- Use backticks for readability: `` `returns error when input is invalid` ``

### 2. Test Structure (AAA Pattern)
```kotlin
@Test
fun `test name`() {
    // Arrange
    val input = "test"
    
    // Act
    val result = function(input)
    
    // Assert
    assertEquals(expected, result)
}
```

### 3. Mock External Dependencies
```kotlin
@Mock
lateinit var mockRepository: Repository

@Before
fun setup() {
    MockitoAnnotations.openMocks(this)
}
```

### 4. Test Edge Cases
- Null values
- Empty collections
- Boundary values
- Error conditions

### 5. Use Test Fixtures
```kotlin
object TestFixtures {
    fun createTestScript(
        id: Long = 1,
        title: String = "Test Script",
        content: String = "Test Content"
    ) = SavedScript(id, title, content)
}
```

## Manual Testing Checklist

### Authentication Flow
- [ ] Google Sign-In works
- [ ] Sign-Out works
- [ ] Session persists across app restarts
- [ ] Anonymous mode works

### Sync Functionality
- [ ] Auto sync triggers correctly
- [ ] Manual sync works
- [ ] Offline changes sync when online
- [ ] Conflicts are resolved correctly
- [ ] WiFi-only setting is respected

### Performance
- [ ] Cache improves load times
- [ ] Batch operations are faster
- [ ] Compression reduces data usage
- [ ] App remains responsive during sync

### Security
- [ ] Data is encrypted at rest
- [ ] Dangerous input is rejected
- [ ] Tokens are stored securely
- [ ] Firebase rules prevent unauthorized access

### Settings
- [ ] All toggles work correctly
- [ ] Sync frequency changes take effect
- [ ] Cache clear works
- [ ] Export data works

## Troubleshooting Tests

### Common Issues

**Tests fail with "Method not mocked"**
```kotlin
// Add to build.gradle.kts
testOptions {
    unitTests.isReturnDefaultValues = true
}
```

**Coroutine tests timeout**
```kotlin
// Use runTest from kotlinx-coroutines-test
@Test
fun `test name`() = runTest {
    // Test code
}
```

**Firebase tests fail**
```kotlin
// Mock Firebase dependencies
@Mock
lateinit var mockFirebaseAuth: FirebaseAuth
```

## Conclusion

This testing guide provides a foundation for ensuring code quality and reliability. Regular testing helps catch bugs early and maintains confidence in the codebase as it evolves.

For questions or improvements to this guide, please contact the development team.