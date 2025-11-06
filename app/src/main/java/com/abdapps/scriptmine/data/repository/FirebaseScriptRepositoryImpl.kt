package com.abdapps.scriptmine.data.repository

import android.util.Log
import com.abdapps.scriptmine.data.model.FirebaseScript
import com.abdapps.scriptmine.data.model.SavedScript
import com.abdapps.scriptmine.data.model.toFirebaseScript
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseScriptRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : FirebaseScriptRepository {
    
    companion object {
        private const val TAG = "FirebaseScriptRepo"
        private const val SCRIPTS_COLLECTION = "scripts"
        private const val MAX_BATCH_SIZE = 500
    }
    
    private val scriptsCollection = firestore.collection(SCRIPTS_COLLECTION)
    
    override suspend fun uploadScript(script: SavedScript): Result<String> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            val firebaseScript = script.toFirebaseScript().copy(userId = currentUser.uid)
            
            val docRef = if (script.firebaseId != null) {
                // Update existing document
                scriptsCollection.document(script.firebaseId)
            } else {
                // Create new document
                scriptsCollection.document()
            }
            
            docRef.set(firebaseScript).await()
            
            Log.d(TAG, "Script uploaded successfully: ${docRef.id}")
            Result.success(docRef.id)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading script", e)
            Result.failure(e)
        }
    }
    
    override suspend fun downloadScript(firebaseId: String): Result<FirebaseScript> {
        return try {
            val document = scriptsCollection.document(firebaseId).get().await()
            
            if (!document.exists()) {
                return Result.failure(Exception("Script not found"))
            }
            
            val firebaseScript = document.toObject<FirebaseScript>()?.copy(id = document.id)
            
            if (firebaseScript != null) {
                Log.d(TAG, "Script downloaded successfully: $firebaseId")
                Result.success(firebaseScript)
            } else {
                Result.failure(Exception("Failed to parse script data"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading script: $firebaseId", e)
            Result.failure(e)
        }
    }
    
    override suspend fun downloadUserScripts(userId: String): Result<List<FirebaseScript>> {
        return try {
            val snapshot = scriptsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isDeleted", false)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val scripts = snapshot.documents.mapNotNull { doc ->
                doc.toObject<FirebaseScript>()?.copy(id = doc.id)
            }
            
            Log.d(TAG, "Downloaded ${scripts.size} scripts for user: $userId")
            Result.success(scripts)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading user scripts: $userId", e)
            Result.failure(e)
        }
    }
    
    override suspend fun deleteScript(firebaseId: String): Result<Unit> {
        return try {
            // Soft delete: mark as deleted instead of removing
            scriptsCollection.document(firebaseId)
                .update(
                    mapOf(
                        "isDeleted" to true,
                        "updatedAt" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()
            
            Log.d(TAG, "Script soft deleted: $firebaseId")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting script: $firebaseId", e)
            Result.failure(e)
        }
    }
    
    override fun listenToUserScripts(userId: String): Flow<List<FirebaseScript>> = callbackFlow {
        val listener = scriptsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("isDeleted", false)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to user scripts", error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val scripts = snapshot.documents.mapNotNull { doc ->
                        doc.toObject<FirebaseScript>()?.copy(id = doc.id)
                    }
                    
                    trySend(scripts)
                    Log.d(TAG, "Real-time update: ${scripts.size} scripts for user: $userId")
                }
            }
        
        awaitClose { listener.remove() }
    }
    
    override suspend fun updateScript(script: SavedScript): Result<String> {
        return try {
            if (script.firebaseId == null) {
                return Result.failure(Exception("Cannot update script without Firebase ID"))
            }
            
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            val firebaseScript = script.toFirebaseScript()
                .copy(userId = currentUser.uid)
                .withUpdatedTimestamp()
            
            scriptsCollection.document(script.firebaseId)
                .set(firebaseScript)
                .await()
            
            Log.d(TAG, "Script updated successfully: ${script.firebaseId}")
            Result.success(script.firebaseId)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating script: ${script.firebaseId}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun batchUploadScripts(scripts: List<SavedScript>): Result<Int> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            var uploadedCount = 0
            
            // Process in batches to avoid Firestore limits
            scripts.chunked(MAX_BATCH_SIZE).forEach { batch ->
                val firestoreBatch = firestore.batch()
                
                batch.forEach { script ->
                    val firebaseScript = script.toFirebaseScript().copy(userId = currentUser.uid)
                    val docRef = if (script.firebaseId != null) {
                        scriptsCollection.document(script.firebaseId)
                    } else {
                        scriptsCollection.document()
                    }
                    
                    firestoreBatch.set(docRef, firebaseScript)
                }
                
                firestoreBatch.commit().await()
                uploadedCount += batch.size
            }
            
            Log.d(TAG, "Batch upload completed: $uploadedCount scripts")
            Result.success(uploadedCount)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in batch upload", e)
            Result.failure(e)
        }
    }
    
    override suspend fun isFirebaseAvailable(): Boolean {
        return try {
            // Check if user is authenticated
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.d(TAG, "Firebase not available: User not authenticated")
                return false
            }
            
            // Try a simple Firestore operation to check connectivity
            firestore.disableNetwork().await()
            firestore.enableNetwork().await()
            
            Log.d(TAG, "Firebase is available")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Firebase not available", e)
            false
        }
    }
}