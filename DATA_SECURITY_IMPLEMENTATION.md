# Data Security and Encryption Implementation

## Overview
This document describes the implementation of data security and encryption features in ScriptMine.

## Components Implemented

### 1. EncryptionManager
**Location**: `app/src/main/java/com/abdapps/scriptmine/security/EncryptionManager.kt`

**Features**:
- AES-GCM encryption using Android Keystore
- 256-bit encryption keys
- Secure key generation and storage
- IV (Initialization Vector) management
- SHA-256 hashing
- Secure random token generation

**Usage**:
```kotlin
@Inject lateinit var encryptionManager: EncryptionManager

// Encrypt data
val encrypted = encryptionManager.encrypt("sensitive data")

// Decrypt data
val decrypted = encryptionManager.decrypt(encrypted)

// Hash data
val hashed = encryptionManager.hash("data to hash")

// Generate secure token
val token = encryptionManager.generateSecureToken()
```

**Key Features**:
- Uses Android Keystore for hardware-backed security
- Automatic key generation on first use
- GCM mode for authenticated encryption
- Base64 encoding for storage compatibility

### 2. SecureDataStore
**Location**: `app/src/main/java/com/abdapps/scriptmine/security/SecureDataStore.kt`

**Features**:
- Encrypted SharedPreferences using EncryptedSharedPreferences
- Secure storage for authentication tokens
- User credentials management
- API key storage
- Custom secure value storage

**Usage**:
```kotlin
@Inject lateinit var secureDataStore: SecureDataStore

// Store auth token
secureDataStore.saveAuthToken("token_value")

// Retrieve auth token
val token = secureDataStore.getAuthToken()

// Store custom secure value
secureDataStore.saveSecureValue("custom_key", "secure_value")

// Clear all tokens
secureDataStore.clearAuthTokens()
```

**Stored Data**:
- Authentication tokens
- Refresh tokens
- User IDs
- API keys
- Backup timestamps
- Encryption salts
- Custom secure values

### 3. DataValidator
**Location**: `app/src/main/java/com/abdapps/scriptmine/security/DataValidator.kt`

**Features**:
- Input validation and sanitization
- Injection attack prevention
- Format validation (email, URL, phone)
- Length validation
- Dangerous pattern detection
- Special character escaping

**Usage**:
```kotlin
@Inject lateinit var dataValidator: DataValidator

// Validate script title
val result = dataValidator.validateScriptTitle(title)
when (result) {
    is ValidationResult.Valid -> {
        val sanitized = result.sanitizedValue
        // Use sanitized value
    }
    is ValidationResult.Invalid -> {
        val error = result.error
        // Show error to user
    }
}

// Validate email
val emailResult = dataValidator.validateEmail(email)

// Check for dangerous patterns
val isDangerous = dataValidator.containsDangerousPatterns(input)
```

**Validation Types**:
- Script titles and content
- Template names
- User names and emails
- User IDs and Firebase IDs
- URLs (HTTPS only)
- Phone numbers
- JSON format
- Timestamps and versions

**Security Features**:
- HTML tag removal
- SQL injection prevention
- XSS attack prevention
- Path traversal prevention
- Maximum length enforcement

## Security Best Practices

### 1. Data Encryption
- All sensitive data should be encrypted before storage
- Use `EncryptionManager` for field-level encryption
- Use `SecureDataStore` for secure preferences

### 2. Input Validation
- Always validate user input before processing
- Sanitize data before storing in database
- Validate data before sending to Firebase

### 3. Authentication Tokens
- Store tokens in `SecureDataStore`
- Never log authentication tokens
- Clear tokens on logout
- Implement token refresh mechanism

### 4. Firebase Security
- Use Firebase Security Rules (see `firestore.rules`)
- Validate user permissions server-side
- Implement rate limiting
- Monitor for suspicious activity

## Integration Examples

### Encrypting Script Content
```kotlin
class ScriptRepository @Inject constructor(
    private val encryptionManager: EncryptionManager,
    private val dataValidator: DataValidator
) {
    suspend fun saveScript(script: SavedScript): Result<Unit> {
        // Validate content
        val validation = dataValidator.validateScriptContent(script.content)
        if (validation.isInvalid()) {
            return Result.failure(Exception(validation.getErrorOrNull()))
        }
        
        // Encrypt sensitive fields if needed
        val encryptedScript = script.copy(
            content = validation.getValueOrNull() ?: script.content
        )
        
        // Save to database
        dao.insertScript(encryptedScript)
        return Result.success(Unit)
    }
}
```

### Validating User Input
```kotlin
class CreateScriptViewModel @Inject constructor(
    private val dataValidator: DataValidator
) : ViewModel() {
    fun validateAndSave(title: String, content: String) {
        val titleValidation = dataValidator.validateScriptTitle(title)
        val contentValidation = dataValidator.validateScriptContent(content)
        
        if (titleValidation.isValid() && contentValidation.isValid()) {
            // Save script with sanitized values
            saveScript(
                title = titleValidation.getValueOrNull()!!,
                content = contentValidation.getValueOrNull()!!
            )
        } else {
            // Show validation errors
            showError(
                titleValidation.getErrorOrNull() 
                    ?: contentValidation.getErrorOrNull()
            )
        }
    }
}
```

### Secure Token Management
```kotlin
class AuthRepository @Inject constructor(
    private val secureDataStore: SecureDataStore
) {
    suspend fun login(credentials: Credentials): Result<User> {
        val result = authService.login(credentials)
        
        if (result.isSuccess) {
            // Store tokens securely
            secureDataStore.saveAuthToken(result.authToken)
            secureDataStore.saveRefreshToken(result.refreshToken)
            secureDataStore.saveUserId(result.userId)
        }
        
        return result
    }
    
    fun logout() {
        // Clear all secure data
        secureDataStore.clearAuthTokens()
    }
}
```

## Testing Security

### Unit Tests
```kotlin
class EncryptionManagerTest {
    @Test
    fun `encrypt and decrypt returns original value`() {
        val manager = EncryptionManager()
        val original = "sensitive data"
        
        val encrypted = manager.encrypt(original)
        val decrypted = manager.decrypt(encrypted)
        
        assertEquals(original, decrypted)
        assertNotEquals(original, encrypted)
    }
}

class DataValidatorTest {
    @Test
    fun `validates email format correctly`() {
        val validator = DataValidator()
        
        val valid = validator.validateEmail("user@example.com")
        assertTrue(valid.isValid())
        
        val invalid = validator.validateEmail("invalid-email")
        assertTrue(invalid.isInvalid())
    }
}
```

## Security Checklist

- [x] Implement AES-GCM encryption
- [x] Use Android Keystore for key management
- [x] Implement secure data storage
- [x] Add input validation
- [x] Implement sanitization
- [x] Prevent injection attacks
- [x] Validate email and URL formats
- [x] Implement token management
- [x] Add dangerous pattern detection
- [ ] Implement certificate pinning (future)
- [ ] Add biometric authentication (future)
- [ ] Implement data backup encryption (future)

## Performance Considerations

1. **Encryption Overhead**: Minimal impact on performance
2. **Validation**: Fast, regex-based validation
3. **Secure Storage**: Slightly slower than regular SharedPreferences
4. **Key Generation**: One-time operation on first use

## Maintenance

### Key Rotation
```kotlin
// WARNING: This will make all encrypted data unrecoverable
encryptionManager.regenerateKey()
```

### Clearing Secure Data
```kotlin
// Clear all secure preferences
secureDataStore.clearAll()

// Clear only auth tokens
secureDataStore.clearAuthTokens()
```

## Future Enhancements

1. **Biometric Authentication**: Add fingerprint/face unlock
2. **Certificate Pinning**: Prevent MITM attacks
3. **Data Backup Encryption**: Encrypt backup files
4. **Secure Logging**: Implement secure logging mechanism
5. **Penetration Testing**: Regular security audits
6. **OWASP Compliance**: Follow OWASP Mobile Top 10

## References

- [Android Keystore System](https://developer.android.com/training/articles/keystore)
- [EncryptedSharedPreferences](https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences)
- [OWASP Mobile Security](https://owasp.org/www-project-mobile-security/)
- [Android Security Best Practices](https://developer.android.com/topic/security/best-practices)