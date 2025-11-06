package com.abdapps.scriptmine.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "saved_scripts")
data class SavedScript(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val templateType: String,
    val clientName: String,
    val formData: String, // JSON string of form data
    val generatedScript: String,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    
    // Firebase sync fields
    val firebaseId: String? = null,           // Firebase document ID
    val userId: String? = null,               // Firebase user ID
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val lastSyncAt: Date? = null,
    val version: Int = 1,                     // For conflict resolution
    val isDeleted: Boolean = false            // Soft delete for sync
)