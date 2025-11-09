package com.abdapps.scriptmine.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages encryption and decryption of sensitive data using Android Keystore
 * Provides AES-GCM encryption for secure data storage
 */
@Singleton
class EncryptionManager @Inject constructor() {
    
    companion object {
        private const val TAG = "EncryptionManager"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "ScriptMineEncryptionKey"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val IV_SEPARATOR = "]"
    }
    
    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }
    
    init {
        // Ensure encryption key exists
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            generateKey()
        }
    }
    
    /**
     * Generates a new encryption key in Android Keystore
     */
    private fun generateKey() {
        try {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setUserAuthenticationRequired(false)
                .build()
            
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
            
            Log.d(TAG, "Encryption key generated successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate encryption key", e)
            throw SecurityException("Failed to generate encryption key", e)
        }
    }
    
    /**
     * Gets the encryption key from Android Keystore
     */
    private fun getKey(): SecretKey {
        return try {
            val entry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
            entry.secretKey
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get encryption key", e)
            throw SecurityException("Failed to get encryption key", e)
        }
    }
    
    /**
     * Encrypts a string using AES-GCM
     * @param plainText The text to encrypt
     * @return Base64 encoded encrypted text with IV prepended
     */
    fun encrypt(plainText: String): String {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getKey())
            
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            
            // Combine IV and encrypted data
            val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            val encryptedBase64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
            
            "$ivBase64$IV_SEPARATOR$encryptedBase64"
            
        } catch (e: Exception) {
            Log.e(TAG, "Encryption failed", e)
            throw SecurityException("Encryption failed", e)
        }
    }
    
    /**
     * Decrypts an encrypted string
     * @param encryptedText Base64 encoded encrypted text with IV prepended
     * @return Decrypted plain text
     */
    fun decrypt(encryptedText: String): String {
        return try {
            // Split IV and encrypted data
            val parts = encryptedText.split(IV_SEPARATOR)
            if (parts.size != 2) {
                throw IllegalArgumentException("Invalid encrypted text format")
            }
            
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val encryptedBytes = Base64.decode(parts[1], Base64.NO_WRAP)
            
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, getKey(), spec)
            
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8)
            
        } catch (e: Exception) {
            Log.e(TAG, "Decryption failed", e)
            throw SecurityException("Decryption failed", e)
        }
    }
    
    /**
     * Encrypts sensitive data if it's not empty
     * @param data The data to encrypt
     * @return Encrypted data or empty string if input is empty
     */
    fun encryptIfNotEmpty(data: String?): String {
        return if (data.isNullOrEmpty()) {
            ""
        } else {
            encrypt(data)
        }
    }
    
    /**
     * Decrypts data if it's not empty
     * @param encryptedData The encrypted data
     * @return Decrypted data or empty string if input is empty
     */
    fun decryptIfNotEmpty(encryptedData: String?): String {
        return if (encryptedData.isNullOrEmpty()) {
            ""
        } else {
            try {
                decrypt(encryptedData)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to decrypt data, returning empty string", e)
                ""
            }
        }
    }
    
    /**
     * Hashes a string using SHA-256
     * Useful for creating secure identifiers
     */
    fun hash(input: String): String {
        return try {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(hashBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Hashing failed", e)
            throw SecurityException("Hashing failed", e)
        }
    }
    
    /**
     * Generates a secure random token
     * @param length The length of the token in bytes (default 32)
     * @return Base64 encoded random token
     */
    fun generateSecureToken(length: Int = 32): String {
        return try {
            val random = java.security.SecureRandom()
            val bytes = ByteArray(length)
            random.nextBytes(bytes)
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Token generation failed", e)
            throw SecurityException("Token generation failed", e)
        }
    }
    
    /**
     * Validates if a string is properly encrypted
     * @param text The text to validate
     * @return true if the text appears to be encrypted
     */
    fun isEncrypted(text: String?): Boolean {
        if (text.isNullOrEmpty()) return false
        
        return try {
            val parts = text.split(IV_SEPARATOR)
            parts.size == 2 && 
            parts[0].isNotEmpty() && 
            parts[1].isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Clears the encryption key (use with caution)
     * This will make all encrypted data unrecoverable
     */
    fun clearKey() {
        try {
            if (keyStore.containsAlias(KEY_ALIAS)) {
                keyStore.deleteEntry(KEY_ALIAS)
                Log.w(TAG, "Encryption key deleted")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete encryption key", e)
        }
    }
    
    /**
     * Re-generates the encryption key
     * WARNING: This will make all previously encrypted data unrecoverable
     */
    fun regenerateKey() {
        clearKey()
        generateKey()
        Log.w(TAG, "Encryption key regenerated")
    }
}

/**
 * Extension functions for easy encryption/decryption
 */
fun String.encrypt(encryptionManager: EncryptionManager): String {
    return encryptionManager.encrypt(this)
}

fun String.decrypt(encryptionManager: EncryptionManager): String {
    return encryptionManager.decrypt(this)
}

fun String.hash(encryptionManager: EncryptionManager): String {
    return encryptionManager.hash(this)
}