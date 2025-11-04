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
    val updatedAt: Date = Date()
)