package com.abdapps.scriptmine.data.repository

import com.abdapps.scriptmine.data.model.FirebaseScript
import com.abdapps.scriptmine.data.model.SavedScript
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Firebase Firestore operations
 */
interface FirebaseScriptRepository {
    
    /**
     * Upload a script to Firebase Firestore
     * @param script The SavedScript to upload
     * @return Result containing the Firebase document ID on success
     */
    suspend fun uploadScript(script: SavedScript): Result<String>
    
    /**
     * Download a specific script from Firebase
     * @param firebaseId The Firebase document ID
     * @return Result containing the FirebaseScript on success
     */
    suspend fun downloadScript(firebaseId: String): Result<FirebaseScript>
    
    /**
     * Download all scripts for a specific user
     * @param userId The Firebase user ID
     * @return Result containing list of FirebaseScript on success
     */
    suspend fun downloadUserScripts(userId: String): Result<List<FirebaseScript>>
    
    /**
     * Delete a script from Firebase (soft delete)
     * @param firebaseId The Firebase document ID to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteScript(firebaseId: String): Result<Unit>
    
    /**
     * Listen to real-time updates for user scripts
     * @param userId The Firebase user ID
     * @return Flow of list of FirebaseScript with real-time updates
     */
    fun listenToUserScripts(userId: String): Flow<List<FirebaseScript>>
    
    /**
     * Update an existing script in Firebase
     * @param script The SavedScript with updated data
     * @return Result containing the Firebase document ID on success
     */
    suspend fun updateScript(script: SavedScript): Result<String>
    
    /**
     * Batch upload multiple scripts
     * @param scripts List of SavedScript to upload
     * @return Result indicating success or failure with count of uploaded scripts
     */
    suspend fun batchUploadScripts(scripts: List<SavedScript>): Result<Int>
    
    /**
     * Check if Firebase is available and user is authenticated
     * @return Boolean indicating Firebase availability
     */
    suspend fun isFirebaseAvailable(): Boolean
}