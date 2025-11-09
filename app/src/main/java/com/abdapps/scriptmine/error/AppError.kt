package com.abdapps.scriptmine.error

/**
 * Sealed class representing all possible errors in the application
 * Provides type-safe error handling with specific error types
 */
sealed class AppError(
    open val message: String,
    open val cause: Throwable? = null
) {
    
    /**
     * Network-related errors
     */
    sealed class NetworkError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause) {
        
        data class NoConnection(
            override val cause: Throwable? = null
        ) : NetworkError("No internet connection available", cause)
        
        data class Timeout(
            override val cause: Throwable? = null
        ) : NetworkError("Network request timed out", cause)
        
        data class ServerError(
            val statusCode: Int,
            override val cause: Throwable? = null
        ) : NetworkError("Server error: $statusCode", cause)
        
        data class UnknownError(
            override val cause: Throwable? = null
        ) : NetworkError("Unknown network error occurred", cause)
    }
    
    /**
     * Firebase-related errors
     */
    sealed class FirebaseError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause) {
        
        data class AuthenticationFailed(
            override val cause: Throwable? = null
        ) : FirebaseError("Firebase authentication failed", cause)
        
        data class PermissionDenied(
            override val cause: Throwable? = null
        ) : FirebaseError("Permission denied to access Firebase resource", cause)
        
        data class DocumentNotFound(
            val documentId: String,
            override val cause: Throwable? = null
        ) : FirebaseError("Document not found: $documentId", cause)
        
        data class QuotaExceeded(
            override val cause: Throwable? = null
        ) : FirebaseError("Firebase quota exceeded", cause)
        
        data class RateLimitExceeded(
            override val cause: Throwable? = null
        ) : FirebaseError("Rate limit exceeded, please try again later", cause)
        
        data class FirestoreError(
            override val message: String,
            override val cause: Throwable? = null
        ) : FirebaseError(message, cause)
    }
    
    /**
     * Database-related errors
     */
    sealed class DatabaseError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause) {
        
        data class InsertFailed(
            override val cause: Throwable? = null
        ) : DatabaseError("Failed to insert data into database", cause)
        
        data class UpdateFailed(
            override val cause: Throwable? = null
        ) : DatabaseError("Failed to update data in database", cause)
        
        data class DeleteFailed(
            override val cause: Throwable? = null
        ) : DatabaseError("Failed to delete data from database", cause)
        
        data class QueryFailed(
            override val cause: Throwable? = null
        ) : DatabaseError("Database query failed", cause)
        
        data class MigrationFailed(
            val fromVersion: Int,
            val toVersion: Int,
            override val cause: Throwable? = null
        ) : DatabaseError("Database migration failed from $fromVersion to $toVersion", cause)
        
        data class CorruptedData(
            override val cause: Throwable? = null
        ) : DatabaseError("Database data is corrupted", cause)
    }
    
    /**
     * Sync-related errors
     */
    sealed class SyncError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause) {
        
        data class SyncFailed(
            override val cause: Throwable? = null
        ) : SyncError("Synchronization failed", cause)
        
        data class ConflictDetected(
            val scriptId: String,
            override val cause: Throwable? = null
        ) : SyncError("Sync conflict detected for script: $scriptId", cause)
        
        data class ConflictResolutionFailed(
            val scriptId: String,
            override val cause: Throwable? = null
        ) : SyncError("Failed to resolve conflict for script: $scriptId", cause)
        
        data class UploadFailed(
            val scriptId: String,
            override val cause: Throwable? = null
        ) : SyncError("Failed to upload script: $scriptId", cause)
        
        data class DownloadFailed(
            val scriptId: String,
            override val cause: Throwable? = null
        ) : SyncError("Failed to download script: $scriptId", cause)
        
        data class SyncTimeout(
            override val cause: Throwable? = null
        ) : SyncError("Sync operation timed out", cause)
    }
    
    /**
     * Validation errors
     */
    sealed class ValidationError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause) {
        
        data class InvalidInput(
            val field: String,
            override val message: String
        ) : ValidationError("Invalid input for $field: $message")
        
        data class RequiredFieldMissing(
            val field: String
        ) : ValidationError("Required field missing: $field")
        
        data class InvalidFormat(
            val field: String,
            val expectedFormat: String
        ) : ValidationError("Invalid format for $field, expected: $expectedFormat")
        
        data class ValueOutOfRange(
            val field: String,
            val min: Any?,
            val max: Any?
        ) : ValidationError("Value for $field is out of range [$min, $max]")
    }
    
    /**
     * Authentication errors
     */
    sealed class AuthError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause) {
        
        data class NotAuthenticated(
            override val cause: Throwable? = null
        ) : AuthError("User is not authenticated", cause)
        
        data class SessionExpired(
            override val cause: Throwable? = null
        ) : AuthError("User session has expired", cause)
        
        data class InvalidCredentials(
            override val cause: Throwable? = null
        ) : AuthError("Invalid credentials provided", cause)
        
        data class AccountDisabled(
            override val cause: Throwable? = null
        ) : AuthError("User account is disabled", cause)
        
        data class AccountDeleted(
            override val cause: Throwable? = null
        ) : AuthError("User account has been deleted", cause)
    }
    
    /**
     * Security errors
     */
    sealed class SecurityError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause) {
        
        data class EncryptionFailed(
            override val cause: Throwable? = null
        ) : SecurityError("Data encryption failed", cause)
        
        data class DecryptionFailed(
            override val cause: Throwable? = null
        ) : SecurityError("Data decryption failed", cause)
        
        data class KeyGenerationFailed(
            override val cause: Throwable? = null
        ) : SecurityError("Encryption key generation failed", cause)
        
        data class DangerousInputDetected(
            val input: String
        ) : SecurityError("Dangerous input pattern detected")
    }
    
    /**
     * Storage errors
     */
    sealed class StorageError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause) {
        
        data class InsufficientSpace(
            val requiredBytes: Long,
            override val cause: Throwable? = null
        ) : StorageError("Insufficient storage space, need $requiredBytes bytes", cause)
        
        data class FileNotFound(
            val path: String,
            override val cause: Throwable? = null
        ) : StorageError("File not found: $path", cause)
        
        data class ReadFailed(
            val path: String,
            override val cause: Throwable? = null
        ) : StorageError("Failed to read file: $path", cause)
        
        data class WriteFailed(
            val path: String,
            override val cause: Throwable? = null
        ) : StorageError("Failed to write file: $path", cause)
    }
    
    /**
     * Unknown or unexpected errors
     */
    data class UnknownError(
        override val message: String = "An unknown error occurred",
        override val cause: Throwable? = null
    ) : AppError(message, cause)
    
    /**
     * Converts error to user-friendly message
     */
    fun toUserMessage(): String {
        return when (this) {
            is NetworkError.NoConnection -> "No internet connection. Please check your network settings."
            is NetworkError.Timeout -> "Request timed out. Please try again."
            is NetworkError.ServerError -> "Server error. Please try again later."
            is NetworkError.UnknownError -> "Network error occurred. Please check your connection."
            
            is FirebaseError.AuthenticationFailed -> "Authentication failed. Please sign in again."
            is FirebaseError.PermissionDenied -> "You don't have permission to access this resource."
            is FirebaseError.DocumentNotFound -> "The requested data was not found."
            is FirebaseError.QuotaExceeded -> "Service quota exceeded. Please try again later."
            is FirebaseError.RateLimitExceeded -> "Too many requests. Please wait a moment and try again."
            is FirebaseError.FirestoreError -> "Cloud sync error: $message"
            
            is DatabaseError.InsertFailed -> "Failed to save data. Please try again."
            is DatabaseError.UpdateFailed -> "Failed to update data. Please try again."
            is DatabaseError.DeleteFailed -> "Failed to delete data. Please try again."
            is DatabaseError.QueryFailed -> "Failed to load data. Please try again."
            is DatabaseError.MigrationFailed -> "Database upgrade failed. Please reinstall the app."
            is DatabaseError.CorruptedData -> "Data corruption detected. Please contact support."
            
            is SyncError.SyncFailed -> "Sync failed. Your changes will be synced when connection is restored."
            is SyncError.ConflictDetected -> "Sync conflict detected. Please review your changes."
            is SyncError.ConflictResolutionFailed -> "Failed to resolve sync conflict."
            is SyncError.UploadFailed -> "Failed to upload changes. Will retry automatically."
            is SyncError.DownloadFailed -> "Failed to download updates. Will retry automatically."
            is SyncError.SyncTimeout -> "Sync is taking longer than expected. Please wait."
            
            is ValidationError.InvalidInput -> message
            is ValidationError.RequiredFieldMissing -> "Required field: $field"
            is ValidationError.InvalidFormat -> "Invalid format for $field"
            is ValidationError.ValueOutOfRange -> "Value for $field is out of range"
            
            is AuthError.NotAuthenticated -> "Please sign in to continue."
            is AuthError.SessionExpired -> "Your session has expired. Please sign in again."
            is AuthError.InvalidCredentials -> "Invalid credentials. Please try again."
            is AuthError.AccountDisabled -> "Your account has been disabled. Please contact support."
            is AuthError.AccountDeleted -> "Your account has been deleted."
            
            is SecurityError.EncryptionFailed -> "Failed to secure your data. Please try again."
            is SecurityError.DecryptionFailed -> "Failed to access encrypted data."
            is SecurityError.KeyGenerationFailed -> "Security initialization failed."
            is SecurityError.DangerousInputDetected -> "Invalid input detected. Please check your data."
            
            is StorageError.InsufficientSpace -> "Not enough storage space available."
            is StorageError.FileNotFound -> "File not found."
            is StorageError.ReadFailed -> "Failed to read file."
            is StorageError.WriteFailed -> "Failed to save file."
            
            is UnknownError -> message
        }
    }
    
    /**
     * Checks if error is recoverable
     */
    fun isRecoverable(): Boolean {
        return when (this) {
            is NetworkError.NoConnection,
            is NetworkError.Timeout,
            is FirebaseError.RateLimitExceeded,
            is SyncError.SyncFailed,
            is SyncError.UploadFailed,
            is SyncError.DownloadFailed,
            is SyncError.SyncTimeout -> true
            
            is DatabaseError.CorruptedData,
            is DatabaseError.MigrationFailed,
            is AuthError.AccountDeleted,
            is SecurityError.KeyGenerationFailed -> false
            
            else -> true
        }
    }
    
    /**
     * Checks if error requires user action
     */
    fun requiresUserAction(): Boolean {
        return when (this) {
            is NetworkError.NoConnection,
            is AuthError.NotAuthenticated,
            is AuthError.SessionExpired,
            is AuthError.InvalidCredentials,
            is ValidationError,
            is SyncError.ConflictDetected,
            is StorageError.InsufficientSpace -> true
            
            else -> false
        }
    }
}