package com.abdapps.scriptmine

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.abdapps.scriptmine.sync.SyncScheduler
import com.abdapps.scriptmine.utils.NetworkMonitor
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Application class for ScriptMine
 * Initializes Firebase, WorkManager, and sync components
 */
@HiltAndroidApp
class ScriptMineApplication : Application(), Configuration.Provider {
    
    @Inject
    lateinit var syncScheduler: SyncScheduler
    
    @Inject
    lateinit var networkMonitor: NetworkMonitor
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        Timber.d("Firebase initialized")
        
        // Initialize WorkManager
        WorkManager.initialize(this, workManagerConfiguration)
        Timber.d("WorkManager initialized")
        
        // Setup sync scheduler
        applicationScope.launch {
            setupSyncScheduler()
        }
        
        // Monitor network changes
        applicationScope.launch {
            monitorNetworkChanges()
        }
    }
    
    private suspend fun setupSyncScheduler() {
        try {
            // Schedule periodic sync
            syncScheduler.scheduleSyncWork()
            Timber.d("Sync scheduler initialized")
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize sync scheduler")
        }
    }
    
    private suspend fun monitorNetworkChanges() {
        try {
            networkMonitor.isConnected.collect { isConnected ->
                if (isConnected) {
                    Timber.d("Network connected - triggering sync")
                    syncScheduler.triggerImmediateSync()
                } else {
                    Timber.d("Network disconnected")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error monitoring network changes")
        }
    }
    
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(
                if (BuildConfig.DEBUG) android.util.Log.DEBUG 
                else android.util.Log.ERROR
            )
            .build()
    }
}
