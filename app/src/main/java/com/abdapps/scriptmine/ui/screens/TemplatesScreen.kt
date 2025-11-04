package com.abdapps.scriptmine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abdapps.scriptmine.data.model.ScriptTemplate
import com.abdapps.scriptmine.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    onTemplateSelected: (ScriptTemplate) -> Unit,
    onNavigateToHistory: () -> Unit
) {
    Scaffold(
        containerColor = FuturisticBackground,
        contentColor = TextPrimary
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
        ) {
        // Header with title and history button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ScriptMine",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextAccent
            )
            
            FuturisticIconButton(
                onClick = onNavigateToHistory,
                icon = Icons.Filled.List,
                size = 52.dp,
                iconSize = 28.dp,
                glowColor = NeonPurple,
                contentDescription = "Historial"
            )
        }
        
        // Subtitle
        Text(
            text = "Selecciona una plantilla para comenzar",
            fontSize = 16.sp,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Templates grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(ScriptTemplate.values()) { template ->
                FuturisticTemplateCard(
                    template = template,
                    onClick = { onTemplateSelected(template) }
                )
            }
        }
        }
    }
}

@Composable
private fun FuturisticTemplateCard(
    template: ScriptTemplate,
    onClick: () -> Unit
) {
    val cardColors = listOf(NeonCyan, NeonPurple, NeonGreen, NeonBlue, NeonPink)
    val glowColor = cardColors[template.ordinal % cardColors.size]
    
    FuturisticCard(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        cornerRadius = 20.dp,
        contentPadding = PaddingValues(20.dp),
        onClick = onClick,
        glowColor = glowColor
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Icon container with neon glow
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(
                                glowColor.copy(alpha = 0.2f),
                                androidx.compose.ui.graphics.Color.Transparent
                            )
                        ),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = glowColor.copy(alpha = 0.5f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = template.icon,
                    contentDescription = template.displayName,
                    modifier = Modifier.size(32.dp),
                    tint = glowColor
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = template.displayName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = TextPrimary,
                lineHeight = 18.sp
            )
        }
    }
}