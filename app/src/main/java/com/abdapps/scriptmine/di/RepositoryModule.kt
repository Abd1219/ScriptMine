package com.abdapps.scriptmine.di

import com.abdapps.scriptmine.data.database.ScriptDao
import com.abdapps.scriptmine.data.repository.ScriptRepository
import com.abdapps.scriptmine.data.repository.FirebaseScriptRepository
import com.abdapps.scriptmine.data.repository.FirebaseScriptRepositoryImpl
import com.abdapps.scriptmine.data.repository.HybridScriptRepository
import com.abdapps.scriptmine.sync.SyncManager
import com.abdapps.scriptmine.utils.NetworkMonitor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LocalRepository

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class HybridRepository

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    @LocalRepository
    fun provideLocalScriptRepository(scriptDao: ScriptDao): ScriptRepository {
        return ScriptRepository(scriptDao)
    }
    
    @Provides
    @Singleton
    fun provideFirebaseScriptRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): FirebaseScriptRepository {
        return FirebaseScriptRepositoryImpl(firestore, auth)
    }
    
    @Provides
    @Singleton
    @HybridRepository
    fun provideHybridScriptRepository(
        scriptDao: ScriptDao,
        firebaseRepo: FirebaseScriptRepository,
        syncManager: SyncManager,
        networkMonitor: NetworkMonitor
    ): HybridScriptRepository {
        return HybridScriptRepository(scriptDao, firebaseRepo, syncManager, networkMonitor)
    }
    
    // Primary repository - use local for now, hybrid will be used directly where needed
    @Provides
    @Singleton
    fun provideScriptRepository(
        @LocalRepository localRepository: ScriptRepository
    ): ScriptRepository {
        return localRepository
    }
}