package com.abdapps.scriptmine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.abdapps.scriptmine.navigation.ScriptMineNavigation
import com.abdapps.scriptmine.ui.components.SyncStatusIndicator
import com.abdapps.scriptmine.ui.theme.ScriptMineTheme
import com.abdapps.scriptmine.ui.viewmodel.AuthenticationViewModel
import com.abdapps.scriptmine.ui.viewmodel.EditScriptViewModel
import com.abdapps.scriptmine.ui.viewmodel.HistoryViewModel
import com.abdapps.scriptmine.ui.viewmodel.SyncStatusViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity for ScriptMine
 * Integrates all components: Navigation, Authentication, Sync Status
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val editScriptViewModel: EditScriptViewModel by viewModels()
    private val historyViewModel: HistoryViewModel by viewModels()
    private val authViewModel: AuthenticationViewModel by viewModels()
    private val syncStatusViewModel: SyncStatusViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            ScriptMineTheme {
                ScriptMineApp(
                    editScriptViewModel = editScriptViewModel,
                    historyViewModel = historyViewModel,
                    authViewModel = authViewModel,
                    syncStatusViewModel = syncStatusViewModel
                )
            }
        }
    }
}

@Composable
fun ScriptMineApp(
    editScriptViewModel: EditScriptViewModel,
    historyViewModel: HistoryViewModel,
    authViewModel: AuthenticationViewModel,
    syncStatusViewModel: SyncStatusViewModel
) {
    val navController = rememberNavController()
    val syncState by syncStatusViewModel.syncState.collectAsStateWithLifecycle()
    val isAuthenticated = authViewModel.isAuthenticated
    
    Scaffold(
        topBar = {
            ScriptMineTopBar(
                syncState = syncState,
                isAuthenticated = isAuthenticated,
                onSyncClick = { syncStatusViewModel.triggerManualSync() }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            ScriptMineNavigation(
                navController = navController,
                editScriptViewModel = editScriptViewModel,
                historyViewModel = historyViewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScriptMineTopBar(
    syncState: com.abdapps.scriptmine.sync.SyncState,
    isAuthenticated: Boolean,
    onSyncClick: () -> Unit
) {
    TopAppBar(
        title = { Text("ScriptMine") },
        actions = {
            SyncStatusIndicator(
                syncState = syncState,
                showDetails = false
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}
