package com.abdapps.scriptmine

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import androidx.work.WorkManager
import com.abdapps.scriptmine.sync.SyncScheduler
import com.abdapps.scriptmine.sync.SyncTriggers
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ScriptMineApplication : Application() {
    
    @Inject
    lateinit var syncScheduler: SyncScheduler
    
    @Inject
    lateinit var syncTriggers: SyncTriggers
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize WorkManager with custom configuration
        initializeWorkManager()
        
        // Initialize sync system
        initializeSyncSystem()
    }
    
    private fun initializeWorkManager() {
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
        
        WorkManager.initialize(this, config)
    }
    
    private fun initializeSyncSystem() {
        // Initialize sync scheduler
        syncScheduler.initialize()
        
        // Initialize sync triggers
        syncTriggers.initialize()
        
        // Register lifecycle observers
        ProcessLifecycleOwner.get().lifecycle.addObserver(syncScheduler)
        ProcessLifecycleOwner.get().lifecycle.addObserver(syncTriggers)
    }
}