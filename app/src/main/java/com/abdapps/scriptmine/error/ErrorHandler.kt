package com.abdapps.scriptmine.error

import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized error handler for the application
 * Converts exceptions to AppError types and provides logging
 */
@Singleton
class ErrorHandler @Inject constructor() {
    
    companion object {
        private const val TAG = "ErrorHandler"
    }
    
    /**
     * Handles an exception and converts it to AppError
     */
    fun handleException(exception: Throwable, context: String = ""): AppError {
        Log.e(TAG, "Error in $context", exception)
        
        return when (exception) {
            // Network errors
            is UnknownHostException -> AppError.NetworkError.NoConnection(exception)
            is SocketTimeoutException -> AppError.NetworkError.Timeout(exception)
            is IOException -> AppError.NetworkError.UnknownError(exception)
            
            // Firebase errors
            is FirebaseNetworkException -> AppError.NetworkError.NoConnection(exception)
            is FirebaseAuthException -> handleFirebaseAuthException(exception)
            is FirebaseFirestoreException -> handleFirestoreException(exception)
            is FirebaseException -> AppError.FirebaseError.FirestoreError(
                exception.message ?: "Firebase error",
                exception
            )
            
            // Security errors
            is SecurityException -> AppError.SecurityError.EncryptionFailed(exception)
            
            // Database errors
            is android.database.SQLException -> AppError.DatabaseError.QueryFailed(exception)
            
            // Unknown errors
            else -> AppError.UnknownError(
                exception.message ?: "Unknown error occurred",
                exception
            )
        }
    }
    
    /**
     * Handles Firebase Auth exceptions
     */
    private fun handleFirebaseAuthException(exception: FirebaseAuthException): AppError {
        return when (exception.errorCode) {
            "ERROR_INVALID_CREDENTIAL",
            "ERROR_WRONG_PASSWORD",
            "ERROR_INVALID_EMAIL" -> AppError.AuthError.InvalidCredentials(exception)
            
            "ERROR_USER_DISABLED" -> AppError.AuthError.AccountDisabled(exception)
            
            "ERROR_USER_NOT_FOUND" -> AppError.AuthError.NotAuthenticated(exception)
            
            "ERROR_NETWORK_REQUEST_FAILED" -> AppError.NetworkError.NoConnection(exception)
            
            else -> AppError.FirebaseError.AuthenticationFailed(exception)
        }
    }
    
    /**
     * Handles Firestore exceptions
     */
    private fun handleFirestoreException(exception: FirebaseFirestoreException): AppError {
        return when (exception.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> 
                AppError.FirebaseError.PermissionDenied(exception)
            
            FirebaseFirestoreException.Code.NOT_FOUND -> 
                AppError.FirebaseError.DocumentNotFound("unknown", exception)
            
            FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED -> 
                AppError.FirebaseError.QuotaExceeded(exception)
            
            FirebaseFirestoreException.Code.UNAVAILABLE -> 
                AppError.NetworkError.NoConnection(exception)
            
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> 
                AppError.NetworkError.Timeout(exception)
            
            FirebaseFirestoreException.Code.UNAUTHENTICATED -> 
                AppError.AuthError.NotAuthenticated(exception)
            
            else -> AppError.FirebaseError.FirestoreError(
                exception.message ?: "Firestore error",
                exception
            )
        }
    }
    
    /**
     * Logs error with appropriate level
     */
    fun logError(error: AppError, context: String = "") {
        val logMessage = if (context.isNotEmpty()) {
            "[$context] ${error.message}"
        } else {
            error.message
        }
        
        when {
            error.isRecoverable() -> Log.w(TAG, logMessage, error.cause)
            else -> Log.e(TAG, logMessage, error.cause)
        }
    }
    
    /**
     * Handles error and returns user-friendly message
     */
    fun handleAndGetMessage(exception: Throwable, context: String = ""): String {
        val error = handleException(exception, context)
        logError(error, context)
        return error.toUserMessage()
    }
    
    /**
     * Wraps a suspending function with error handling
     */
    suspend fun <T> withErrorHandling(
        context: String = "",
        block: suspend () -> T
    ): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            val error = handleException(e, context)
            logError(error, context)
            Result.failure(e)
        }
    }
    
    /**
     * Wraps a function with error handling and returns AppError
     */
    suspend fun <T> withAppErrorHandling(
        context: String = "",
        block: suspend () -> T
    ): ErrorResult<T> {
        return try {
            ErrorResult.Success(block())
        } catch (e: Exception) {
            val error = handleException(e, context)
            logError(error, context)
            ErrorResult.Error(error)
        }
    }
}

/**
 * Result type that includes AppError
 */
sealed class ErrorResult<out T> {
    data class Success<T>(val data: T) : ErrorResult<T>()
    data class Error(val error: AppError) : ErrorResult<Nothing>()
    
    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
    
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
    
    fun errorOrNull(): AppError? = when (this) {
        is Success -> null
        is Error -> error
    }
    
    inline fun onSuccess(action: (T) -> Unit): ErrorResult<T> {
        if (this is Success) action(data)
        return this
    }
    
    inline fun onError(action: (AppError) -> Unit): ErrorResult<T> {
        if (this is Error) action(error)
        return this
    }
    
    inline fun <R> map(transform: (T) -> R): ErrorResult<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> Error(error)
        }
    }
    
    inline fun <R> flatMap(transform: (T) -> ErrorResult<R>): ErrorResult<R> {
        return when (this) {
            is Success -> transform(data)
            is Error -> Error(error)
        }
    }
}

/**
 * Extension function to convert Result to ErrorResult
 */
fun <T> Result<T>.toErrorResult(errorHandler: ErrorHandler, context: String = ""): ErrorResult<T> {
    return fold(
        onSuccess = { ErrorResult.Success(it) },
        onFailure = { ErrorResult.Error(errorHandler.handleException(it, context)) }
    )
}