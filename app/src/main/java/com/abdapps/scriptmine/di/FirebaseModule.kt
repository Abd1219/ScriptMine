package com.abdapps.scriptmine.di

import android.content.Context
import com.abdapps.scriptmine.auth.AuthenticationManager
import com.abdapps.scriptmine.auth.SessionManager
import com.abdapps.scriptmine.data.database.ScriptDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        val firestore = FirebaseFirestore.getInstance()
        
        // Enable offline persistence
        val settings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        
        firestore.firestoreSettings = settings
        
        return firestore
    }
    
    @Provides
    @Singleton
    fun provideAuthenticationManager(
        @ApplicationContext context: Context,
        firebaseAuth: FirebaseAuth
    ): AuthenticationManager {
        return AuthenticationManager(context, firebaseAuth)
    }
    
    @Provides
    @Singleton
    fun provideSessionManager(
        @ApplicationContext context: Context,
        authManager: AuthenticationManager,
        scriptDao: ScriptDao
    ): SessionManager {
        return SessionManager(context, authManager, scriptDao)
    }
}