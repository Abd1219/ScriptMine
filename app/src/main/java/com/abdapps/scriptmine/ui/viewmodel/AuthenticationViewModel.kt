package com.abdapps.scriptmine.ui.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdapps.scriptmine.auth.AuthState
import com.abdapps.scriptmine.auth.AuthUser
import com.abdapps.scriptmine.auth.AuthenticationManager
import com.abdapps.scriptmine.auth.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing authentication state and operations
 */
@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val authManager: AuthenticationManager
) : ViewModel() {
    
    // Authentication state from AuthenticationManager
    val authState: StateFlow<AuthState> = authManager.observeAuthState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = if (authManager.isAuthenticated) {
                AuthState.Authenticated(
                    AuthUser(
                        uid = authManager.currentUserId ?: "",
                        email = authManager.currentUser?.email ?: "",
                        displayName = authManager.currentUser?.displayName ?: "",
                        photoUrl = authManager.currentUser?.photoUrl?.toString()
                    )
                )
            } else {
                AuthState.Unauthenticated
            }
        )
    
    // UI state for authentication operations
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    // Current user convenience property
    val currentUser: AuthUser?
        get() = (authState.value as? AuthState.Authenticated)?.user
    
    val isAuthenticated: Boolean
        get() = authState.value is AuthState.Authenticated
    
    /**
     * Initiates Google Sign-In process
     */
    fun startGoogleSignIn(): Intent {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        return authManager.getGoogleSignInIntent()
    }
    
    /**
     * Handles Google Sign-In result
     */
    fun handleGoogleSignInResult(data: Intent?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = authManager.handleGoogleSignInResult(data)
            
            when (result) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null,
                        signInSuccess = true
                    )
                    
                    // Clear success flag after a delay
                    kotlinx.coroutines.delay(2000)
                    _uiState.value = _uiState.value.copy(signInSuccess = false)
                }
                
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }
    
    /**
     * Signs out the current user
     */
    fun signOut() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = authManager.signOut()
            
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Sign out failed: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }
    
    /**
     * Deletes the current user account
     */
    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = authManager.deleteAccount()
            
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Account deletion failed: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }
    
    /**
     * Re-authenticates the current user
     */
    fun reauthenticate() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = authManager.reauthenticate()
            
            when (result) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                }
                
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }
    
    /**
     * Checks if the current session is valid
     */
    fun validateSession() {
        viewModelScope.launch {
            val isValid = authManager.isSessionValid()
            
            if (!isValid && isAuthenticated) {
                _uiState.value = _uiState.value.copy(
                    error = "Session expired. Please sign in again."
                )
            }
        }
    }
    
    /**
     * Clears the current error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Gets user display information
     */
    fun getUserDisplayInfo(): UserDisplayInfo? {
        return currentUser?.let { user ->
            UserDisplayInfo(
                displayName = user.displayName.ifEmpty { user.email.substringBefore("@") },
                email = user.email,
                photoUrl = user.photoUrl,
                initials = getInitials(user.displayName.ifEmpty { user.email })
            )
        }
    }
    
    /**
     * Gets initials from a name or email
     */
    private fun getInitials(name: String): String {
        return name.split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .ifEmpty { name.take(2).uppercase() }
    }
    
    /**
     * Checks if authentication is required for sync
     */
    fun isAuthRequiredForSync(): Boolean {
        // For now, sync works without auth (offline-first)
        // But some features might require authentication
        return false
    }
    
    /**
     * Gets authentication status for sync operations
     */
    fun getAuthStatusForSync(): AuthSyncStatus {
        return when (authState.value) {
            is AuthState.Authenticated -> AuthSyncStatus.AUTHENTICATED
            is AuthState.Unauthenticated -> {
                if (isAuthRequiredForSync()) {
                    AuthSyncStatus.REQUIRED
                } else {
                    AuthSyncStatus.OPTIONAL
                }
            }
        }
    }
}

/**
 * UI state for authentication operations
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val signInSuccess: Boolean = false
)

/**
 * User display information for UI
 */
data class UserDisplayInfo(
    val displayName: String,
    val email: String,
    val photoUrl: String? = null,
    val initials: String
)

/**
 * Authentication status for sync operations
 */
enum class AuthSyncStatus {
    AUTHENTICATED,    // User is signed in
    REQUIRED,        // Authentication is required for sync
    OPTIONAL         // Authentication is optional, can work offline
}