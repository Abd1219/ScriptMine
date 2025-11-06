package com.abdapps.scriptmine.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.util.Date

/**
 * Firebase Firestore data model for scripts
 * This model represents how scripts are stored in Firebase Firestore
 */
data class FirebaseScript(
    @DocumentId
    val id: String = "",
    val templateType: String = "",
    val clientName: String = "",
    val formData: Map<String, Any> = emptyMap(),
    val generatedScript: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null,
    val userId: String = "",
    val version: Int = 1,
    val isDeleted: Boolean = false
) {
    /**
     * Converts FirebaseScript to SavedScript for local database storage
     */
    fun toSavedScript(localId: Long = 0): SavedScript {
        return SavedScript(
            id = localId,
            templateType = templateType,
            clientName = clientName,
            formData = Json.encodeToString(formData),
            generatedScript = generatedScript,
            createdAt = createdAt?.toDate() ?: Date(),
            updatedAt = updatedAt?.toDate() ?: Date(),
            firebaseId = id,
            userId = userId,
            syncStatus = SyncStatus.SYNCED,
            lastSyncAt = Date(),
            version = version,
            isDeleted = isDeleted
        )
    }
    
    /**
     * Creates a copy with updated timestamp for modifications
     */
    fun withUpdatedTimestamp(): FirebaseScript {
        return copy(
            updatedAt = Timestamp.now(),
            version = version + 1
        )
    }
    
    /**
     * Creates a copy marked as deleted (soft delete)
     */
    fun markAsDeleted(): FirebaseScript {
        return copy(
            isDeleted = true,
            updatedAt = Timestamp.now(),
            version = version + 1
        )
    }
}

/**
 * Extension function to convert SavedScript to FirebaseScript
 */
fun SavedScript.toFirebaseScript(): FirebaseScript {
    val formDataMap = try {
        Json.decodeFromString<Map<String, Any>>(formData)
    } catch (e: Exception) {
        // Fallback: convert JSON string to a simple map
        mapOf("data" to formData)
    }
    
    return FirebaseScript(
        id = firebaseId ?: "",
        templateType = templateType,
        clientName = clientName,
        formData = formDataMap,
        generatedScript = generatedScript,
        createdAt = Timestamp(createdAt),
        updatedAt = Timestamp(updatedAt),
        userId = userId ?: "",
        version = version,
        isDeleted = isDeleted
    )
}

/**
 * Helper function to create a new FirebaseScript from form data
 */
fun createFirebaseScript(
    templateType: String,
    clientName: String,
    formData: Map<String, Any>,
    generatedScript: String,
    userId: String
): FirebaseScript {
    val now = Timestamp.now()
    return FirebaseScript(
        templateType = templateType,
        clientName = clientName,
        formData = formData,
        generatedScript = generatedScript,
        createdAt = now,
        updatedAt = now,
        userId = userId,
        version = 1,
        isDeleted = false
    )
}