package com.abdapps.scriptmine.di

import com.abdapps.scriptmine.error.CircuitBreaker
import com.abdapps.scriptmine.error.ErrorHandler
import com.abdapps.scriptmine.error.RetryManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ErrorHandlingModule {
    
    @Provides
    @Singleton
    fun provideErrorHandler(): ErrorHandler {
        return ErrorHandler()
    }
    
    @Provides
    @Singleton
    fun provideRetryManager(errorHandler: ErrorHandler): RetryManager {
        return RetryManager(errorHandler)
    }
    
    @Provides
    @Singleton
    fun provideCircuitBreaker(): CircuitBreaker {
        return CircuitBreaker()
    }
}