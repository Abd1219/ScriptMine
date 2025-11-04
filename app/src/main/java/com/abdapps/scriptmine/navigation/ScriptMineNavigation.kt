package com.abdapps.scriptmine.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.abdapps.scriptmine.data.model.ScriptTemplate
import com.abdapps.scriptmine.ui.screens.EditScriptScreen
import com.abdapps.scriptmine.ui.screens.HistoryScreen
import com.abdapps.scriptmine.ui.screens.TemplatesScreen
import com.abdapps.scriptmine.ui.viewmodel.EditScriptViewModel
import com.abdapps.scriptmine.ui.viewmodel.HistoryViewModel

sealed class Screen(val route: String) {
    object Templates : Screen("templates")
    object EditScript : Screen("edit_script/{templateName}") {
        fun createRoute(templateName: String) = "edit_script/$templateName"
    }
    object EditExistingScript : Screen("edit_script/{templateName}/{scriptId}") {
        fun createRoute(templateName: String, scriptId: Long) = "edit_script/$templateName/$scriptId"
    }
    object History : Screen("history")
}

@Composable
fun ScriptMineNavigation(
    navController: NavHostController = rememberNavController(),
    editScriptViewModel: EditScriptViewModel,
    historyViewModel: HistoryViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Templates.route
    ) {
        composable(Screen.Templates.route) {
            TemplatesScreen(
                onTemplateSelected = { template ->
                    editScriptViewModel.setTemplate(template)
                    navController.navigate(Screen.EditScript.createRoute(template.name))
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                }
            )
        }
        
        composable(Screen.EditScript.route) { backStackEntry ->
            val templateName = backStackEntry.arguments?.getString("templateName")
            templateName?.let { name ->
                val template = ScriptTemplate.values().find { it.name == name }
                template?.let {
                    editScriptViewModel.setTemplate(it)
                }
            }
            
            EditScriptScreen(
                viewModel = editScriptViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.EditExistingScript.route) { backStackEntry ->
            val templateName = backStackEntry.arguments?.getString("templateName")
            val scriptIdString = backStackEntry.arguments?.getString("scriptId")
            
            scriptIdString?.toLongOrNull()?.let { scriptId ->
                editScriptViewModel.loadScript(scriptId)
            }
            
            EditScriptScreen(
                viewModel = editScriptViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.History.route) {
            HistoryScreen(
                viewModel = historyViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEditScript = { scriptId ->
                    // We need to get the template name from the script
                    // For now, we'll navigate to a generic edit route
                    navController.navigate("edit_existing_script/$scriptId")
                }
            )
        }
        
        composable("edit_existing_script/{scriptId}") { backStackEntry ->
            val scriptIdString = backStackEntry.arguments?.getString("scriptId")
            
            scriptIdString?.toLongOrNull()?.let { scriptId ->
                editScriptViewModel.loadScript(scriptId)
            }
            
            EditScriptScreen(
                viewModel = editScriptViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}