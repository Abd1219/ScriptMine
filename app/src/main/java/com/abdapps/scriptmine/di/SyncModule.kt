package com.abdapps.scriptmine.di

import android.content.Context
import androidx.work.WorkManager
import com.abdapps.scriptmine.data.database.ScriptDao
import com.abdapps.scriptmine.data.repository.FirebaseScriptRepository
import com.abdapps.scriptmine.data.repository.HybridScriptRepository
import com.abdapps.scriptmine.sync.ConflictResolver
import com.abdapps.scriptmine.sync.SyncManager
import com.abdapps.scriptmine.sync.SyncScheduler
import com.abdapps.scriptmine.sync.SyncTriggers
import com.abdapps.scriptmine.utils.NetworkMonitor
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SyncModule {
    
    @Provides
    @Singleton
    fun provideConflictResolver(): ConflictResolver {
        return ConflictResolver()
    }
    
    @Provides
    @Singleton
    fun provideSyncManager(
        localDao: ScriptDao,
        firebaseRepo: FirebaseScriptRepository,
        conflictResolver: ConflictResolver,
        auth: FirebaseAuth
    ): SyncManager {
        return SyncManager(localDao, firebaseRepo, conflictResolver, auth)
    }
    
    @Provides
    @Singleton
    fun provideSyncScheduler(
        @ApplicationContext context: Context,
        networkMonitor: NetworkMonitor,
        workManager: WorkManager
    ): SyncScheduler {
        return SyncScheduler(context, networkMonitor, workManager)
    }
    
    @Provides
    @Singleton
    fun provideSyncTriggers(
        @ApplicationContext context: Context,
        syncScheduler: SyncScheduler,
        hybridRepository: HybridScriptRepository,
        networkMonitor: NetworkMonitor
    ): SyncTriggers {
        return SyncTriggers(context, syncScheduler, hybridRepository, networkMonitor)
    }
}