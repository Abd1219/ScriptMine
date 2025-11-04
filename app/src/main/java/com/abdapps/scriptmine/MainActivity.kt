package com.abdapps.scriptmine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.abdapps.scriptmine.navigation.ScriptMineNavigation
import com.abdapps.scriptmine.ui.theme.ScriptMineTheme
import com.abdapps.scriptmine.ui.viewmodel.EditScriptViewModel
import com.abdapps.scriptmine.ui.viewmodel.HistoryViewModel
import com.abdapps.scriptmine.ui.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val application = application as ScriptMineApplication
        val viewModelFactory = ViewModelFactory(application.repository)
        
        setContent {
            ScriptMineTheme {
                val navController = rememberNavController()
                val editScriptViewModel: EditScriptViewModel = viewModel(factory = viewModelFactory)
                val historyViewModel: HistoryViewModel = viewModel(factory = viewModelFactory)
                
                ScriptMineNavigation(
                    navController = navController,
                    editScriptViewModel = editScriptViewModel,
                    historyViewModel = historyViewModel
                )
            }
        }
    }
}