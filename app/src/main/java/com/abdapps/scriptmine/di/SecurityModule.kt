package com.abdapps.scriptmine.di

import android.content.Context
import com.abdapps.scriptmine.security.EncryptionManager
import com.abdapps.scriptmine.security.SecureDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {
    
    @Provides
    @Singleton
    fun provideEncryptionManager(): EncryptionManager {
        return EncryptionManager()
    }
    
    @Provides
    @Singleton
    fun provideSecureDataStore(
        @ApplicationContext context: Context
    ): SecureDataStore {
        return SecureDataStore(context)
    }
    
    @Provides
    @Singleton
    fun provideDataValidator(): com.abdapps.scriptmine.security.DataValidator {
        return com.abdapps.scriptmine.security.DataValidator()
    }
}