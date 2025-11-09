package com.abdapps.scriptmine.performance

import android.util.Log
import com.abdapps.scriptmine.data.model.SavedScript
import com.abdapps.scriptmine.error.AppError
import com.abdapps.scriptmine.error.ErrorResult
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages batch operations for improved performance
 * Provides efficient bulk processing of scripts and data
 */
@Singleton
class BatchOperationManager @Inject constructor() {
    
    companion object {
        private const val TAG = "BatchOperationManager"
        private const val DEFAULT_BATCH_SIZE = 50
        private const val DEFAULT_PARALLEL_OPERATIONS = 3
    }
    
    /**
     * Batch operation result
     */
    data class BatchResult<T>(
        val successful: List<T>,
        val failed: List<BatchFailure<*>>,
        val totalProcessed: Int,
        val successRate: Float
    ) {
        val isFullSuccess: Boolean get() = failed.isEmpty()
        val hasPartialSuccess: Boolean get() = successful.isNotEmpty() && failed.isNotEmpty()
        val isFullFailure: Boolean get() = successful.isEmpty()
    }
    
    /**
     * Represents a failed item in batch operation
     */
    data class BatchFailure<T>(
        val item: T,
        val error: AppError,
        val attemptNumber: Int = 0
    )
    
    /**
     * Batch operation progress
     */
    data class BatchProgress(
        val processed: Int,
        val total: Int,
        val currentItem: String? = null
    ) {
        val percentage: Float get() = if (total > 0) (processed.toFloat() / total) * 100 else 0f
    }
    
    /**
     * Processes items in batches with parallel execution
     */
    suspend fun <T, R> processBatch(
        items: List<T>,
        batchSize: Int = DEFAULT_BATCH_SIZE,
        parallelOperations: Int = DEFAULT_PARALLEL_OPERATIONS,
        operation: suspend (T) -> R
    ): BatchResult<R> {
        if (items.isEmpty()) {
            return BatchResult(emptyList(), emptyList(), 0, 1.0f)
        }
        
        Log.d(TAG, "Processing ${items.size} items in batches of $batchSize with $parallelOperations parallel operations")
        
        val successful = mutableListOf<R>()
        val failed = mutableListOf<BatchFailure<T>>()
        
        // Process in batches
        items.chunked(batchSize).forEach { batch ->
            // Process batch items in parallel
            val results = coroutineScope {
                batch.chunked(
                    (batch.size + parallelOperations - 1) / parallelOperations
                ).map { chunk ->
                    async(Dispatchers.IO) {
                        chunk.map { item ->
                            try {
                                val result = operation(item)
                                Result.success(result)
                            } catch (e: Exception) {
                                Result.failure<R>(e)
                            }
                        }
                    }
                }.awaitAll().flatten()
            }
            
            // Collect results
            results.forEachIndexed { index, result ->
                result.fold(
                    onSuccess = { successful.add(it) },
                    onFailure = { 
                        failed.add(
                            BatchFailure(
                                batch[index],
                                AppError.UnknownError(it.message ?: "Batch operation failed", it)
                            )
                        )
                    }
                )
            }
        }
        
        val successRate = successful.size.toFloat() / items.size
        Log.d(TAG, "Batch processing complete: ${successful.size}/${items.size} successful (${successRate * 100}%)")
        
        return BatchResult(
            successful = successful.toList(),
            failed = failed.toList(),
            totalProcessed = items.size,
            successRate = successRate
        )
    }
    
    /**
     * Processes items in batches with progress updates
     */
    fun <T, R> processBatchWithProgress(
        items: List<T>,
        batchSize: Int = DEFAULT_BATCH_SIZE,
        operation: suspend (T) -> R
    ): Flow<BatchOperationState<R>> = flow {
        if (items.isEmpty()) {
            emit(BatchOperationState.Completed(BatchResult(emptyList(), emptyList(), 0, 1.0f)))
            return@flow
        }
        
        emit(BatchOperationState.Started(items.size))
        
        val successful = mutableListOf<R>()
        val failed = mutableListOf<BatchFailure<T>>()
        var processed = 0
        
        items.chunked(batchSize).forEach { batch ->
            batch.forEach { item ->
                try {
                    emit(BatchOperationState.Processing(
                        BatchProgress(processed, items.size, item.toString())
                    ))
                    
                    val result = operation(item)
                    successful.add(result)
                    
                } catch (e: Exception) {
                    failed.add(
                        BatchFailure(
                            item,
                            AppError.UnknownError(e.message ?: "Operation failed", e)
                        )
                    )
                }
                
                processed++
            }
            
            // Small delay between batches to prevent overwhelming the system
            delay(100)
        }
        
        val result = BatchResult(
            successful = successful.toList(),
            failed = failed.toList(),
            totalProcessed = items.size,
            successRate = successful.size.toFloat() / items.size
        )
        
        emit(BatchOperationState.Completed(result))
    }
    
    /**
     * Uploads multiple scripts in batches
     */
    suspend fun batchUploadScripts(
        scripts: List<SavedScript>,
        uploadOperation: suspend (SavedScript) -> Unit
    ): BatchResult<SavedScript> {
        Log.d(TAG, "Starting batch upload of ${scripts.size} scripts")
        
        val successful = mutableListOf<SavedScript>()
        val failed = mutableListOf<BatchFailure<SavedScript>>()
        
        scripts.chunked(DEFAULT_BATCH_SIZE).forEach { batch ->
            // Upload batch in parallel
            val results = coroutineScope {
                batch.map { script ->
                    async(Dispatchers.IO) {
                        try {
                            uploadOperation(script)
                            Result.success(script)
                        } catch (e: Exception) {
                            Result.failure<SavedScript>(e)
                        }
                    }
                }.awaitAll()
            }
            
            // Collect results
            results.forEachIndexed { index, result ->
                result.fold(
                    onSuccess = { successful.add(it) },
                    onFailure = {
                        failed.add(
                            BatchFailure(
                                batch[index],
                                AppError.SyncError.UploadFailed(batch[index].id.toString(), it)
                            )
                        )
                    }
                )
            }
            
            // Small delay between batches
            delay(500)
        }
        
        return BatchResult(
            successful = successful.toList(),
            failed = failed.toList(),
            totalProcessed = scripts.size,
            successRate = successful.size.toFloat() / scripts.size
        )
    }
    
    /**
     * Downloads multiple scripts in batches
     */
    suspend fun batchDownloadScripts(
        scriptIds: List<String>,
        downloadOperation: suspend (String) -> SavedScript
    ): BatchResult<SavedScript> {
        Log.d(TAG, "Starting batch download of ${scriptIds.size} scripts")
        
        return processBatch(
            items = scriptIds,
            batchSize = DEFAULT_BATCH_SIZE,
            parallelOperations = DEFAULT_PARALLEL_OPERATIONS,
            operation = downloadOperation
        )
    }
    
    /**
     * Deletes multiple scripts in batches
     */
    suspend fun batchDeleteScripts(
        scriptIds: List<String>,
        deleteOperation: suspend (String) -> Unit
    ): BatchResult<String> {
        Log.d(TAG, "Starting batch delete of ${scriptIds.size} scripts")
        
        val successful = mutableListOf<String>()
        val failed = mutableListOf<BatchFailure<String>>()
        
        scriptIds.chunked(DEFAULT_BATCH_SIZE).forEach { batch ->
            batch.forEach { scriptId ->
                try {
                    deleteOperation(scriptId)
                    successful.add(scriptId)
                } catch (e: Exception) {
                    failed.add(
                        BatchFailure(
                            scriptId,
                            AppError.SyncError.SyncFailed(e)
                        )
                    )
                }
            }
            
            delay(200)
        }
        
        return BatchResult(
            successful = successful.toList(),
            failed = failed.toList(),
            totalProcessed = scriptIds.size,
            successRate = successful.size.toFloat() / scriptIds.size
        )
    }
    
    /**
     * Retries failed items from a previous batch operation
     */
    suspend fun <T, R> retryFailedItems(
        failures: List<BatchFailure<T>>,
        operation: suspend (T) -> R,
        maxRetries: Int = 3
    ): BatchResult<R> {
        if (failures.isEmpty()) {
            return BatchResult(emptyList(), emptyList(), 0, 1.0f)
        }
        
        Log.d(TAG, "Retrying ${failures.size} failed items (max $maxRetries retries)")
        
        val successful = mutableListOf<R>()
        val stillFailed = mutableListOf<BatchFailure<T>>()
        
        failures.forEach { failure ->
            var currentAttempt = failure.attemptNumber
            var succeeded = false
            
            while (currentAttempt < maxRetries && !succeeded) {
                try {
                    val result = operation(failure.item)
                    successful.add(result)
                    succeeded = true
                    Log.d(TAG, "Retry successful for item after ${currentAttempt + 1} attempts")
                } catch (e: Exception) {
                    currentAttempt++
                    if (currentAttempt >= maxRetries) {
                        stillFailed.add(
                            failure.copy(
                                error = AppError.UnknownError(e.message ?: "Retry failed", e),
                                attemptNumber = currentAttempt
                            )
                        )
                    }
                    delay(1000L * currentAttempt) // Exponential backoff
                }
            }
        }
        
        return BatchResult(
            successful = successful.toList(),
            failed = stillFailed.toList(),
            totalProcessed = failures.size,
            successRate = successful.size.toFloat() / failures.size
        )
    }
}

/**
 * Represents the state of a batch operation
 */
sealed class BatchOperationState<out T> {
    data class Started(val totalItems: Int) : BatchOperationState<Nothing>()
    data class Processing<T>(val progress: BatchOperationManager.BatchProgress) : BatchOperationState<T>()
    data class Completed<T>(val result: BatchOperationManager.BatchResult<T>) : BatchOperationState<T>()
    data class Error(val error: AppError) : BatchOperationState<Nothing>()
}