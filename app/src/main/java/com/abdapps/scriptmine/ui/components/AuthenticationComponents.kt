package com.abdapps.scriptmine.ui.components

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.abdapps.scriptmine.R
import com.abdapps.scriptmine.auth.AuthState
import com.abdapps.scriptmine.ui.theme.*
import com.abdapps.scriptmine.ui.viewmodel.AuthenticationViewModel
import com.abdapps.scriptmine.ui.viewmodel.UserDisplayInfo

/**
 * Google Sign-In button with futuristic design
 */
@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = NeonBlue.copy(alpha = 0.2f),
            contentColor = NeonBlue
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = NeonBlue,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = NeonBlue
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = if (isLoading) "Signing in..." else stringResource(R.string.sign_in_with_google),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextAccent
            )
        }
    }
}

/**
 * User profile card showing authenticated user info
 */
@Composable
fun UserProfileCard(
    userInfo: UserDisplayInfo,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceCard
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User avatar
            UserAvatar(
                photoUrl = userInfo.photoUrl,
                initials = userInfo.initials,
                size = 56.dp
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // User info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = userInfo.displayName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextAccent
                )
                
                Text(
                    text = userInfo.email,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
            
            // Sign out button
            IconButton(
                onClick = onSignOut
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = stringResource(R.string.sign_out),
                    tint = NeonPink
                )
            }
        }
    }
}

/**
 * User avatar with fallback to initials
 */
@Composable
fun UserAvatar(
    photoUrl: String?,
    initials: String,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(NeonCyan, NeonBlue)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUrl.isNullOrEmpty()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "User avatar",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = initials,
                fontSize = (size.value * 0.4f).sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Authentication status indicator
 */
@Composable
fun AuthStatusIndicator(
    authState: AuthState,
    modifier: Modifier = Modifier
) {
    val (icon, color, text) = when (authState) {
        is AuthState.Authenticated -> Triple(
            Icons.Default.CheckCircle,
            NeonGreen,
            "Signed in as ${authState.user.displayName}"
        )
        is AuthState.Unauthenticated -> Triple(
            Icons.Default.Info,
            WarningColor,
            "Working offline"
        )
    }
    
    Row(
        modifier = modifier
            .background(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = color
        )
        
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

/**
 * Sign-in prompt card for unauthenticated users
 */
@Composable
fun SignInPromptCard(
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceCard
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = NeonCyan
            )
            
            Text(
                text = stringResource(R.string.authentication_required),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextAccent,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = stringResource(R.string.sign_in_to_sync),
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            
            GoogleSignInButton(
                onClick = onSignIn,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Complete authentication screen
 */
@Composable
fun AuthenticationScreen(
    authViewModel: AuthenticationViewModel = hiltViewModel(),
    onAuthenticationComplete: () -> Unit = {}
) {
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()
    val uiState by authViewModel.uiState.collectAsState()
    
    // Google Sign-In launcher
    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            authViewModel.handleGoogleSignInResult(result.data)
        }
    }
    
    // Handle authentication success
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onAuthenticationComplete()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App logo/title
        Text(
            text = "ScriptMine",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = TextAccent
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Sync your scripts across devices",
            fontSize = 16.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        when (authState) {
            is AuthState.Unauthenticated -> {
                SignInPromptCard(
                    onSignIn = {
                        val signInIntent = authViewModel.startGoogleSignIn()
                        signInLauncher.launch(signInIntent)
                    }
                )
            }
            
            is AuthState.Authenticated -> {
                val userInfo = authViewModel.getUserDisplayInfo()
                if (userInfo != null) {
                    UserProfileCard(
                        userInfo = userInfo,
                        onSignOut = { authViewModel.signOut() }
                    )
                }
            }
        }
        
        // Error message
        uiState.error?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = NeonPink.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = NeonPink,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Continue offline option
        TextButton(
            onClick = onAuthenticationComplete
        ) {
            Text(
                text = "Continue offline",
                color = TextSecondary,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * Compact authentication widget for main screens
 */
@Composable
fun AuthenticationWidget(
    authViewModel: AuthenticationViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()
    val uiState by authViewModel.uiState.collectAsState()
    
    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            authViewModel.handleGoogleSignInResult(result.data)
        }
    }
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (authState) {
            is AuthState.Authenticated -> {
                val userInfo = authViewModel.getUserDisplayInfo()
                if (userInfo != null) {
                    UserAvatar(
                        photoUrl = userInfo.photoUrl,
                        initials = userInfo.initials,
                        size = 32.dp
                    )
                    
                    Text(
                        text = userInfo.displayName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextAccent
                    )
                }
            }
            
            is AuthState.Unauthenticated -> {
                IconButton(
                    onClick = {
                        val signInIntent = authViewModel.startGoogleSignIn()
                        signInLauncher.launch(signInIntent)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Sign in",
                        tint = NeonBlue
                    )
                }
                
                Text(
                    text = "Sign in",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

/**
 * Authentication settings section
 */
@Composable
fun AuthenticationSettings(
    authViewModel: AuthenticationViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val authState by authViewModel.authState.collectAsState()
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Account",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextAccent
        )
        
        when (authState) {
            is AuthState.Authenticated -> {
                val userInfo = authViewModel.getUserDisplayInfo()
                if (userInfo != null) {
                    UserProfileCard(
                        userInfo = userInfo,
                        onSignOut = { authViewModel.signOut() }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Additional authenticated user options
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = SurfaceCard
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = NeonPink,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Delete Account",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextAccent
                            )
                            
                            Text(
                                text = "Permanently delete your account and all data",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                        
                        IconButton(
                            onClick = { authViewModel.deleteAccount() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Delete account",
                                tint = NeonPink
                            )
                        }
                    }
                }
            }
            
            is AuthState.Unauthenticated -> {
                SignInPromptCard(
                    onSignIn = {
                        // This would need to be handled by the parent composable
                        // since we need the activity result launcher
                    }
                )
            }
        }
    }
}