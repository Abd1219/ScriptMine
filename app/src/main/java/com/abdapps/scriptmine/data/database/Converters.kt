package com.abdapps.scriptmine.data.database

import androidx.room.TypeConverter
import com.abdapps.scriptmine.data.model.SyncStatus
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): Int {
        return status.ordinal
    }
    
    @TypeConverter
    fun toSyncStatus(ordinal: Int): SyncStatus {
        return SyncStatus.values()[ordinal]
    }
}