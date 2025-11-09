package com.abdapps.scriptmine.performance

import android.util.Log
import android.util.LruCache
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages in-memory caching for improved performance
 * Provides LRU cache with TTL (Time To Live) support
 */
@Singleton
class CacheManager @Inject constructor() {
    
    companion object {
        private const val TAG = "CacheManager"
        private const val DEFAULT_CACHE_SIZE_MB = 10
        const val DEFAULT_TTL_MS = 5 * 60 * 1000L // 5 minutes
        private const val BYTES_PER_MB = 1024 * 1024
    }
    
    /**
     * Cache entry with TTL
     */
    private data class CacheEntry<T>(
        val value: T,
        val timestamp: Long,
        val ttl: Long
    ) {
        fun isExpired(): Boolean {
            return System.currentTimeMillis() - timestamp > ttl
        }
    }
    
    /**
     * Generic LRU cache with TTL
     */
    private class TtlLruCache<K, V>(
        maxSize: Int,
        private val defaultTtl: Long = DEFAULT_TTL_MS
    ) : LruCache<K, CacheEntry<V>>(maxSize) {
        
        private val mutex = Mutex()
        
        suspend fun putWithTtl(key: K, value: V, ttl: Long = defaultTtl) = mutex.withLock {
            put(key, CacheEntry(value, System.currentTimeMillis(), ttl))
        }
        
        suspend fun getIfValid(key: K): V? = mutex.withLock {
            val entry = get(key)
            if (entry != null && entry.isExpired()) {
                remove(key)
                null
            } else {
                entry?.value
            }
        }
        
        suspend fun clearExpired() = mutex.withLock {
            val keysToRemove = mutableListOf<K>()
            snapshot().forEach { (key, entry) ->
                if (entry.isExpired()) {
                    keysToRemove.add(key)
                }
            }
            keysToRemove.forEach { remove(it) }
        }
        
        override fun sizeOf(key: K, value: CacheEntry<V>): Int {
            // Estimate size in bytes
            return when (val v = value.value) {
                is String -> v.length * 2 // 2 bytes per char
                is ByteArray -> v.size
                is List<*> -> v.size * 100 // Rough estimate
                else -> 100 // Default size
            }
        }
    }
    
    // Different caches for different data types
    private val scriptCache = TtlLruCache<String, Any>(
        maxSize = DEFAULT_CACHE_SIZE_MB * BYTES_PER_MB,
        defaultTtl = DEFAULT_TTL_MS
    )
    
    private val templateCache = TtlLruCache<String, Any>(
        maxSize = 2 * BYTES_PER_MB,
        defaultTtl = 10 * 60 * 1000L // 10 minutes
    )
    
    private val userDataCache = TtlLruCache<String, Any>(
        maxSize = 1 * BYTES_PER_MB,
        defaultTtl = 15 * 60 * 1000L // 15 minutes
    )
    
    private val queryResultCache = TtlLruCache<String, Any>(
        maxSize = 5 * BYTES_PER_MB,
        defaultTtl = 2 * 60 * 1000L // 2 minutes
    )
    
    /**
     * Puts a value in the script cache
     */
    suspend fun <T> putScript(key: String, value: T, ttl: Long = DEFAULT_TTL_MS) {
        scriptCache.putWithTtl(key, value as Any, ttl)
        Log.d(TAG, "Cached script: $key (TTL: ${ttl}ms)")
    }
    
    /**
     * Gets a value from the script cache
     */
    suspend fun <T> getScript(key: String): T? {
        @Suppress("UNCHECKED_CAST")
        return scriptCache.getIfValid(key) as? T
    }
    
    /**
     * Puts a value in the template cache
     */
    suspend fun <T> putTemplate(key: String, value: T, ttl: Long = 10 * 60 * 1000L) {
        templateCache.putWithTtl(key, value as Any, ttl)
        Log.d(TAG, "Cached template: $key")
    }
    
    /**
     * Gets a value from the template cache
     */
    suspend fun <T> getTemplate(key: String): T? {
        @Suppress("UNCHECKED_CAST")
        return templateCache.getIfValid(key) as? T
    }
    
    /**
     * Puts a value in the user data cache
     */
    suspend fun <T> putUserData(key: String, value: T, ttl: Long = 15 * 60 * 1000L) {
        userDataCache.putWithTtl(key, value as Any, ttl)
        Log.d(TAG, "Cached user data: $key")
    }
    
    /**
     * Gets a value from the user data cache
     */
    suspend fun <T> getUserData(key: String): T? {
        @Suppress("UNCHECKED_CAST")
        return userDataCache.getIfValid(key) as? T
    }
    
    /**
     * Puts a query result in cache
     */
    suspend fun <T> putQueryResult(query: String, result: T, ttl: Long = 2 * 60 * 1000L) {
        val key = generateQueryKey(query)
        queryResultCache.putWithTtl(key, result as Any, ttl)
        Log.d(TAG, "Cached query result: $key")
    }
    
    /**
     * Gets a query result from cache
     */
    suspend fun <T> getQueryResult(query: String): T? {
        val key = generateQueryKey(query)
        @Suppress("UNCHECKED_CAST")
        return queryResultCache.getIfValid(key) as? T
    }
    
    /**
     * Generates a cache key from a query
     */
    private fun generateQueryKey(query: String): String {
        return query.hashCode().toString()
    }
    
    /**
     * Invalidates a specific cache entry
     */
    suspend fun invalidateScript(key: String) {
        scriptCache.remove(key)
        Log.d(TAG, "Invalidated script cache: $key")
    }
    
    /**
     * Invalidates template cache
     */
    suspend fun invalidateTemplate(key: String) {
        templateCache.remove(key)
        Log.d(TAG, "Invalidated template cache: $key")
    }
    
    /**
     * Invalidates user data cache
     */
    suspend fun invalidateUserData(key: String) {
        userDataCache.remove(key)
        Log.d(TAG, "Invalidated user data cache: $key")
    }
    
    /**
     * Invalidates query result cache
     */
    suspend fun invalidateQueryResult(query: String) {
        val key = generateQueryKey(query)
        queryResultCache.remove(key)
        Log.d(TAG, "Invalidated query result cache: $key")
    }
    
    /**
     * Clears all expired entries from all caches
     */
    suspend fun clearExpired() {
        Log.d(TAG, "Clearing expired cache entries")
        scriptCache.clearExpired()
        templateCache.clearExpired()
        userDataCache.clearExpired()
        queryResultCache.clearExpired()
    }
    
    /**
     * Clears all caches
     */
    suspend fun clearAll() {
        Log.w(TAG, "Clearing all caches")
        scriptCache.evictAll()
        templateCache.evictAll()
        userDataCache.evictAll()
        queryResultCache.evictAll()
    }
    
    /**
     * Gets cache statistics
     */
    fun getCacheStats(): CacheStats {
        return CacheStats(
            scriptCacheSize = scriptCache.size(),
            scriptCacheHitCount = scriptCache.hitCount(),
            scriptCacheMissCount = scriptCache.missCount(),
            templateCacheSize = templateCache.size(),
            queryCacheSize = queryResultCache.size(),
            totalCacheSize = scriptCache.size() + templateCache.size() + 
                           userDataCache.size() + queryResultCache.size()
        )
    }
    
    /**
     * Cache statistics
     */
    data class CacheStats(
        val scriptCacheSize: Int,
        val scriptCacheHitCount: Int,
        val scriptCacheMissCount: Int,
        val templateCacheSize: Int,
        val queryCacheSize: Int,
        val totalCacheSize: Int
    ) {
        val scriptCacheHitRate: Float
            get() = if (scriptCacheHitCount + scriptCacheMissCount > 0) {
                scriptCacheHitCount.toFloat() / (scriptCacheHitCount + scriptCacheMissCount)
            } else 0f
    }
}

/**
 * Cache-aware data loader
 */
@Singleton
class CachedDataLoader @Inject constructor(
    private val cacheManager: CacheManager
) {
    
    companion object {
        private const val TAG = "CachedDataLoader"
    }
    
    /**
     * Loads data with caching
     * Checks cache first, loads from source if not found
     */
    suspend fun <T> loadWithCache(
        key: String,
        ttl: Long = CacheManager.DEFAULT_TTL_MS,
        loader: suspend () -> T
    ): T {
        // Try to get from cache
        val cached = cacheManager.getScript<T>(key)
        if (cached != null) {
            Log.d(TAG, "Cache hit for key: $key")
            return cached
        }
        
        // Load from source
        Log.d(TAG, "Cache miss for key: $key, loading from source")
        val data = loader()
        
        // Store in cache
        cacheManager.putScript(key, data, ttl)
        
        return data
    }
    
    /**
     * Loads list data with caching
     */
    suspend fun <T> loadListWithCache(
        key: String,
        ttl: Long = CacheManager.DEFAULT_TTL_MS,
        loader: suspend () -> List<T>
    ): List<T> {
        val cached = cacheManager.getScript<List<T>>(key)
        if (cached != null) {
            Log.d(TAG, "Cache hit for list key: $key (${cached.size} items)")
            return cached
        }
        
        Log.d(TAG, "Cache miss for list key: $key, loading from source")
        val data = loader()
        cacheManager.putScript(key, data, ttl)
        
        return data
    }
    
    /**
     * Invalidates cache and reloads data
     */
    suspend fun <T> reloadWithCache(
        key: String,
        ttl: Long = CacheManager.DEFAULT_TTL_MS,
        loader: suspend () -> T
    ): T {
        cacheManager.invalidateScript(key)
        return loadWithCache(key, ttl, loader)
    }
}