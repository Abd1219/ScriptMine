package com.abdapps.scriptmine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abdapps.scriptmine.data.model.SavedScript
import com.abdapps.scriptmine.data.model.ScriptTemplate
import com.abdapps.scriptmine.ui.theme.*
import com.abdapps.scriptmine.ui.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onNavigateBack: () -> Unit,
    onEditScript: (Long) -> Unit
) {
    val scripts by viewModel.scripts.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    
    val groupedScripts = remember(scripts) {
        scripts.groupBy { it.templateType }
    }
    
    Scaffold(
        containerColor = FuturisticBackground,
        contentColor = TextPrimary
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FuturisticIconButton(
                onClick = onNavigateBack,
                icon = Icons.Filled.ArrowBack,
                size = 48.dp,
                iconSize = 24.dp,
                glowColor = NeonCyan,
                contentDescription = "Volver"
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = "Historial de Scripts",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextAccent
            )
        }
        
        // Content
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Primary,
                    strokeWidth = 3.dp
                )
            }
        } else if (scripts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                color = SurfaceElevated,
                                shape = androidx.compose.foundation.shape.CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = TextPlaceholder
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "No hay scripts guardados",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                    Text(
                        text = "Crea tu primer script desde las plantillas",
                        fontSize = 14.sp,
                        color = TextPlaceholder,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                groupedScripts.forEach { (templateType, scriptsInGroup) ->
                    item {
                        NeumorphismScriptGroupHeader(templateType = templateType)
                    }
                    
                    items(scriptsInGroup) { script ->
                        NeumorphismScriptItem(
                            script = script,
                            onEdit = { onEditScript(script.id) },
                            onDelete = { viewModel.deleteScript(script) }
                        )
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
        }
    }
}

@Composable
private fun NeumorphismScriptGroupHeader(templateType: String) {
    val template = ScriptTemplate.values().find { it.name == templateType }
    
    FuturisticCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        contentPadding = PaddingValues(16.dp),
        glowColor = NeonPurple
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            template?.let {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = SurfaceElevated,
                            shape = androidx.compose.foundation.shape.CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = it.icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = template?.displayName ?: templateType,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun NeumorphismScriptItem(
    script: SavedScript,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    
    FuturisticCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        contentPadding = PaddingValues(16.dp),
        onClick = onEdit,
        glowColor = NeonCyan
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = script.clientName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateFormat.format(script.updatedAt),
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            
            FuturisticIconButton(
                onClick = { showDeleteDialog = true },
                icon = Icons.Filled.Delete,
                size = 40.dp,
                iconSize = 18.dp,
                glowColor = ErrorColor,
                contentDescription = "Eliminar"
            )
        }
    }
    
    if (showDeleteDialog) {
        NeumorphismAlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = "Eliminar script",
            text = "¿Estás seguro de que quieres eliminar este script?",
            confirmText = "Eliminar",
            dismissText = "Cancelar",
            onConfirm = {
                onDelete()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
private fun NeumorphismAlertDialog(
    onDismissRequest: () -> Unit,
    title: String,
    text: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { 
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            ) 
        },
        text = { 
            Text(
                text = text,
                color = TextSecondary
            ) 
        },
        confirmButton = {
            FuturisticButton(
                onClick = onConfirm,
                isPrimary = true,
                cornerRadius = 12.dp,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = confirmText,
                    color = FuturisticBackground,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            FuturisticButton(
                onClick = onDismiss,
                isPrimary = false,
                cornerRadius = 12.dp,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = dismissText,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        containerColor = FuturisticSurface,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
    )
}