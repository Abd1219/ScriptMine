package com.abdapps.scriptmine.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure data store for sensitive information
 * Uses EncryptedSharedPreferences for secure storage
 */
@Singleton
class SecureDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val PREFS_NAME = "scriptmine_secure_data"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_LAST_BACKUP_TIME = "last_backup_time"
        private const val KEY_ENCRYPTION_SALT = "encryption_salt"
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
    
    /**
     * Stores authentication token securely
     */
    fun saveAuthToken(token: String) {
        encryptedPrefs.edit()
            .putString(KEY_AUTH_TOKEN, token)
            .apply()
    }
    
    /**
     * Retrieves authentication token
     */
    fun getAuthToken(): String? {
        return encryptedPrefs.getString(KEY_AUTH_TOKEN, null)
    }
    
    /**
     * Stores refresh token securely
     */
    fun saveRefreshToken(token: String) {
        encryptedPrefs.edit()
            .putString(KEY_REFRESH_TOKEN, token)
            .apply()
    }
    
    /**
     * Retrieves refresh token
     */
    fun getRefreshToken(): String? {
        return encryptedPrefs.getString(KEY_REFRESH_TOKEN, null)
    }
    
    /**
     * Stores user ID securely
     */
    fun saveUserId(userId: String) {
        encryptedPrefs.edit()
            .putString(KEY_USER_ID, userId)
            .apply()
    }
    
    /**
     * Retrieves user ID
     */
    fun getUserId(): String? {
        return encryptedPrefs.getString(KEY_USER_ID, null)
    }
    
    /**
     * Stores API key securely
     */
    fun saveApiKey(apiKey: String) {
        encryptedPrefs.edit()
            .putString(KEY_API_KEY, apiKey)
            .apply()
    }
    
    /**
     * Retrieves API key
     */
    fun getApiKey(): String? {
        return encryptedPrefs.getString(KEY_API_KEY, null)
    }
    
    /**
     * Stores last backup timestamp
     */
    fun saveLastBackupTime(timestamp: Long) {
        encryptedPrefs.edit()
            .putLong(KEY_LAST_BACKUP_TIME, timestamp)
            .apply()
    }
    
    /**
     * Retrieves last backup timestamp
     */
    fun getLastBackupTime(): Long {
        return encryptedPrefs.getLong(KEY_LAST_BACKUP_TIME, 0L)
    }
    
    /**
     * Stores encryption salt
     */
    fun saveEncryptionSalt(salt: String) {
        encryptedPrefs.edit()
            .putString(KEY_ENCRYPTION_SALT, salt)
            .apply()
    }
    
    /**
     * Retrieves encryption salt
     */
    fun getEncryptionSalt(): String? {
        return encryptedPrefs.getString(KEY_ENCRYPTION_SALT, null)
    }
    
    /**
     * Stores a custom secure value
     */
    fun saveSecureValue(key: String, value: String) {
        encryptedPrefs.edit()
            .putString(key, value)
            .apply()
    }
    
    /**
     * Retrieves a custom secure value
     */
    fun getSecureValue(key: String): String? {
        return encryptedPrefs.getString(key, null)
    }
    
    /**
     * Stores a secure boolean value
     */
    fun saveSecureBoolean(key: String, value: Boolean) {
        encryptedPrefs.edit()
            .putBoolean(key, value)
            .apply()
    }
    
    /**
     * Retrieves a secure boolean value
     */
    fun getSecureBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return encryptedPrefs.getBoolean(key, defaultValue)
    }
    
    /**
     * Stores a secure long value
     */
    fun saveSecureLong(key: String, value: Long) {
        encryptedPrefs.edit()
            .putLong(key, value)
            .apply()
    }
    
    /**
     * Retrieves a secure long value
     */
    fun getSecureLong(key: String, defaultValue: Long = 0L): Long {
        return encryptedPrefs.getLong(key, defaultValue)
    }
    
    /**
     * Checks if a key exists
     */
    fun contains(key: String): Boolean {
        return encryptedPrefs.contains(key)
    }
    
    /**
     * Removes a specific key
     */
    fun remove(key: String) {
        encryptedPrefs.edit()
            .remove(key)
            .apply()
    }
    
    /**
     * Clears all authentication tokens
     */
    fun clearAuthTokens() {
        encryptedPrefs.edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .apply()
    }
    
    /**
     * Clears all secure data
     * WARNING: This will remove all stored secure information
     */
    fun clearAll() {
        encryptedPrefs.edit()
            .clear()
            .apply()
    }
    
    /**
     * Gets all stored keys (for debugging)
     */
    fun getAllKeys(): Set<String> {
        return encryptedPrefs.all.keys
    }
    
    /**
     * Checks if secure storage is properly initialized
     */
    fun isInitialized(): Boolean {
        return try {
            encryptedPrefs.all
            true
        } catch (e: Exception) {
            false
        }
    }
}