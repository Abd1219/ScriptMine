package com.abdapps.scriptmine.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.abdapps.scriptmine.auth.AuthState
import com.abdapps.scriptmine.ui.components.AuthenticationScreen
import com.abdapps.scriptmine.ui.viewmodel.AuthenticationViewModel

/**
 * Authentication screen wrapper for navigation
 */
@Composable
fun AuthenticationScreenWrapper(
    onNavigateToMain: () -> Unit,
    authViewModel: AuthenticationViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    
    // Navigate to main screen if already authenticated
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onNavigateToMain()
        }
    }
    
    AuthenticationScreen(
        authViewModel = authViewModel,
        onAuthenticationComplete = onNavigateToMain
    )
}