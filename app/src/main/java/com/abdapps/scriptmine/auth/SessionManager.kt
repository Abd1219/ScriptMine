package com.abdapps.scriptmine.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.abdapps.scriptmine.data.database.ScriptDao
import com.abdapps.scriptmine.data.model.SyncStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages user sessions, preferences, and user-specific data
 * Handles session persistence, user switching, and anonymous mode
 */
@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authManager: AuthenticationManager,
    private val scriptDao: ScriptDao
) {
    
    companion object {
        private const val TAG = "SessionManager"
        private const val PREFS_NAME = "scriptmine_session"
        private const val KEY_CURRENT_USER = "current_user"
        private const val KEY_SESSION_START = "session_start"
        private const val KEY_LAST_SYNC = "last_sync"
        private const val KEY_ANONYMOUS_MODE = "anonymous_mode"
        private const val KEY_USER_PREFERENCES = "user_preferences"
        private const val KEY_OFFLINE_WORK_ENABLED = "offline_work_enabled"
    }
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    private val _sessionState = MutableStateFlow(getCurrentSessionState())
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()
    
    private val _userPreferences = MutableStateFlow(getUserPreferences())
    val userPreferences: StateFlow<UserPreferences> = _userPreferences.asStateFlow()
    
    init {
        // Observe auth state changes and update session accordingly
        authManager.observeAuthState().onEach { authState ->
            handleAuthStateChange(authState)
        }.launchIn(kotlinx.coroutines.GlobalScope)
    }
    
    /**
     * Starts a new user session
     */
    suspend fun startSession(user: AuthUser) {
        Log.d(TAG, "Starting session for user: ${user.uid}")
        
        try {
            // Save current user info
            encryptedPrefs.edit()
                .putString(KEY_CURRENT_USER, Json.encodeToString(user))
                .putLong(KEY_SESSION_START, System.currentTimeMillis())
                .putBoolean(KEY_ANONYMOUS_MODE, false)
                .apply()
            
            // Update session state
            _sessionState.value = SessionState.Authenticated(
                user = user,
                sessionStart = Date(),
                isAnonymous = false
            )
            
            // Associate existing scripts with user if coming from anonymous mode
            associateAnonymousScriptsWithUser(user.uid)
            
            Log.d(TAG, "Session started successfully for user: ${user.uid}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start session", e)
        }
    }
    
    /**
     * Ends the current user session
     */
    suspend fun endSession() {
        Log.d(TAG, "Ending current session")
        
        try {
            // Clear session data
            encryptedPrefs.edit()
                .remove(KEY_CURRENT_USER)
                .remove(KEY_SESSION_START)
                .putBoolean(KEY_ANONYMOUS_MODE, true)
                .apply()
            
            // Update session state
            _sessionState.value = SessionState.Anonymous
            
            Log.d(TAG, "Session ended successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to end session", e)
        }
    }
    
    /**
     * Switches to anonymous mode
     */
    suspend fun switchToAnonymousMode() {
        Log.d(TAG, "Switching to anonymous mode")
        
        try {
            // Keep offline work enabled in anonymous mode
            encryptedPrefs.edit()
                .putBoolean(KEY_ANONYMOUS_MODE, true)
                .putBoolean(KEY_OFFLINE_WORK_ENABLED, true)
                .apply()
            
            _sessionState.value = SessionState.Anonymous
            
            Log.d(TAG, "Switched to anonymous mode")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to switch to anonymous mode", e)
        }
    }
    
    /**
     * Gets the current session state
     */
    private fun getCurrentSessionState(): SessionState {
        return try {
            val isAnonymous = encryptedPrefs.getBoolean(KEY_ANONYMOUS_MODE, true)
            
            if (isAnonymous) {
                SessionState.Anonymous
            } else {
                val userJson = encryptedPrefs.getString(KEY_CURRENT_USER, null)
                val sessionStart = encryptedPrefs.getLong(KEY_SESSION_START, 0L)
                
                if (userJson != null && sessionStart > 0) {
                    val user = Json.decodeFromString<AuthUser>(userJson)
                    SessionState.Authenticated(
                        user = user,
                        sessionStart = Date(sessionStart),
                        isAnonymous = false
                    )
                } else {
                    SessionState.Anonymous
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting session state", e)
            SessionState.Anonymous
        }
    }
    
    /**
     * Handles authentication state changes
     */
    private suspend fun handleAuthStateChange(authState: AuthState) {
        when (authState) {
            is AuthState.Authenticated -> {
                startSession(authState.user)
            }
            is AuthState.Unauthenticated -> {
                endSession()
            }
        }
    }
    
    /**
     * Associates anonymous scripts with authenticated user
     */
    private suspend fun associateAnonymousScriptsWithUser(userId: String) {
        try {
            val anonymousScripts = scriptDao.getScriptsByUser("")
            
            anonymousScripts.collect { scripts ->
                scripts.forEach { script ->
                    if (script.userId.isNullOrEmpty()) {
                        val updatedScript = script.copy(
                            userId = userId,
                            syncStatus = SyncStatus.PENDING
                        )
                        scriptDao.updateScript(updatedScript)
                    }
                }
            }
            
            Log.d(TAG, "Associated anonymous scripts with user: $userId")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to associate anonymous scripts", e)
        }
    }
    
    /**
     * Updates user preferences
     */
    fun updateUserPreferences(preferences: UserPreferences) {
        try {
            encryptedPrefs.edit()
                .putString(KEY_USER_PREFERENCES, Json.encodeToString(preferences))
                .apply()
            
            _userPreferences.value = preferences
            
            Log.d(TAG, "User preferences updated")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update user preferences", e)
        }
    }
    
    /**
     * Gets user preferences
     */
    private fun getUserPreferences(): UserPreferences {
        return try {
            val prefsJson = encryptedPrefs.getString(KEY_USER_PREFERENCES, null)
            if (prefsJson != null) {
                Json.decodeFromString<UserPreferences>(prefsJson)
            } else {
                UserPreferences() // Default preferences
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user preferences", e)
            UserPreferences() // Default preferences
        }
    }
    
    /**
     * Records last sync time
     */
    fun recordLastSync() {
        encryptedPrefs.edit()
            .putLong(KEY_LAST_SYNC, System.currentTimeMillis())
            .apply()
    }
    
    /**
     * Gets last sync time
     */
    fun getLastSyncTime(): Date? {
        val timestamp = encryptedPrefs.getLong(KEY_LAST_SYNC, 0L)
        return if (timestamp > 0) Date(timestamp) else null
    }
    
    /**
     * Checks if offline work is enabled
     */
    fun isOfflineWorkEnabled(): Boolean {
        return encryptedPrefs.getBoolean(KEY_OFFLINE_WORK_ENABLED, true)
    }
    
    /**
     * Sets offline work enabled state
     */
    fun setOfflineWorkEnabled(enabled: Boolean) {
        encryptedPrefs.edit()
            .putBoolean(KEY_OFFLINE_WORK_ENABLED, enabled)
            .apply()
    }
    
    /**
     * Gets session duration
     */
    fun getSessionDuration(): Long {
        val sessionStart = encryptedPrefs.getLong(KEY_SESSION_START, 0L)
        return if (sessionStart > 0) {
            System.currentTimeMillis() - sessionStart
        } else {
            0L
        }
    }
    
    /**
     * Clears all session data (for debugging or reset)
     */
    suspend fun clearAllSessionData() {
        Log.w(TAG, "Clearing all session data")
        
        try {
            encryptedPrefs.edit().clear().apply()
            _sessionState.value = SessionState.Anonymous
            _userPreferences.value = UserPreferences()
            
            Log.w(TAG, "All session data cleared")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear session data", e)
        }
    }
}

/**
 * Represents the current session state
 */
sealed class SessionState {
    object Anonymous : SessionState()
    data class Authenticated(
        val user: AuthUser,
        val sessionStart: Date,
        val isAnonymous: Boolean = false
    ) : SessionState()
}

/**
 * User preferences and settings
 */
@kotlinx.serialization.Serializable
data class UserPreferences(
    val autoSyncEnabled: Boolean = true,
    val syncOnWifiOnly: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val offlineWorkEnabled: Boolean = true,
    val dataCompressionEnabled: Boolean = true,
    val syncFrequencyMinutes: Int = 15,
    val maxRetryAttempts: Int = 3
)