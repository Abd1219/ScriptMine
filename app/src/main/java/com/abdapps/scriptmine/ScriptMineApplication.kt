package com.abdapps.scriptmine

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for ScriptMine
 * Initializes Firebase and other app-wide components
 */
@HiltAndroidApp
class ScriptMineApplication : Application() {
    
    companion object {
        private const val TAG = "ScriptMineApp"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        try {
            FirebaseApp.initializeApp(this)
            Log.d(TAG, "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firebase", e)
        }
    }
}
