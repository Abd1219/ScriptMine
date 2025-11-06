package com.abdapps.scriptmine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.abdapps.scriptmine.navigation.ScriptMineNavigation
import com.abdapps.scriptmine.ui.theme.ScriptMineTheme
import com.abdapps.scriptmine.ui.viewmodel.EditScriptViewModel
import com.abdapps.scriptmine.ui.viewmodel.HistoryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            ScriptMineTheme {
                val navController = rememberNavController()
                val editScriptViewModel: EditScriptViewModel = hiltViewModel()
                val historyViewModel: HistoryViewModel = hiltViewModel()
                
                ScriptMineNavigation(
                    navController = navController,
                    editScriptViewModel = editScriptViewModel,
                    historyViewModel = historyViewModel
                )
            }
        }
    }
}