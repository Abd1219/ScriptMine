package com.abdapps.scriptmine.di

import android.content.Context
import androidx.room.Room
import com.abdapps.scriptmine.data.database.ScriptDao
import com.abdapps.scriptmine.data.database.ScriptDatabase
import com.abdapps.scriptmine.data.database.DatabaseMigrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideScriptDatabase(@ApplicationContext context: Context): ScriptDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            ScriptDatabase::class.java,
            "script_database"
        )
        .addMigrations(DatabaseMigrations.MIGRATION_1_2)
        .build()
    }
    
    @Provides
    fun provideScriptDao(database: ScriptDatabase): ScriptDao {
        return database.scriptDao()
    }
}