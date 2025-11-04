package com.abdapps.scriptmine

import android.app.Application
import com.abdapps.scriptmine.data.database.ScriptDatabase
import com.abdapps.scriptmine.data.repository.ScriptRepository

class ScriptMineApplication : Application() {
    
    val database by lazy { ScriptDatabase.getDatabase(this) }
    val repository by lazy { ScriptRepository(database.scriptDao()) }
}