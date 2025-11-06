package com.abdapps.scriptmine.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database migrations for ScriptMine
 * 
 * Migration from version 1 to 2:
 * - Adds Firebase sync fields to saved_scripts table
 * - Creates indexes for performance optimization
 */
object DatabaseMigrations {
    
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add new columns for Firebase sync
            database.execSQL("""
                ALTER TABLE saved_scripts 
                ADD COLUMN firebaseId TEXT
            """.trimIndent())
            
            database.execSQL("""
                ALTER TABLE saved_scripts 
                ADD COLUMN userId TEXT
            """.trimIndent())
            
            database.execSQL("""
                ALTER TABLE saved_scripts 
                ADD COLUMN syncStatus INTEGER NOT NULL DEFAULT 0
            """.trimIndent())
            
            database.execSQL("""
                ALTER TABLE saved_scripts 
                ADD COLUMN lastSyncAt INTEGER
            """.trimIndent())
            
            database.execSQL("""
                ALTER TABLE saved_scripts 
                ADD COLUMN version INTEGER NOT NULL DEFAULT 1
            """.trimIndent())
            
            database.execSQL("""
                ALTER TABLE saved_scripts 
                ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0
            """.trimIndent())
            
            // Create indexes for performance
            database.execSQL("""
                CREATE INDEX IF NOT EXISTS index_saved_scripts_syncStatus 
                ON saved_scripts(syncStatus)
            """.trimIndent())
            
            database.execSQL("""
                CREATE INDEX IF NOT EXISTS index_saved_scripts_firebaseId 
                ON saved_scripts(firebaseId)
            """.trimIndent())
            
            database.execSQL("""
                CREATE INDEX IF NOT EXISTS index_saved_scripts_userId 
                ON saved_scripts(userId)
            """.trimIndent())
            
            database.execSQL("""
                CREATE INDEX IF NOT EXISTS index_saved_scripts_isDeleted 
                ON saved_scripts(isDeleted)
            """.trimIndent())
        }
    }
    
    /**
     * Validates that the migration was successful by checking if all new columns exist
     */
    fun validateMigration1To2(database: SupportSQLiteDatabase): Boolean {
        return try {
            // Try to query the new columns
            val cursor = database.query("""
                SELECT firebaseId, userId, syncStatus, lastSyncAt, version, isDeleted 
                FROM saved_scripts LIMIT 1
            """.trimIndent())
            cursor.close()
            true
        } catch (e: Exception) {
            false
        }
    }
}