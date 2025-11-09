package com.abdapps.scriptmine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.abdapps.scriptmine.navigation.ScriptMineNavigation
import com.abdapps.scriptmine.ui.theme.ScriptMineTheme
import com.abdapps.scriptmine.ui.viewmodel.EditScriptViewModel
import com.abdapps.scriptmine.ui.viewmodel.HistoryViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity for ScriptMine
 * Simplified version without authentication for now
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val editScriptViewModel: EditScriptViewModel by viewModels()
    private val historyViewModel: HistoryViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            ScriptMineTheme {
                ScriptMineApp(
                    editScriptViewModel = editScriptViewModel,
                    historyViewModel = historyViewModel
                )
            }
        }
    }
}

@Composable
fun ScriptMineApp(
    editScriptViewModel: EditScriptViewModel,
    historyViewModel: HistoryViewModel
) {
    val navController = rememberNavController()
    
    Scaffold(
        topBar = {
            ScriptMineTopBar()
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
fun ScriptMineTopBar() {
    TopAppBar(
        title = { Text("ScriptMine") },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}
