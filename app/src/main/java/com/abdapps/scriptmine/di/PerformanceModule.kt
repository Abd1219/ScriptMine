package com.abdapps.scriptmine.di

import com.abdapps.scriptmine.performance.BatchOperationManager
import com.abdapps.scriptmine.performance.CacheManager
import com.abdapps.scriptmine.performance.CachedDataLoader
import com.abdapps.scriptmine.performance.DataCompressionManager
import com.abdapps.scriptmine.performance.IncrementalSyncManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PerformanceModule {
    
    @Provides
    @Singleton
    fun provideBatchOperationManager(): BatchOperationManager {
        return BatchOperationManager()
    }
    
    @Provides
    @Singleton
    fun provideCacheManager(): CacheManager {
        return CacheManager()
    }
    
    @Provides
    @Singleton
    fun provideCachedDataLoader(cacheManager: CacheManager): CachedDataLoader {
        return CachedDataLoader(cacheManager)
    }
    
    @Provides
    @Singleton
    fun provideDataCompressionManager(): DataCompressionManager {
        return DataCompressionManager()
    }
    
    @Provides
    @Singleton
    fun provideIncrementalSyncManager(): IncrementalSyncManager {
        return IncrementalSyncManager()
    }
}