package com.abdapps.scriptmine.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages Firebase Authentication with Google Sign-In integration
 * Provides authentication state management and user session handling
 */
@Singleton
class AuthenticationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth
) {
    
    companion object {
        private const val TAG = "AuthenticationManager"
        const val RC_SIGN_IN = 9001
    }
    
    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(com.abdapps.scriptmine.R.string.default_web_client_id))
            .requestEmail()
            .build()
        
        GoogleSignIn.getClient(context, gso)
    }
    
    /**
     * Gets the current authenticated user
     */
    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser
    
    /**
     * Checks if user is currently authenticated
     */
    val isAuthenticated: Boolean
        get() = currentUser != null
    
    /**
     * Gets the current user ID
     */
    val currentUserId: String?
        get() = currentUser?.uid
    
    /**
     * Observes authentication state changes
     */
    fun observeAuthState(): Flow<AuthState> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            val authState = if (user != null) {
                AuthState.Authenticated(
                    user = AuthUser(
                        uid = user.uid,
                        email = user.email ?: "",
                        displayName = user.displayName ?: "",
                        photoUrl = user.photoUrl?.toString()
                    )
                )
            } else {
                AuthState.Unauthenticated
            }
            
            trySend(authState)
        }
        
        firebaseAuth.addAuthStateListener(listener)
        
        // Send initial state
        listener.onAuthStateChanged(firebaseAuth)
        
        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }
    
    /**
     * Gets Google Sign-In intent
     */
    fun getGoogleSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }
    
    /**
     * Handles Google Sign-In result
     */
    suspend fun handleGoogleSignInResult(data: Intent?): AuthResult {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            
            if (account != null) {
                firebaseAuthWithGoogle(account)
            } else {
                AuthResult.Error("Google Sign-In account is null")
            }
            
        } catch (e: ApiException) {
            Log.e(TAG, "Google Sign-In failed", e)
            AuthResult.Error("Google Sign-In failed: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during Google Sign-In", e)
            AuthResult.Error("Unexpected error: ${e.message}")
        }
    }
    
    /**
     * Authenticates with Firebase using Google account
     */
    private suspend fun firebaseAuthWithGoogle(account: GoogleSignInAccount): AuthResult {
        return try {
            Log.d(TAG, "Authenticating with Firebase using Google account: ${account.email}")
            
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            
            val user = result.user
            if (user != null) {
                Log.d(TAG, "Firebase authentication successful: ${user.uid}")
                
                AuthResult.Success(
                    user = AuthUser(
                        uid = user.uid,
                        email = user.email ?: "",
                        displayName = user.displayName ?: "",
                        photoUrl = user.photoUrl?.toString()
                    ),
                    isNewUser = result.additionalUserInfo?.isNewUser ?: false
                )
            } else {
                AuthResult.Error("Firebase authentication returned null user")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Firebase authentication failed", e)
            AuthResult.Error("Firebase authentication failed: ${e.message}")
        }
    }
    
    /**
     * Signs out the current user
     */
    suspend fun signOut(): Result<Unit> {
        return try {
            Log.d(TAG, "Signing out user")
            
            // Sign out from Firebase
            firebaseAuth.signOut()
            
            // Sign out from Google
            googleSignInClient.signOut().await()
            
            Log.d(TAG, "User signed out successfully")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during sign out", e)
            Result.failure(e)
        }
    }
    
    /**
     * Deletes the current user account
     */
    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val user = currentUser
            if (user == null) {
                return Result.failure(Exception("No user is currently signed in"))
            }
            
            Log.d(TAG, "Deleting user account: ${user.uid}")
            
            // Delete Firebase account
            user.delete().await()
            
            // Sign out from Google
            googleSignInClient.signOut().await()
            
            Log.d(TAG, "User account deleted successfully")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user account", e)
            Result.failure(e)
        }
    }
    
    /**
     * Re-authenticates the current user (required for sensitive operations)
     */
    suspend fun reauthenticate(): AuthResult {
        return try {
            val user = currentUser
            if (user == null) {
                return AuthResult.Error("No user is currently signed in")
            }
            
            // Get fresh Google credentials
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account?.idToken == null) {
                return AuthResult.Error("No valid Google credentials available")
            }
            
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            user.reauthenticate(credential).await()
            
            Log.d(TAG, "User re-authenticated successfully")
            
            AuthResult.Success(
                user = AuthUser(
                    uid = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName ?: "",
                    photoUrl = user.photoUrl?.toString()
                ),
                isNewUser = false
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Re-authentication failed", e)
            AuthResult.Error("Re-authentication failed: ${e.message}")
        }
    }
    
    /**
     * Checks if the current user session is valid
     */
    suspend fun isSessionValid(): Boolean {
        return try {
            val user = currentUser
            if (user == null) {
                return false
            }
            
            // Try to get fresh token to verify session
            user.getIdToken(true).await()
            true
            
        } catch (e: Exception) {
            Log.w(TAG, "Session validation failed", e)
            false
        }
    }
    
    /**
     * Gets the current user's ID token
     */
    suspend fun getIdToken(forceRefresh: Boolean = false): String? {
        return try {
            currentUser?.getIdToken(forceRefresh)?.await()?.token
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get ID token", e)
            null
        }
    }
}

/**
 * Represents the authentication state
 */
sealed class AuthState {
    object Unauthenticated : AuthState()
    data class Authenticated(val user: AuthUser) : AuthState()
}

/**
 * Represents an authenticated user
 */
data class AuthUser(
    val uid: String,
    val email: String,
    val displayName: String,
    val photoUrl: String? = null
)

/**
 * Represents the result of an authentication operation
 */
sealed class AuthResult {
    data class Success(val user: AuthUser, val isNewUser: Boolean = false) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

/**
 * Authentication error types
 */
enum class AuthError {
    NETWORK_ERROR,
    INVALID_CREDENTIALS,
    USER_DISABLED,
    USER_NOT_FOUND,
    WEAK_PASSWORD,
    EMAIL_ALREADY_IN_USE,
    OPERATION_NOT_ALLOWED,
    UNKNOWN_ERROR
}