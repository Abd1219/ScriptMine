package com.abdapps.scriptmine.security

import android.util.Patterns
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Validates and sanitizes user input data
 * Prevents injection attacks and ensures data integrity
 */
@Singleton
class DataValidator @Inject constructor() {
    
    companion object {
        private const val MAX_SCRIPT_TITLE_LENGTH = 200
        private const val MAX_SCRIPT_CONTENT_LENGTH = 50000
        private const val MAX_TEMPLATE_NAME_LENGTH = 100
        private const val MAX_USER_NAME_LENGTH = 100
        private const val MAX_EMAIL_LENGTH = 254
        
        // Dangerous patterns that could indicate injection attempts
        private val DANGEROUS_PATTERNS = listOf(
            "<script",
            "javascript:",
            "onerror=",
            "onclick=",
            "onload=",
            "../",
            "..\\",
            "DROP TABLE",
            "DELETE FROM",
            "INSERT INTO",
            "UPDATE SET"
        )
    }
    
    /**
     * Validates and sanitizes script title
     */
    fun validateScriptTitle(title: String?): ValidationResult {
        if (title.isNullOrBlank()) {
            return ValidationResult.Invalid("Script title cannot be empty")
        }
        
        if (title.length > MAX_SCRIPT_TITLE_LENGTH) {
            return ValidationResult.Invalid("Script title is too long (max $MAX_SCRIPT_TITLE_LENGTH characters)")
        }
        
        if (containsDangerousPatterns(title)) {
            return ValidationResult.Invalid("Script title contains invalid characters")
        }
        
        val sanitized = sanitizeText(title)
        return ValidationResult.Valid(sanitized)
    }
    
    /**
     * Validates and sanitizes script content
     */
    fun validateScriptContent(content: String?): ValidationResult {
        if (content.isNullOrBlank()) {
            return ValidationResult.Invalid("Script content cannot be empty")
        }
        
        if (content.length > MAX_SCRIPT_CONTENT_LENGTH) {
            return ValidationResult.Invalid("Script content is too long (max $MAX_SCRIPT_CONTENT_LENGTH characters)")
        }
        
        // Script content can contain more characters, but we still sanitize
        val sanitized = sanitizeScriptContent(content)
        return ValidationResult.Valid(sanitized)
    }
    
    /**
     * Validates template name
     */
    fun validateTemplateName(name: String?): ValidationResult {
        if (name.isNullOrBlank()) {
            return ValidationResult.Invalid("Template name cannot be empty")
        }
        
        if (name.length > MAX_TEMPLATE_NAME_LENGTH) {
            return ValidationResult.Invalid("Template name is too long (max $MAX_TEMPLATE_NAME_LENGTH characters)")
        }
        
        if (containsDangerousPatterns(name)) {
            return ValidationResult.Invalid("Template name contains invalid characters")
        }
        
        val sanitized = sanitizeText(name)
        return ValidationResult.Valid(sanitized)
    }
    
    /**
     * Validates user name
     */
    fun validateUserName(name: String?): ValidationResult {
        if (name.isNullOrBlank()) {
            return ValidationResult.Invalid("User name cannot be empty")
        }
        
        if (name.length > MAX_USER_NAME_LENGTH) {
            return ValidationResult.Invalid("User name is too long (max $MAX_USER_NAME_LENGTH characters)")
        }
        
        if (containsDangerousPatterns(name)) {
            return ValidationResult.Invalid("User name contains invalid characters")
        }
        
        val sanitized = sanitizeText(name)
        return ValidationResult.Valid(sanitized)
    }
    
    /**
     * Validates email address
     */
    fun validateEmail(email: String?): ValidationResult {
        if (email.isNullOrBlank()) {
            return ValidationResult.Invalid("Email cannot be empty")
        }
        
        if (email.length > MAX_EMAIL_LENGTH) {
            return ValidationResult.Invalid("Email is too long")
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return ValidationResult.Invalid("Invalid email format")
        }
        
        val sanitized = email.trim().lowercase()
        return ValidationResult.Valid(sanitized)
    }
    
    /**
     * Validates user ID format
     */
    fun validateUserId(userId: String?): ValidationResult {
        if (userId.isNullOrBlank()) {
            return ValidationResult.Invalid("User ID cannot be empty")
        }
        
        // User IDs should be alphanumeric with possible hyphens and underscores
        if (!userId.matches(Regex("^[a-zA-Z0-9_-]+$"))) {
            return ValidationResult.Invalid("Invalid user ID format")
        }
        
        return ValidationResult.Valid(userId)
    }
    
    /**
     * Validates Firebase document ID
     */
    fun validateFirebaseId(id: String?): ValidationResult {
        if (id.isNullOrBlank()) {
            return ValidationResult.Invalid("Firebase ID cannot be empty")
        }
        
        // Firebase IDs should be alphanumeric
        if (!id.matches(Regex("^[a-zA-Z0-9]+$"))) {
            return ValidationResult.Invalid("Invalid Firebase ID format")
        }
        
        return ValidationResult.Valid(id)
    }
    
    /**
     * Validates URL format
     */
    fun validateUrl(url: String?): ValidationResult {
        if (url.isNullOrBlank()) {
            return ValidationResult.Invalid("URL cannot be empty")
        }
        
        if (!Patterns.WEB_URL.matcher(url).matches()) {
            return ValidationResult.Invalid("Invalid URL format")
        }
        
        // Ensure HTTPS for security
        if (!url.startsWith("https://", ignoreCase = true)) {
            return ValidationResult.Invalid("URL must use HTTPS")
        }
        
        return ValidationResult.Valid(url.trim())
    }
    
    /**
     * Validates phone number format
     */
    fun validatePhoneNumber(phone: String?): ValidationResult {
        if (phone.isNullOrBlank()) {
            return ValidationResult.Invalid("Phone number cannot be empty")
        }
        
        // Remove common formatting characters
        val cleaned = phone.replace(Regex("[\\s()-]"), "")
        
        if (!Patterns.PHONE.matcher(cleaned).matches()) {
            return ValidationResult.Invalid("Invalid phone number format")
        }
        
        return ValidationResult.Valid(cleaned)
    }
    
    /**
     * Sanitizes general text input
     */
    fun sanitizeText(text: String): String {
        return text
            .trim()
            .replace(Regex("<[^>]*>"), "") // Remove HTML tags
            .replace(Regex("\\s+"), " ") // Normalize whitespace
    }
    
    /**
     * Sanitizes script content (less aggressive than general text)
     */
    fun sanitizeScriptContent(content: String): String {
        return content
            .trim()
            .replace(Regex("\\r\\n"), "\n") // Normalize line endings
            .replace(Regex("\\r"), "\n")
    }
    
    /**
     * Checks if text contains dangerous patterns
     */
    fun containsDangerousPatterns(text: String): Boolean {
        val lowerText = text.lowercase()
        return DANGEROUS_PATTERNS.any { pattern ->
            lowerText.contains(pattern.lowercase())
        }
    }
    
    /**
     * Validates that a string contains only safe characters
     */
    fun isSafeString(text: String): Boolean {
        // Allow alphanumeric, spaces, and common punctuation
        return text.matches(Regex("^[a-zA-Z0-9\\s.,!?;:()\\-_'\"]+$"))
    }
    
    /**
     * Escapes special characters for safe storage
     */
    fun escapeSpecialCharacters(text: String): String {
        return text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("'", "\\'")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
    
    /**
     * Unescapes special characters
     */
    fun unescapeSpecialCharacters(text: String): String {
        return text
            .replace("\\\\", "\\")
            .replace("\\\"", "\"")
            .replace("\\'", "'")
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
    }
    
    /**
     * Validates JSON string format
     */
    fun validateJson(json: String?): ValidationResult {
        if (json.isNullOrBlank()) {
            return ValidationResult.Invalid("JSON cannot be empty")
        }
        
        return try {
            // Basic JSON validation
            kotlinx.serialization.json.Json.parseToJsonElement(json)
            ValidationResult.Valid(json)
        } catch (e: Exception) {
            ValidationResult.Invalid("Invalid JSON format: ${e.message}")
        }
    }
    
    /**
     * Validates timestamp
     */
    fun validateTimestamp(timestamp: Long): ValidationResult {
        if (timestamp < 0) {
            return ValidationResult.Invalid("Timestamp cannot be negative")
        }
        
        // Check if timestamp is reasonable (not too far in the future)
        val maxFutureTime = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000) // 1 year
        if (timestamp > maxFutureTime) {
            return ValidationResult.Invalid("Timestamp is too far in the future")
        }
        
        return ValidationResult.Valid(timestamp.toString())
    }
    
    /**
     * Validates version number
     */
    fun validateVersion(version: Int): ValidationResult {
        if (version < 0) {
            return ValidationResult.Invalid("Version cannot be negative")
        }
        
        return ValidationResult.Valid(version.toString())
    }
}

/**
 * Represents the result of a validation operation
 */
sealed class ValidationResult {
    data class Valid(val sanitizedValue: String) : ValidationResult()
    data class Invalid(val error: String) : ValidationResult()
    
    fun isValid(): Boolean = this is Valid
    fun isInvalid(): Boolean = this is Invalid
    
    fun getValueOrNull(): String? = when (this) {
        is Valid -> sanitizedValue
        is Invalid -> null
    }
    
    fun getErrorOrNull(): String? = when (this) {
        is Valid -> null
        is Invalid -> error
    }
}