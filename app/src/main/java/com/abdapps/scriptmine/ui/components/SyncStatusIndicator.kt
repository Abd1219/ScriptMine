package com.abdapps.scriptmine.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abdapps.scriptmine.data.model.SyncStatus
import com.abdapps.scriptmine.sync.SyncState
import com.abdapps.scriptmine.ui.theme.*
import com.abdapps.scriptmine.utils.ConnectionType
import com.abdapps.scriptmine.utils.NetworkState

/**
 * Displays the current sync status with animated indicators
 */
@Composable
fun SyncStatusIndicator(
    syncState: SyncState,
    pendingCount: Int = 0,
    networkState: NetworkState = NetworkState(),
    modifier: Modifier = Modifier,
    showDetails: Boolean = false
) {
    val (icon, color, text) = getSyncStatusInfo(syncState, pendingCount, networkState)
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Animated sync icon
        AnimatedSyncIcon(
            icon = icon,
            color = color,
            isAnimating = syncState == SyncState.SYNCING
        )
        
        if (showDetails) {
            Column {
                Text(
                    text = text,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = color
                )
                
                if (pendingCount > 0) {
                    Text(
                        text = "$pendingCount pending",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

/**
 * Animated icon that rotates when syncing
 */
@Composable
private fun AnimatedSyncIcon(
    icon: ImageVector,
    color: Color,
    isAnimating: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sync_rotation")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Box(
        modifier = modifier
            .size(24.dp)
            .background(
                color = color.copy(alpha = 0.1f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(16.dp)
                .rotate(if (isAnimating) rotation else 0f),
            tint = color
        )
    }
}

/**
 * Detailed sync status card for settings or debug screens
 */
@Composable
fun SyncStatusCard(
    syncState: SyncState,
    pendingCount: Int,
    networkState: NetworkState,
    onManualSync: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    FuturisticCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        contentPadding = PaddingValues(16.dp),
        glowColor = NeonCyan
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sync Status",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextAccent
                )
                
                SyncStatusIndicator(
                    syncState = syncState,
                    pendingCount = pendingCount,
                    networkState = networkState,
                    showDetails = true
                )
            }
            
            // Network status
            NetworkStatusRow(networkState = networkState)
            
            // Pending items
            if (pendingCount > 0) {
                PendingItemsRow(count = pendingCount)
            }
            
            // Manual sync button
            if (networkState.isConnected && syncState != SyncState.SYNCING) {
                FuturisticCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onManualSync,
                    glowColor = NeonGreen
                ) {
                    Text(
                        text = "Sync Now",
                        modifier = Modifier.padding(12.dp),
                        color = NeonGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Shows network connection status
 */
@Composable
private fun NetworkStatusRow(
    networkState: NetworkState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Network",
            fontSize = 14.sp,
            color = TextSecondary
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            NetworkIcon(connectionType = networkState.connectionType)
            
            Text(
                text = networkState.getDescription(),
                fontSize = 12.sp,
                color = if (networkState.isConnected) NeonGreen else TextSecondary
            )
        }
    }
}

/**
 * Shows pending sync items count
 */
@Composable
private fun PendingItemsRow(
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Pending Sync",
            fontSize = 14.sp,
            color = TextSecondary
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = NeonCyan
            )
            
            Text(
                text = "$count items",
                fontSize = 12.sp,
                color = NeonCyan
            )
        }
    }
}

/**
 * Network connection type icon
 */
@Composable
private fun NetworkIcon(
    connectionType: ConnectionType,
    modifier: Modifier = Modifier
) {
    val icon = when (connectionType) {
        ConnectionType.WIFI -> Icons.Default.Settings
        ConnectionType.MOBILE -> Icons.Default.Phone
        ConnectionType.ETHERNET -> Icons.Default.Settings
        ConnectionType.NONE -> Icons.Default.Close
        ConnectionType.OTHER -> Icons.Default.Settings
    }
    
    val color = when (connectionType) {
        ConnectionType.WIFI -> NeonGreen
        ConnectionType.MOBILE -> NeonBlue
        ConnectionType.ETHERNET -> NeonPurple
        ConnectionType.NONE -> TextSecondary
        ConnectionType.OTHER -> TextSecondary
    }
    
    Icon(
        imageVector = icon,
        contentDescription = connectionType.displayName,
        modifier = modifier.size(16.dp),
        tint = color
    )
}

/**
 * Individual script sync status indicator
 */
@Composable
fun ScriptSyncStatusBadge(
    syncStatus: SyncStatus,
    modifier: Modifier = Modifier
) {
    val (icon, color, text) = when (syncStatus) {
        SyncStatus.SYNCED -> Triple(Icons.Default.Check, NeonGreen, "Synced")
        SyncStatus.PENDING -> Triple(Icons.Default.Info, NeonCyan, "Pending")
        SyncStatus.SYNCING -> Triple(Icons.Default.Refresh, NeonBlue, "Syncing")
        SyncStatus.ERROR -> Triple(Icons.Default.Close, NeonPink, "Error")
        SyncStatus.CONFLICT -> Triple(Icons.Default.Warning, NeonPink, "Conflict")
    }
    
    Row(
        modifier = modifier
            .background(
                color = color.copy(alpha = 0.1f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = color
        )
        
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

/**
 * Gets sync status information for display
 */
private fun getSyncStatusInfo(
    syncState: SyncState,
    pendingCount: Int,
    networkState: NetworkState
): Triple<ImageVector, Color, String> {
    return when {
        syncState == SyncState.SYNCING -> Triple(
            Icons.Default.Refresh,
            NeonBlue,
            "Syncing..."
        )
        
        syncState == SyncState.ERROR -> Triple(
            Icons.Default.Close,
            NeonPink,
            "Sync Error"
        )
        
        !networkState.isConnected -> Triple(
            Icons.Default.Close,
            TextSecondary,
            "Offline"
        )
        
        pendingCount > 0 -> Triple(
            Icons.Default.Info,
            NeonCyan,
            "Pending Sync"
        )
        
        else -> Triple(
            Icons.Default.Check,
            NeonGreen,
            "Up to Date"
        )
    }
}