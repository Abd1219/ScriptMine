package com.abdapps.scriptmine.sync

import android.util.Log
import com.abdapps.scriptmine.data.model.FirebaseScript
import com.abdapps.scriptmine.data.model.SavedScript
import com.abdapps.scriptmine.data.model.SyncStatus
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles conflict resolution between local and remote script versions
 * Implements various strategies for resolving data conflicts during sync
 */
@Singleton
class ConflictResolver @Inject constructor() {
    
    companion object {
        private const val TAG = "ConflictResolver"
    }
    
    /**
     * Checks if there's a conflict between local and remote versions
     * @param local Local SavedScript
     * @param remote Remote FirebaseScript
     * @return true if conflict exists, false otherwise
     */
    fun hasConflict(local: SavedScript, remote: FirebaseScript): Boolean {
        // Same version but different content indicates a conflict
        if (local.version != remote.version) {
            return false // Different versions, not a conflict but a version mismatch
        }
        
        // Check if content differs
        val localFormData = parseFormData(local.formData)
        val remoteFormData = remote.formData
        
        val contentDiffers = local.templateType != remote.templateType ||
                local.clientName != remote.clientName ||
                local.generatedScript != remote.generatedScript ||
                !areFormDataEqual(localFormData, remoteFormData)
        
        if (contentDiffers) {
            Log.d(TAG, "Conflict detected for script ${local.id}: same version but different content")
        }
        
        return contentDiffers
    }
    
    /**
     * Resolves conflict between local and remote versions
     * @param local Local SavedScript
     * @param remote Remote FirebaseScript
     * @return Resolved SavedScript
     */
    fun resolve(local: SavedScript, remote: FirebaseScript): SavedScript {
        Log.d(TAG, "Resolving conflict for script ${local.id}")
        
        return when (determineResolutionStrategy(local, remote)) {
            ConflictResolutionStrategy.PREFER_LOCAL -> {
                Log.d(TAG, "Resolution: Prefer local version")
                resolvePreferLocal(local)
            }
            
            ConflictResolutionStrategy.PREFER_REMOTE -> {
                Log.d(TAG, "Resolution: Prefer remote version")
                resolvePreferRemote(local, remote)
            }
            
            ConflictResolutionStrategy.MERGE -> {
                Log.d(TAG, "Resolution: Merge versions")
                resolveMerge(local, remote)
            }
            
            ConflictResolutionStrategy.MANUAL -> {
                Log.d(TAG, "Resolution: Requires manual intervention")
                resolveManual(local, remote)
            }
        }
    }
    
    /**
     * Determines the best resolution strategy based on conflict analysis
     */
    private fun determineResolutionStrategy(
        local: SavedScript, 
        remote: FirebaseScript
    ): ConflictResolutionStrategy {
        
        val localTime = local.updatedAt
        val remoteTime = remote.updatedAt?.toDate() ?: Date(0)
        val timeDifference = kotlin.math.abs(localTime.time - remoteTime.time)
        
        return when {
            // If timestamps are very close (within 5 seconds), prefer merge
            timeDifference < 5000 -> ConflictResolutionStrategy.MERGE
            
            // If local is significantly newer (more than 1 minute), prefer local
            localTime.time > remoteTime.time + 60000 -> ConflictResolutionStrategy.PREFER_LOCAL
            
            // If remote is significantly newer (more than 1 minute), prefer remote
            remoteTime.time > localTime.time + 60000 -> ConflictResolutionStrategy.PREFER_REMOTE
            
            // If only form data differs, try to merge
            canMergeFormData(local, remote) -> ConflictResolutionStrategy.MERGE
            
            // Otherwise, require manual resolution
            else -> ConflictResolutionStrategy.MANUAL
        }
    }
    
    /**
     * Resolves conflict by preferring the local version
     */
    private fun resolvePreferLocal(local: SavedScript): SavedScript {
        return local.copy(
            syncStatus = SyncStatus.PENDING, // Will be uploaded to Firebase
            version = local.version + 1,
            updatedAt = Date()
        )
    }
    
    /**
     * Resolves conflict by preferring the remote version
     */
    private fun resolvePreferRemote(local: SavedScript, remote: FirebaseScript): SavedScript {
        return local.copy(
            templateType = remote.templateType,
            clientName = remote.clientName,
            formData = Json.encodeToString(remote.formData),
            generatedScript = remote.generatedScript,
            updatedAt = remote.updatedAt?.toDate() ?: Date(),
            syncStatus = SyncStatus.SYNCED,
            version = remote.version,
            lastSyncAt = Date()
        )
    }
    
    /**
     * Resolves conflict by merging both versions intelligently
     */
    private fun resolveMerge(local: SavedScript, remote: FirebaseScript): SavedScript {
        val localFormData = parseFormData(local.formData)
        val remoteFormData = remote.formData
        
        // Merge form data - prefer non-empty values
        val mergedFormData = mergeFormData(localFormData, remoteFormData)
        
        // Use the most recent timestamp
        val localTime = local.updatedAt
        val remoteTime = remote.updatedAt?.toDate() ?: Date(0)
        val useLocal = localTime.after(remoteTime)
        
        return local.copy(
            templateType = if (useLocal) local.templateType else remote.templateType,
            clientName = if (useLocal) local.clientName else remote.clientName,
            formData = Json.encodeToString(mergedFormData),
            generatedScript = if (useLocal) local.generatedScript else remote.generatedScript,
            updatedAt = if (useLocal) localTime else remoteTime,
            syncStatus = SyncStatus.PENDING, // Merged version needs to be uploaded
            version = maxOf(local.version, remote.version) + 1,
            lastSyncAt = Date()
        )
    }
    
    /**
     * Marks conflict for manual resolution
     */
    private fun resolveManual(local: SavedScript, remote: FirebaseScript): SavedScript {
        return local.copy(
            syncStatus = SyncStatus.CONFLICT,
            lastSyncAt = Date()
        )
    }
    
    /**
     * Checks if form data can be automatically merged
     */
    private fun canMergeFormData(local: SavedScript, remote: FirebaseScript): Boolean {
        val localFormData = parseFormData(local.formData)
        val remoteFormData = remote.formData
        
        // Can merge if there are no conflicting non-empty values
        for ((key, localValue) in localFormData) {
            val remoteValue = remoteFormData[key]
            if (remoteValue != null && 
                localValue.toString().isNotEmpty() && 
                remoteValue.toString().isNotEmpty() && 
                localValue != remoteValue) {
                return false
            }
        }
        
        return true
    }
    
    /**
     * Merges form data from local and remote versions
     */
    private fun mergeFormData(
        localData: Map<String, Any>, 
        remoteData: Map<String, Any>
    ): Map<String, Any> {
        val merged = mutableMapOf<String, Any>()
        
        // Add all keys from both maps
        val allKeys = (localData.keys + remoteData.keys).toSet()
        
        for (key in allKeys) {
            val localValue = localData[key]
            val remoteValue = remoteData[key]
            
            merged[key] = when {
                // If only one has a value, use it
                localValue == null -> remoteValue ?: ""
                remoteValue == null -> localValue
                
                // If both have values, prefer non-empty ones
                localValue.toString().isEmpty() -> remoteValue
                remoteValue.toString().isEmpty() -> localValue
                
                // If both have non-empty values, prefer the longer one (more detailed)
                localValue.toString().length >= remoteValue.toString().length -> localValue
                else -> remoteValue
            }
        }
        
        return merged
    }
    
    /**
     * Parses form data JSON string to Map
     */
    private fun parseFormData(formDataJson: String): Map<String, Any> {
        return try {
            Json.decodeFromString<Map<String, Any>>(formDataJson)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse form data JSON: $formDataJson", e)
            emptyMap()
        }
    }
    
    /**
     * Compares two form data maps for equality
     */
    private fun areFormDataEqual(data1: Map<String, Any>, data2: Map<String, Any>): Boolean {
        if (data1.size != data2.size) return false
        
        for ((key, value1) in data1) {
            val value2 = data2[key]
            if (value1 != value2) return false
        }
        
        return true
    }
    
    /**
     * Gets a human-readable description of the conflict
     */
    fun getConflictDescription(local: SavedScript, remote: FirebaseScript): String {
        val differences = mutableListOf<String>()
        
        if (local.templateType != remote.templateType) {
            differences.add("Template type differs")
        }
        
        if (local.clientName != remote.clientName) {
            differences.add("Client name differs")
        }
        
        if (local.generatedScript != remote.generatedScript) {
            differences.add("Generated script differs")
        }
        
        val localFormData = parseFormData(local.formData)
        if (!areFormDataEqual(localFormData, remote.formData)) {
            differences.add("Form data differs")
        }
        
        return if (differences.isEmpty()) {
            "No specific differences detected"
        } else {
            differences.joinToString(", ")
        }
    }
}

/**
 * Strategies for resolving conflicts
 */
enum class ConflictResolutionStrategy {
    PREFER_LOCAL,   // Use local version
    PREFER_REMOTE,  // Use remote version
    MERGE,          // Merge both versions
    MANUAL          // Requires manual intervention
}