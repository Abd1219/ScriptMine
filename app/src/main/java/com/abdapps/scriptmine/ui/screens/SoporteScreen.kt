package com.abdapps.scriptmine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abdapps.scriptmine.ui.theme.*
import com.abdapps.scriptmine.ui.viewmodel.SoporteViewModel
import com.abdapps.scriptmine.utils.ClipboardHelper
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoporteScreen(
    viewModel: SoporteViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    
    // Collect all StateFlows from ViewModel
    val horaInicio by viewModel.horaInicio.collectAsStateWithLifecycle()
    val horaTermino by viewModel.horaTermino.collectAsStateWithLifecycle()
    val tiempoEspera by viewModel.tiempoEspera.collectAsStateWithLifecycle()
    val actividadesSoporte by viewModel.actividadesSoporte.collectAsStateWithLifecycle()
    val observacionesSoporte by viewModel.observacionesSoporte.collectAsStateWithLifecycle()
    val scriptPreview by viewModel.scriptPreview.collectAsStateWithLifecycle()
    val normalizedScriptPreview by viewModel.normalizedScriptPreview.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val saveSuccess by viewModel.saveSuccess.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = FuturisticBackground,
        contentColor = TextPrimary,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Script de Soporte",
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
        ) {
            // Form content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Información de Horarios
                FuturisticCard(
                    glowColor = NeonBlue,
                    cornerRadius = 16.dp
                ) {
                    Text(
                        text = "Información de Horarios",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextAccent,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    SoporteTextField(
                        label = "Hora de inicio",
                        value = horaInicio,
                        onValueChange = viewModel::updateHoraInicio,
                        placeholder = "Ej: 08:30 AM"
                    )
                    
                    SoporteTextField(
                        label = "Hora de Termino",
                        value = horaTermino,
                        onValueChange = viewModel::updateHoraTermino,
                        placeholder = "Ej: 12:45 PM"
                    )
                    
                    SoporteTextField(
                        label = "Tiempo de espera para accesos",
                        value = tiempoEspera,
                        onValueChange = viewModel::updateTiempoEspera,
                        placeholder = "Ej: 30 minutos"
                    )
                }
                
                // Actividades y Observaciones
                FuturisticCard(
                    glowColor = NeonGreen,
                    cornerRadius = 16.dp
                ) {
                    Text(
                        text = "Actividades y Observaciones",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextAccent,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    SoporteTextArea(
                        label = "Actividades realizadas en sitio",
                        value = actividadesSoporte,
                        onValueChange = viewModel::updateActividadesSoporte,
                        placeholder = "Describe las actividades realizadas durante el soporte técnico"
                    )
                    
                    SoporteTextArea(
                        label = "Observaciones y contratiempos durante la actividad",
                        value = observacionesSoporte,
                        onValueChange = viewModel::updateObservacionesSoporte,
                        placeholder = "Describe observaciones, problemas encontrados o contratiempos"
                    )
                }
                
                // Vista Previa del Script
                FuturisticCard(
                    glowColor = NeonPurple,
                    cornerRadius = 16.dp
                ) {
                    Text(
                        text = "Vista Previa del Script",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextAccent,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(
                                color = SurfaceInput,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                    ) {
                        val scrollState = rememberScrollState()
                        
                        Text(
                            text = scriptPreview.ifEmpty { "La vista previa aparecerá aquí mientras completas el formulario..." },
                            fontSize = 14.sp,
                            color = if (scriptPreview.isEmpty()) TextPlaceholder else TextPrimary,
                            lineHeight = 20.sp,
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                        )
                        
                        // Scroll indicator
                        if (scriptPreview.isNotEmpty() && scrollState.maxValue > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .width(4.dp)
                                    .height(60.dp)
                                    .background(
                                        color = NeonPurple.copy(alpha = 0.5f),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                    }
                }
                
                // Vista Previa Normalizada
                FuturisticCard(
                    glowColor = NeonCyan,
                    cornerRadius = 16.dp
                ) {
                    Text(
                        text = "Vista Previa Normalizada",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextAccent,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Text(
                        text = "Sin acentos, puntuación, saltos de línea y en minúsculas",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(
                                color = SurfaceInput,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                    ) {
                        val scrollState2 = rememberScrollState()
                        
                        Text(
                            text = normalizedScriptPreview.ifEmpty { "La vista previa normalizada aparecerá aquí..." },
                            fontSize = 14.sp,
                            color = if (normalizedScriptPreview.isEmpty()) TextPlaceholder else TextPrimary,
                            lineHeight = 20.sp,
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState2)
                        )
                        
                        // Scroll indicator
                        if (normalizedScriptPreview.isNotEmpty() && scrollState2.maxValue > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .width(4.dp)
                                    .height(60.dp)
                                    .background(
                                        color = NeonCyan.copy(alpha = 0.5f),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                    }
                }
                
                // Espacio adicional para los botones
                Spacer(modifier = Modifier.height(80.dp))
            }
            
            // Bottom action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FuturisticBackground)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Save button with loading states
                FuturisticButton(
                    onClick = { 
                        if (!isSaving) {
                            viewModel.onSaveScript() 
                        }
                    },
                    modifier = Modifier.weight(1f),
                    isPrimary = true,
                    cornerRadius = 12.dp,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    when {
                        isSaving -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = FuturisticBackground,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Guardando...",
                                color = FuturisticBackground,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                        saveSuccess -> {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = FuturisticBackground
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "¡Guardado!",
                                color = FuturisticBackground,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = FuturisticBackground
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Guardar",
                                color = FuturisticBackground,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                
                // Copy original button with feedback
                var copied by remember { mutableStateOf(false) }
                
                LaunchedEffect(copied) {
                    if (copied) {
                        delay(1500)
                        copied = false
                    }
                }
                
                FuturisticButton(
                    onClick = { 
                        ClipboardHelper.copyToClipboard(context, scriptPreview)
                        copied = true
                    },
                    modifier = Modifier.weight(1f),
                    isPrimary = false,
                    cornerRadius = 12.dp,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    if (copied) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = NeonGreen
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "¡Copiado!",
                            color = NeonGreen,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = NeonPurple
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Copiar",
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }
                
                // Copy normalized button with feedback
                var copiedNormalized by remember { mutableStateOf(false) }
                
                LaunchedEffect(copiedNormalized) {
                    if (copiedNormalized) {
                        delay(1500)
                        copiedNormalized = false
                    }
                }
                
                FuturisticButton(
                    onClick = { 
                        ClipboardHelper.copyToClipboard(context, normalizedScriptPreview)
                        copiedNormalized = true
                    },
                    modifier = Modifier.weight(1f),
                    isPrimary = false,
                    cornerRadius = 12.dp,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    if (copiedNormalized) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = NeonGreen
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "¡Copiado!",
                            color = NeonGreen,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = NeonCyan
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Normalizado",
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }
                
                // Clear button
                FuturisticIconButton(
                    onClick = { viewModel.onClearForm() },
                    icon = Icons.Filled.Clear,
                    size = 40.dp,
                    iconSize = 18.dp,
                    glowColor = ErrorColor,
                    contentDescription = "Limpiar formulario"
                )
            }
        }
    }
}

@Composable
private fun SoporteTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
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
            onValueChange = onValueChange,
            placeholder = placeholder,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SoporteTextArea(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
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
            onValueChange = onValueChange,
            placeholder = placeholder,
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 4
        )
    }
}