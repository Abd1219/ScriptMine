package com.abdapps.scriptmine.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.abdapps.scriptmine.auth.AuthState
import com.abdapps.scriptmine.ui.components.AuthenticationSettings
import com.abdapps.scriptmine.ui.theme.*
import com.abdapps.scriptmine.ui.viewmodel.AuthenticationViewModel
import com.abdapps.scriptmine.ui.viewmodel.SettingsViewModel

/**
 * Settings screen with sync, authentication, and app preferences
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    authViewModel: AuthenticationViewModel = hiltViewModel()
) {
    val uiState by settingsViewModel.uiState.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextAccent
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextAccent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FuturisticBackground
                )
            )
        },
        containerColor = FuturisticBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Sync Settings Section
            SettingsSection(title = "Sync Settings") {
                SyncSettingsContent(
                    uiState = uiState,
                    onAutoSyncChanged = { settingsViewModel.setAutoSyncEnabled(it) },
                    onWifiOnlyChanged = { settingsViewModel.setSyncOnWifiOnly(it) },
                    onSyncFrequencyChanged = { settingsViewModel.setSyncFrequency(it) },
                    onManualSync = { settingsViewModel.triggerManualSync() }
                )
            }
            
            // Account Section
            SettingsSection(title = "Account") {
                AuthenticationSettings(authViewModel = authViewModel)
            }
            
            // Notifications Section
            SettingsSection(title = "Notifications") {
                NotificationSettingsContent(
                    notificationsEnabled = uiState.notificationsEnabled,
                    onNotificationsChanged = { settingsViewModel.setNotificationsEnabled(it) }
                )
            }
            
            // Data Management Section
            SettingsSection(title = "Data Management") {
                DataManagementContent(
                    onExportData = { settingsViewModel.exportData() },
                    onClearCache = { settingsViewModel.clearCache() },
                    onResetSync = { settingsViewModel.resetSync() }
                )
            }
            
            // App Info Section
            SettingsSection(title = "About") {
                AppInfoContent()
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextAccent
        )
        
        Card(
            colors = CardDefaults.cardColors(
                containerColor = SurfaceCard
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SyncSettingsContent(
    uiState: com.abdapps.scriptmine.ui.viewmodel.SettingsUiState,
    onAutoSyncChanged: (Boolean) -> Unit,
    onWifiOnlyChanged: (Boolean) -> Unit,
    onSyncFrequencyChanged: (Int) -> Unit,
    onManualSync: () -> Unit
) {
    // Auto Sync Toggle
    SettingToggle(
        title = "Automatic Sync",
        description = "Sync data automatically in the background",
        checked = uiState.autoSyncEnabled,
        onCheckedChange = onAutoSyncChanged,
        icon = Icons.Default.Refresh
    )
    
    Divider(color = TextSecondary.copy(alpha = 0.2f))
    
    // WiFi Only Toggle
    SettingToggle(
        title = "WiFi Only",
        description = "Only sync when connected to WiFi",
        checked = uiState.syncOnWifiOnly,
        onCheckedChange = onWifiOnlyChanged,
        icon = Icons.Default.Settings,
        enabled = uiState.autoSyncEnabled
    )
    
    Divider(color = TextSecondary.copy(alpha = 0.2f))
    
    // Sync Frequency
    SettingItem(
        title = "Sync Frequency",
        description = "${uiState.syncFrequencyMinutes} minutes",
        icon = Icons.Default.Info,
        enabled = uiState.autoSyncEnabled
    ) {
        // Frequency selector
        var showDialog by remember { mutableStateOf(false) }
        
        TextButton(onClick = { showDialog = true }) {
            Text("Change", color = NeonBlue)
        }
        
        if (showDialog) {
            SyncFrequencyDialog(
                currentFrequency = uiState.syncFrequencyMinutes,
                onDismiss = { showDialog = false },
                onConfirm = { frequency ->
                    onSyncFrequencyChanged(frequency)
                    showDialog = false
                }
            )
        }
    }
    
    Divider(color = TextSecondary.copy(alpha = 0.2f))
    
    // Manual Sync Button
    Button(
        onClick = onManualSync,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = NeonBlue.copy(alpha = 0.2f),
            contentColor = NeonBlue
        ),
        enabled = !uiState.isSyncing
    ) {
        if (uiState.isSyncing) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = NeonBlue,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Syncing...")
        } else {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sync Now")
        }
    }
    
    // Last Sync Info
    uiState.lastSyncTime?.let { lastSync ->
        Text(
            text = "Last synced: $lastSync",
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun NotificationSettingsContent(
    notificationsEnabled: Boolean,
    onNotificationsChanged: (Boolean) -> Unit
) {
    SettingToggle(
        title = "Enable Notifications",
        description = "Receive notifications about sync status and updates",
        checked = notificationsEnabled,
        onCheckedChange = onNotificationsChanged,
        icon = Icons.Default.Notifications
    )
}

@Composable
private fun DataManagementContent(
    onExportData: () -> Unit,
    onClearCache: () -> Unit,
    onResetSync: () -> Unit
) {
    // Export Data
    SettingItem(
        title = "Export Data",
        description = "Export all your scripts to a file",
        icon = Icons.Default.Share
    ) {
        TextButton(onClick = onExportData) {
            Text("Export", color = NeonGreen)
        }
    }
    
    Divider(color = TextSecondary.copy(alpha = 0.2f))
    
    // Clear Cache
    SettingItem(
        title = "Clear Cache",
        description = "Free up storage space by clearing cached data",
        icon = Icons.Default.Delete
    ) {
        TextButton(onClick = onClearCache) {
            Text("Clear", color = WarningColor)
        }
    }
    
    Divider(color = TextSecondary.copy(alpha = 0.2f))
    
    // Reset Sync
    SettingItem(
        title = "Reset Sync",
        description = "Reset sync status and force full sync",
        icon = Icons.Default.Refresh
    ) {
        var showDialog by remember { mutableStateOf(false) }
        
        TextButton(onClick = { showDialog = true }) {
            Text("Reset", color = NeonPink)
        }
        
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Reset Sync?") },
                text = { Text("This will reset all sync status and trigger a full sync. Continue?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onResetSync()
                            showDialog = false
                        }
                    ) {
                        Text("Reset", color = NeonPink)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun AppInfoContent() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InfoRow(label = "Version", value = "1.0.0")
        InfoRow(label = "Build", value = "100")
        InfoRow(label = "Developer", value = "Abd Apps")
    }
}

@Composable
private fun SettingToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) NeonCyan else TextSecondary,
                modifier = Modifier.size(24.dp)
            )
            
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (enabled) TextAccent else TextSecondary
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NeonGreen,
                checkedTrackColor = NeonGreen.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
private fun SettingItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean = true,
    action: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) NeonCyan else TextSecondary,
                modifier = Modifier.size(24.dp)
            )
            
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (enabled) TextAccent else TextSecondary
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
        
        action()
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = TextSecondary
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextAccent
        )
    }
}

@Composable
private fun SyncFrequencyDialog(
    currentFrequency: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val frequencies = listOf(5, 15, 30, 60, 120, 240)
    var selectedFrequency by remember { mutableStateOf(currentFrequency) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sync Frequency") },
        text = {
            Column {
                frequencies.forEach { frequency ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedFrequency == frequency,
                            onClick = { selectedFrequency = frequency }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when {
                                frequency < 60 -> "$frequency minutes"
                                frequency == 60 -> "1 hour"
                                else -> "${frequency / 60} hours"
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedFrequency) }) {
                Text("OK", color = NeonBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}