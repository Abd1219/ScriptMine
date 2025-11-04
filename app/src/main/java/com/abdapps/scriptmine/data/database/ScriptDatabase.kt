package com.abdapps.scriptmine.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.abdapps.scriptmine.data.model.SavedScript

@Database(
    entities = [SavedScript::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ScriptDatabase : RoomDatabase() {
    abstract fun scriptDao(): ScriptDao

    companion object {
        @Volatile
        private var INSTANCE: ScriptDatabase? = null

        fun getDatabase(context: Context): ScriptDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScriptDatabase::class.java,
                    "script_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}