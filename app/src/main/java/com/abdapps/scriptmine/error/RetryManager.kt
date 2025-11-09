package com.abdapps.scriptmine.error

import android.util.Log
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min
import kotlin.math.pow

/**
 * Manages retry logic with exponential backoff
 * Provides configurable retry strategies for different operations
 */
@Singleton
class RetryManager @Inject constructor(
    private val errorHandler: ErrorHandler
) {
    
    companion object {
        private const val TAG = "RetryManager"
        private const val DEFAULT_MAX_RETRIES = 3
        private const val DEFAULT_INITIAL_DELAY_MS = 1000L
        private const val DEFAULT_MAX_DELAY_MS = 30000L
        private const val DEFAULT_BACKOFF_MULTIPLIER = 2.0
    }
    
    /**
     * Retry configuration
     */
    data class RetryConfig(
        val maxRetries: Int = DEFAULT_MAX_RETRIES,
        val initialDelayMs: Long = DEFAULT_INITIAL_DELAY_MS,
        val maxDelayMs: Long = DEFAULT_MAX_DELAY_MS,
        val backoffMultiplier: Double = DEFAULT_BACKOFF_MULTIPLIER,
        val retryableErrors: Set<Class<out AppError>> = setOf(
            AppError.NetworkError::class.java,
            AppError.SyncError::class.java,
            AppError.FirebaseError.RateLimitExceeded::class.java
        )
    )
    
    /**
     * Retry result
     */
    sealed class RetryResult<out T> {
        data class Success<T>(val data: T, val attemptNumber: Int) : RetryResult<T>()
        data class Failure(val error: AppError, val attemptNumber: Int) : RetryResult<Nothing>()
        data class MaxRetriesExceeded(val lastError: AppError) : RetryResult<Nothing>()
    }
    
    /**
     * Executes a block with retry logic
     */
    suspend fun <T> withRetry(
        config: RetryConfig = RetryConfig(),
        context: String = "",
        block: suspend (attemptNumber: Int) -> T
    ): RetryResult<T> {
        var currentAttempt = 0
        var lastError: AppError? = null
        
        while (currentAttempt <= config.maxRetries) {
            try {
                Log.d(TAG, "[$context] Attempt ${currentAttempt + 1}/${config.maxRetries + 1}")
                
                val result = block(currentAttempt)
                
                if (currentAttempt > 0) {
                    Log.i(TAG, "[$context] Succeeded after $currentAttempt retries")
                }
                
                return RetryResult.Success(result, currentAttempt)
                
            } catch (e: Exception) {
                val error = errorHandler.handleException(e, context)
                lastError = error
                
                Log.w(TAG, "[$context] Attempt ${currentAttempt + 1} failed: ${error.message}")
                
                // Check if error is retryable
                if (!isRetryable(error, config)) {
                    Log.e(TAG, "[$context] Error is not retryable, aborting")
                    return RetryResult.Failure(error, currentAttempt)
                }
                
                // Check if we have more retries
                if (currentAttempt >= config.maxRetries) {
                    Log.e(TAG, "[$context] Max retries exceeded")
                    return RetryResult.MaxRetriesExceeded(error)
                }
                
                // Calculate delay with exponential backoff
                val delayMs = calculateDelay(currentAttempt, config)
                Log.d(TAG, "[$context] Waiting ${delayMs}ms before retry")
                delay(delayMs)
                
                currentAttempt++
            }
        }
        
        return RetryResult.MaxRetriesExceeded(
            lastError ?: AppError.UnknownError("Max retries exceeded")
        )
    }
    
    /**
     * Executes a block with retry logic and returns ErrorResult
     */
    suspend fun <T> withRetryErrorResult(
        config: RetryConfig = RetryConfig(),
        context: String = "",
        block: suspend (attemptNumber: Int) -> T
    ): ErrorResult<T> {
        return when (val result = withRetry(config, context, block)) {
            is RetryResult.Success -> ErrorResult.Success(result.data)
            is RetryResult.Failure -> ErrorResult.Error(result.error)
            is RetryResult.MaxRetriesExceeded -> ErrorResult.Error(result.lastError)
        }
    }
    
    /**
     * Checks if an error is retryable based on configuration
     */
    private fun isRetryable(error: AppError, config: RetryConfig): Boolean {
        // Check if error is in retryable list
        val isInRetryableList = config.retryableErrors.any { retryableClass ->
            retryableClass.isInstance(error)
        }
        
        // Also check if error itself says it's recoverable
        return isInRetryableList || error.isRecoverable()
    }
    
    /**
     * Calculates delay with exponential backoff
     */
    private fun calculateDelay(attemptNumber: Int, config: RetryConfig): Long {
        val exponentialDelay = config.initialDelayMs * 
            config.backoffMultiplier.pow(attemptNumber.toDouble())
        
        // Add jitter to prevent thundering herd
        val jitter = (Math.random() * 0.1 * exponentialDelay).toLong()
        
        val totalDelay = exponentialDelay.toLong() + jitter
        
        // Cap at max delay
        return min(totalDelay, config.maxDelayMs)
    }
    
    /**
     * Creates a retry config for network operations
     */
    fun networkRetryConfig(): RetryConfig {
        return RetryConfig(
            maxRetries = 3,
            initialDelayMs = 1000L,
            maxDelayMs = 10000L,
            backoffMultiplier = 2.0,
            retryableErrors = setOf(
                AppError.NetworkError::class.java
            )
        )
    }
    
    /**
     * Creates a retry config for sync operations
     */
    fun syncRetryConfig(): RetryConfig {
        return RetryConfig(
            maxRetries = 5,
            initialDelayMs = 2000L,
            maxDelayMs = 30000L,
            backoffMultiplier = 2.0,
            retryableErrors = setOf(
                AppError.NetworkError::class.java,
                AppError.SyncError::class.java,
                AppError.FirebaseError.RateLimitExceeded::class.java
            )
        )
    }
    
    /**
     * Creates a retry config for Firebase operations
     */
    fun firebaseRetryConfig(): RetryConfig {
        return RetryConfig(
            maxRetries = 3,
            initialDelayMs = 1500L,
            maxDelayMs = 15000L,
            backoffMultiplier = 2.5,
            retryableErrors = setOf(
                AppError.NetworkError::class.java,
                AppError.FirebaseError.RateLimitExceeded::class.java,
                AppError.FirebaseError.FirestoreError::class.java
            )
        )
    }
    
    /**
     * Creates a retry config for database operations
     */
    fun databaseRetryConfig(): RetryConfig {
        return RetryConfig(
            maxRetries = 2,
            initialDelayMs = 500L,
            maxDelayMs = 2000L,
            backoffMultiplier = 2.0,
            retryableErrors = setOf(
                AppError.DatabaseError.QueryFailed::class.java,
                AppError.DatabaseError.InsertFailed::class.java,
                AppError.DatabaseError.UpdateFailed::class.java
            )
        )
    }
    
    /**
     * Creates a retry config with no retries (fail fast)
     */
    fun noRetryConfig(): RetryConfig {
        return RetryConfig(
            maxRetries = 0,
            initialDelayMs = 0L,
            maxDelayMs = 0L,
            backoffMultiplier = 1.0,
            retryableErrors = emptySet()
        )
    }
}

/**
 * Circuit breaker for preventing repeated failures
 */
@Singleton
class CircuitBreaker @Inject constructor() {
    
    companion object {
        private const val TAG = "CircuitBreaker"
        private const val DEFAULT_FAILURE_THRESHOLD = 5
        private const val DEFAULT_TIMEOUT_MS = 60000L // 1 minute
    }
    
    private data class CircuitState(
        var failureCount: Int = 0,
        var lastFailureTime: Long = 0L,
        var isOpen: Boolean = false
    )
    
    private val circuits = mutableMapOf<String, CircuitState>()
    
    /**
     * Executes a block with circuit breaker protection
     */
    suspend fun <T> withCircuitBreaker(
        circuitName: String,
        failureThreshold: Int = DEFAULT_FAILURE_THRESHOLD,
        timeoutMs: Long = DEFAULT_TIMEOUT_MS,
        block: suspend () -> T
    ): Result<T> {
        val state = circuits.getOrPut(circuitName) { CircuitState() }
        
        // Check if circuit is open
        if (state.isOpen) {
            val timeSinceLastFailure = System.currentTimeMillis() - state.lastFailureTime
            
            if (timeSinceLastFailure < timeoutMs) {
                Log.w(TAG, "Circuit breaker [$circuitName] is OPEN, rejecting request")
                return Result.failure(
                    Exception("Circuit breaker is open for $circuitName")
                )
            } else {
                // Try to close circuit (half-open state)
                Log.i(TAG, "Circuit breaker [$circuitName] entering HALF-OPEN state")
                state.isOpen = false
                state.failureCount = 0
            }
        }
        
        return try {
            val result = block()
            
            // Success - reset failure count
            if (state.failureCount > 0) {
                Log.i(TAG, "Circuit breaker [$circuitName] reset after success")
                state.failureCount = 0
            }
            
            Result.success(result)
            
        } catch (e: Exception) {
            state.failureCount++
            state.lastFailureTime = System.currentTimeMillis()
            
            Log.w(TAG, "Circuit breaker [$circuitName] failure count: ${state.failureCount}")
            
            // Open circuit if threshold exceeded
            if (state.failureCount >= failureThreshold) {
                state.isOpen = true
                Log.e(TAG, "Circuit breaker [$circuitName] is now OPEN")
            }
            
            Result.failure(e)
        }
    }
    
    /**
     * Resets a specific circuit
     */
    fun resetCircuit(circuitName: String) {
        circuits[circuitName] = CircuitState()
        Log.i(TAG, "Circuit breaker [$circuitName] manually reset")
    }
    
    /**
     * Resets all circuits
     */
    fun resetAllCircuits() {
        circuits.clear()
        Log.i(TAG, "All circuit breakers reset")
    }
    
    /**
     * Gets the state of a circuit
     */
    fun getCircuitState(circuitName: String): String {
        val state = circuits[circuitName] ?: return "CLOSED"
        return when {
            state.isOpen -> "OPEN"
            state.failureCount > 0 -> "HALF-OPEN"
            else -> "CLOSED"
        }
    }
}