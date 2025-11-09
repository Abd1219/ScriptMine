package com.abdapps.scriptmine.performance

import android.util.Log
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages data compression for optimized data transfer
 * Provides GZIP compression/decompression for strings and data
 */
@Singleton
class DataCompressionManager @Inject constructor() {
    
    companion object {
        private const val TAG = "DataCompressionManager"
        private const val MIN_COMPRESSION_SIZE = 1024 // 1KB - don't compress smaller data
        private const val COMPRESSION_THRESHOLD_RATIO = 0.9f // Only use if compressed is <90% of original
    }
    
    /**
     * Compression result
     */
    data class CompressionResult(
        val compressed: ByteArray,
        val originalSize: Int,
        val compressedSize: Int,
        val compressionRatio: Float,
        val timeTakenMs: Long
    ) {
        val savedBytes: Int get() = originalSize - compressedSize
        val savedPercentage: Float get() = (1 - compressionRatio) * 100
        
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            
            other as CompressionResult
            
            if (!compressed.contentEquals(other.compressed)) return false
            if (originalSize != other.originalSize) return false
            if (compressedSize != other.compressedSize) return false
            
            return true
        }
        
        override fun hashCode(): Int {
            var result = compressed.contentHashCode()
            result = 31 * result + originalSize
            result = 31 * result + compressedSize
            return result
        }
    }
    
    /**
     * Compresses a string using GZIP
     */
    fun compressString(input: String): CompressionResult {
        val startTime = System.currentTimeMillis()
        val inputBytes = input.toByteArray(Charsets.UTF_8)
        val originalSize = inputBytes.size
        
        // Don't compress if too small
        if (originalSize < MIN_COMPRESSION_SIZE) {
            Log.d(TAG, "Skipping compression for small data ($originalSize bytes)")
            return CompressionResult(
                compressed = inputBytes,
                originalSize = originalSize,
                compressedSize = originalSize,
                compressionRatio = 1.0f,
                timeTakenMs = 0
            )
        }
        
        val outputStream = ByteArrayOutputStream()
        GZIPOutputStream(outputStream).use { gzip ->
            gzip.write(inputBytes)
        }
        
        val compressed = outputStream.toByteArray()
        val compressedSize = compressed.size
        val compressionRatio = compressedSize.toFloat() / originalSize
        val timeTaken = System.currentTimeMillis() - startTime
        
        // Only use compression if it actually reduces size significantly
        return if (compressionRatio < COMPRESSION_THRESHOLD_RATIO) {
            Log.d(TAG, "Compressed $originalSize bytes to $compressedSize bytes (${compressionRatio * 100}%) in ${timeTaken}ms")
            CompressionResult(
                compressed = compressed,
                originalSize = originalSize,
                compressedSize = compressedSize,
                compressionRatio = compressionRatio,
                timeTakenMs = timeTaken
            )
        } else {
            Log.d(TAG, "Compression not beneficial ($compressionRatio ratio), using original")
            CompressionResult(
                compressed = inputBytes,
                originalSize = originalSize,
                compressedSize = originalSize,
                compressionRatio = 1.0f,
                timeTakenMs = timeTaken
            )
        }
    }
    
    /**
     * Decompresses a GZIP compressed byte array to string
     */
    fun decompressString(compressed: ByteArray): String {
        val startTime = System.currentTimeMillis()
        
        return try {
            val inputStream = ByteArrayInputStream(compressed)
            val outputStream = ByteArrayOutputStream()
            
            GZIPInputStream(inputStream).use { gzip ->
                gzip.copyTo(outputStream)
            }
            
            val result = outputStream.toString(Charsets.UTF_8.name())
            val timeTaken = System.currentTimeMillis() - startTime
            
            Log.d(TAG, "Decompressed ${compressed.size} bytes to ${result.length} chars in ${timeTaken}ms")
            result
            
        } catch (e: Exception) {
            // If decompression fails, assume it wasn't compressed
            Log.w(TAG, "Decompression failed, assuming uncompressed data", e)
            String(compressed, Charsets.UTF_8)
        }
    }
    
    /**
     * Compresses byte array
     */
    fun compressBytes(input: ByteArray): CompressionResult {
        val startTime = System.currentTimeMillis()
        val originalSize = input.size
        
        if (originalSize < MIN_COMPRESSION_SIZE) {
            return CompressionResult(
                compressed = input,
                originalSize = originalSize,
                compressedSize = originalSize,
                compressionRatio = 1.0f,
                timeTakenMs = 0
            )
        }
        
        val outputStream = ByteArrayOutputStream()
        GZIPOutputStream(outputStream).use { gzip ->
            gzip.write(input)
        }
        
        val compressed = outputStream.toByteArray()
        val compressedSize = compressed.size
        val compressionRatio = compressedSize.toFloat() / originalSize
        val timeTaken = System.currentTimeMillis() - startTime
        
        return if (compressionRatio < COMPRESSION_THRESHOLD_RATIO) {
            Log.d(TAG, "Compressed bytes: $originalSize -> $compressedSize (${compressionRatio * 100}%)")
            CompressionResult(
                compressed = compressed,
                originalSize = originalSize,
                compressedSize = compressedSize,
                compressionRatio = compressionRatio,
                timeTakenMs = timeTaken
            )
        } else {
            CompressionResult(
                compressed = input,
                originalSize = originalSize,
                compressedSize = originalSize,
                compressionRatio = 1.0f,
                timeTakenMs = timeTaken
            )
        }
    }
    
    /**
     * Decompresses byte array
     */
    fun decompressBytes(compressed: ByteArray): ByteArray {
        return try {
            val inputStream = ByteArrayInputStream(compressed)
            val outputStream = ByteArrayOutputStream()
            
            GZIPInputStream(inputStream).use { gzip ->
                gzip.copyTo(outputStream)
            }
            
            outputStream.toByteArray()
            
        } catch (e: Exception) {
            Log.w(TAG, "Byte decompression failed, assuming uncompressed", e)
            compressed
        }
    }
    
    /**
     * Checks if data should be compressed based on size
     */
    fun shouldCompress(dataSize: Int): Boolean {
        return dataSize >= MIN_COMPRESSION_SIZE
    }
    
    /**
     * Estimates compression ratio for data
     */
    fun estimateCompressionRatio(input: String): Float {
        // Quick estimation based on character repetition
        val uniqueChars = input.toSet().size
        val totalChars = input.length
        
        // More unique characters = less compressible
        return (uniqueChars.toFloat() / totalChars).coerceIn(0.3f, 1.0f)
    }
}

/**
 * Manages incremental sync for optimized data transfer
 * Only syncs changed data instead of full objects
 */
@Singleton
class IncrementalSyncManager @Inject constructor() {
    
    companion object {
        private const val TAG = "IncrementalSyncManager"
    }
    
    /**
     * Represents a change in data
     */
    data class DataChange(
        val field: String,
        val oldValue: Any?,
        val newValue: Any?,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * Delta update for a script
     */
    data class ScriptDelta(
        val scriptId: String,
        val changes: List<DataChange>,
        val version: Int
    ) {
        fun isEmpty(): Boolean = changes.isEmpty()
        
        fun estimateSize(): Int {
            return changes.sumOf { change ->
                (change.field.length + 
                 (change.oldValue?.toString()?.length ?: 0) +
                 (change.newValue?.toString()?.length ?: 0)) * 2 // 2 bytes per char
            }
        }
    }
    
    /**
     * Calculates delta between two objects
     */
    fun <T> calculateDelta(
        id: String,
        old: T,
        new: T,
        version: Int,
        fieldExtractor: (T) -> Map<String, Any?>
    ): ScriptDelta {
        val oldFields = fieldExtractor(old)
        val newFields = fieldExtractor(new)
        
        val changes = mutableListOf<DataChange>()
        
        // Find changed fields
        newFields.forEach { (field, newValue) ->
            val oldValue = oldFields[field]
            if (oldValue != newValue) {
                changes.add(DataChange(field, oldValue, newValue))
            }
        }
        
        // Find removed fields
        oldFields.keys.subtract(newFields.keys).forEach { field ->
            changes.add(DataChange(field, oldFields[field], null))
        }
        
        Log.d(TAG, "Calculated delta for $id: ${changes.size} changes")
        return ScriptDelta(id, changes, version)
    }
    
    /**
     * Applies delta to an object
     */
    fun <T> applyDelta(
        original: T,
        delta: ScriptDelta,
        fieldApplier: (T, String, Any?) -> T
    ): T {
        var result = original
        
        delta.changes.forEach { change ->
            result = fieldApplier(result, change.field, change.newValue)
        }
        
        Log.d(TAG, "Applied ${delta.changes.size} changes to ${delta.scriptId}")
        return result
    }
    
    /**
     * Checks if incremental sync is beneficial
     */
    fun shouldUseIncrementalSync(delta: ScriptDelta, fullObjectSize: Int): Boolean {
        val deltaSize = delta.estimateSize()
        val ratio = deltaSize.toFloat() / fullObjectSize
        
        // Use incremental if delta is less than 50% of full object
        return ratio < 0.5f && !delta.isEmpty()
    }
}