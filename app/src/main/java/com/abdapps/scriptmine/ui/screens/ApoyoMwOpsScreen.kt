package com.abdapps.scriptmine.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abdapps.scriptmine.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApoyoMwOpsScreen(
    onNavigateBack: () -> Unit = {}
) {
    Scaffold(
        containerColor = FuturisticBackground,
        contentColor = TextPrimary,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Apoyo Soporte MW OPS",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextAccent
                    )
                },
                navigationIcon = {
                    FuturisticIconButton(
                        onClick = onNavigateBack,
                        icon = Icons.Filled.ArrowBack,
                        size = 48.dp,
                        iconSize = 24.dp,
                        glowColor = NeonCyan,
                        contentDescription = "Volver"
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FuturisticBackground,
                    titleContentColor = TextAccent,
                    navigationIconContentColor = NeonCyan
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Información del Ticket
            FuturisticCard(
                glowColor = NeonBlue,
                cornerRadius = 16.dp
            ) {
                Text(
                    text = "Información del Ticket",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextAccent,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                ApoyoMwOpsTextField(
                    label = "SD",
                    placeholder = "Ingresa el número SD"
                )
                
                ApoyoMwOpsTextField(
                    label = "CTA",
                    placeholder = "Ingresa el CTA"
                )
                
                ApoyoMwOpsTextField(
                    label = "Cliente",
                    placeholder = "Ingresa el nombre del cliente"
                )
            }
            
            // Información Técnica
            FuturisticCard(
                glowColor = NeonGreen,
                cornerRadius = 16.dp
            ) {
                Text(
                    text = "Información Técnica",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextAccent,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                ApoyoMwOpsTextField(
                    label = "IP HBS",
                    placeholder = "Ingresa la IP HBS"
                )
                
                ApoyoMwOpsTextField(
                    label = "IP HUS",
                    placeholder = "Ingresa la IP HUS"
                )
            }
            
            // Descripción de la Falla
            FuturisticCard(
                glowColor = NeonPurple,
                cornerRadius = 16.dp
            ) {
                Text(
                    text = "Descripción de la Falla",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextAccent,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                ApoyoMwOpsTextArea(
                    label = "Falla Reportada",
                    placeholder = "Describe detalladamente la falla reportada por el cliente"
                )
            }
            
            // Espacio adicional al final
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ApoyoMwOpsTextField(
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var value by remember { mutableStateOf("") }
    
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        FuturisticTextField(
            value = value,
            onValueChange = { value = it },
            placeholder = placeholder,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ApoyoMwOpsTextArea(
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var value by remember { mutableStateOf("") }
    
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        FuturisticTextField(
            value = value,
            onValueChange = { value = it },
            placeholder = placeholder,
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 5
        )
    }
}