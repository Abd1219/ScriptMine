package com.abdapps.scriptmine.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abdapps.scriptmine.data.model.ScriptField
import com.abdapps.scriptmine.data.model.ScriptTemplate
import com.abdapps.scriptmine.ui.theme.*
import com.abdapps.scriptmine.ui.viewmodel.EditScriptViewModel
import com.abdapps.scriptmine.utils.ClipboardHelper
import com.abdapps.scriptmine.utils.LocationHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScriptScreen(
    viewModel: EditScriptViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locationHelper = remember { LocationHelper(context) }
    
    val formData by viewModel.formData.collectAsStateWithLifecycle()
    val generatedScript by viewModel.generatedScript.collectAsStateWithLifecycle()
    val normalizedScript by viewModel.normalizedScript.collectAsStateWithLifecycle()
    val currentTemplate by viewModel.currentTemplate.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val saveSuccess by viewModel.saveSuccess.collectAsStateWithLifecycle()
    
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            // Permission granted, get location will be handled in the click handler
        }
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
                text = currentTemplate?.displayName ?: "Editar Script",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextAccent
            )
        }
        
        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            currentTemplate?.let { template ->
                // Form fields
                template.fields.forEach { field ->
                    when (field) {
                        is ScriptField.TEXT -> {
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Text(
                                    text = field.label + if (field.required) " *" else "",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                FuturisticTextField(
                                    value = formData[field.key] ?: field.defaultValue,
                                    onValueChange = { viewModel.updateField(field.key, it) },
                                    placeholder = if (field.defaultValue.isNotEmpty()) field.defaultValue else "Ingresa ${field.label.lowercase()}",
                                    modifier = Modifier.fillMaxWidth(),
                                    isError = field.required && (formData[field.key]?.isEmpty() != false && field.defaultValue.isEmpty())
                                )
                            }
                        }
                        
                        is ScriptField.TEXTAREA -> {
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Text(
                                    text = field.label + if (field.required) " *" else "",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                FuturisticTextField(
                                    value = formData[field.key] ?: "",
                                    onValueChange = { viewModel.updateField(field.key, it) },
                                    placeholder = "Ingresa ${field.label.lowercase()}",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 120.dp, max = 200.dp),
                                    singleLine = false,
                                    maxLines = Int.MAX_VALUE,
                                    isError = field.required && (formData[field.key]?.isEmpty() != false)
                                )
                            }
                        }
                        
                        is ScriptField.DROPDOWN -> {
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Text(
                                    text = field.label + if (field.required) " *" else "",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                FuturisticDropdown(
                                    selectedValue = formData[field.key] ?: "",
                                    options = field.options,
                                    onValueSelected = { viewModel.updateField(field.key, it) },
                                    placeholder = "Selecciona ${field.label.lowercase()}",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        
                        is ScriptField.COORDINATES -> {
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Text(
                                    text = field.label + if (field.required) " *" else "",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FuturisticTextField(
                                        value = formData[field.key] ?: "",
                                        onValueChange = { viewModel.updateField(field.key, it) },
                                        placeholder = "Coordenadas GPS",
                                        modifier = Modifier.weight(1f),
                                        isError = field.required && (formData[field.key]?.isEmpty() != false)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    FuturisticIconButton(
                                        onClick = {
                                            if (locationHelper.hasLocationPermission()) {
                                                scope.launch {
                                                    val location = locationHelper.getCurrentLocation()
                                                    location?.let {
                                                        viewModel.updateField(field.key, it)
                                                    }
                                                }
                                            } else {
                                                locationPermissionLauncher.launch(
                                                    arrayOf(
                                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                                    )
                                                )
                                            }
                                        },
                                        icon = Icons.Filled.LocationOn,
                                        size = 48.dp,
                                        iconSize = 24.dp,
                                        glowColor = NeonGreen,
                                        contentDescription = "Obtener ubicación"
                                    )
                                }
                            }
                        }
                        
                        is ScriptField.TIME -> {
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Text(
                                    text = field.label + if (field.required) " *" else "",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                FuturisticTimeField(
                                    value = formData[field.key] ?: "",
                                    onValueChange = { viewModel.updateField(field.key, it) },
                                    label = field.label,
                                    modifier = Modifier.fillMaxWidth(),
                                    isError = field.required && (formData[field.key]?.isEmpty() != false)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Preview section
                Text(
                    text = "Vista Previa",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                FuturisticCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    contentPadding = PaddingValues(0.dp),
                    glowColor = NeonBlue
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(16.dp)
                    ) {
                        val scrollState = rememberScrollState()
                        
                        Text(
                            text = generatedScript.ifEmpty { "El script aparecerá aquí mientras completas el formulario..." },
                            fontSize = 14.sp,
                            color = if (generatedScript.isEmpty()) TextPlaceholder else TextPrimary,
                            lineHeight = 20.sp,
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                        )
                        
                        // Scroll indicator
                        if (generatedScript.isNotEmpty() && scrollState.maxValue > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .width(4.dp)
                                    .height(60.dp)
                                    .background(
                                        color = NeonBlue.copy(alpha = 0.5f),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                    }
                }
                
                // Normalized preview section (only for SOPORTE template)
                if (currentTemplate == ScriptTemplate.SOPORTE && normalizedScript.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Vista Previa Normalizada",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = "Sin acentos, puntuación, saltos de línea y en minúsculas",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    FuturisticCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp,
                        contentPadding = PaddingValues(0.dp),
                        glowColor = NeonCyan
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .padding(16.dp)
                        ) {
                            val scrollState2 = rememberScrollState()
                            
                            Text(
                                text = normalizedScript,
                                fontSize = 14.sp,
                                color = TextPrimary,
                                lineHeight = 20.sp,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState2)
                            )
                            
                            // Scroll indicator
                            if (normalizedScript.isNotEmpty() && scrollState2.maxValue > 0) {
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
                    
                    // Copy normalized button directly below the normalized preview
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    var copiedNormalized by remember { mutableStateOf(false) }
                    
                    LaunchedEffect(copiedNormalized) {
                        if (copiedNormalized) {
                            kotlinx.coroutines.delay(1500)
                            copiedNormalized = false
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        FuturisticButton(
                            onClick = { 
                                ClipboardHelper.copyToClipboard(context, normalizedScript)
                                copiedNormalized = true
                            },
                            isPrimary = false,
                            cornerRadius = 12.dp,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            if (copiedNormalized) {
                                Icon(
                                    imageVector = Icons.Filled.Done,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = NeonGreen
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "¡Copiado!",
                                    color = NeonGreen,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = NeonCyan
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Copiar Normalizado",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(80.dp)) // Space for bottom buttons
            }
        }
        
        // Bottom action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(FuturisticBackground)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FuturisticButton(
                onClick = { 
                    if (!isSaving) {
                        viewModel.saveScript() 
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
            
            // Copy button with state
            var copied by remember { mutableStateOf(false) }
            
            LaunchedEffect(copied) {
                if (copied) {
                    kotlinx.coroutines.delay(1500)
                    copied = false
                }
            }
            
            FuturisticButton(
                onClick = { 
                    ClipboardHelper.copyToClipboard(context, generatedScript)
                    copied = true
                },
                modifier = Modifier.weight(1f),
                isPrimary = false,
                cornerRadius = 12.dp,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            ) {
                if (copied) {
                    Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = NeonGreen
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "¡Copiado!",
                        color = NeonGreen,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = NeonPurple
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Copiar",
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
            
            FuturisticIconButton(
                onClick = { viewModel.clearForm() },
                icon = Icons.Filled.Clear,
                size = 40.dp,
                iconSize = 18.dp,
                glowColor = ErrorColor,
                contentDescription = "Limpiar"
            )
        }
        }
    }
}

@Composable
private fun FuturisticDropdown(
    selectedValue: String,
    options: List<String>,
    onValueSelected: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        // Campo de texto clickeable que actúa como trigger del dropdown
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .futuristicInput(
                    glowColor = if (expanded) Primary else BorderGlow.copy(alpha = 0.3f),
                    isFocused = expanded
                )
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = selectedValue.ifEmpty { placeholder },
                color = if (selectedValue.isEmpty()) TextPlaceholder else TextPrimary,
                fontSize = 16.sp,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
        
        // Dropdown menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(
                    color = FuturisticSurface,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                )
                .border(
                    width = 1.dp,
                    color = BorderGlow.copy(alpha = 0.5f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                )
                .padding(4.dp)
                .widthIn(max = 400.dp) // Limitar el ancho máximo
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = option,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            maxLines = 3,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            lineHeight = 16.sp
                        ) 
                    },
                    onClick = {
                        onValueSelected(option)
                        expanded = false
                    },
                    modifier = Modifier
                        .background(
                            color = if (option == selectedValue) SurfaceElevated else androidx.compose.ui.graphics.Color.Transparent,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun FuturisticTimeField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    var showTimeDialog by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableStateOf(8) }
    var selectedMinute by remember { mutableStateOf(0) }
    
    // Parse existing value
    LaunchedEffect(value) {
        if (value.isNotEmpty()) {
            try {
                val parts = value.split(":")
                if (parts.size == 2) {
                    selectedHour = parts[0].toInt()
                    selectedMinute = parts[1].toInt()
                }
            } catch (e: Exception) {
                // Keep default values
            }
        }
    }
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .futuristicInput(
                    glowColor = if (isError) ErrorColor else if (showTimeDialog) Primary else BorderGlow.copy(alpha = 0.3f),
                    isFocused = showTimeDialog
                )
                .clickable { showTimeDialog = true }
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = value.ifEmpty { "Selecciona hora" },
                color = if (value.isEmpty()) TextPlaceholder else TextPrimary,
                fontSize = 16.sp
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        FuturisticIconButton(
            onClick = { showTimeDialog = true },
            icon = Icons.Filled.DateRange,
            size = 48.dp,
            iconSize = 24.dp,
            glowColor = NeonBlue,
            contentDescription = "Seleccionar hora"
        )
    }
    
    if (showTimeDialog) {
        AlertDialog(
            onDismissRequest = { showTimeDialog = false },
            confirmButton = {
                FuturisticButton(
                    onClick = {
                        val hour = selectedHour.toString().padStart(2, '0')
                        val minute = selectedMinute.toString().padStart(2, '0')
                        onValueChange("$hour:$minute")
                        showTimeDialog = false
                    },
                    isPrimary = true,
                    cornerRadius = 8.dp,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Aceptar",
                        color = FuturisticBackground,
                        fontSize = 14.sp
                    )
                }
            },
            dismissButton = {
                FuturisticButton(
                    onClick = { showTimeDialog = false },
                    isPrimary = false,
                    cornerRadius = 8.dp,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Cancelar",
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                }
            },
            title = {
                Text(
                    text = "Seleccionar $label",
                    color = TextAccent,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Hour selector
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Hora",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FuturisticDropdownTime(
                                selectedValue = selectedHour,
                                options = (0..23).toList(),
                                onValueSelected = { selectedHour = it },
                                modifier = Modifier.width(80.dp)
                            )
                        }
                        
                        Text(
                            text = ":",
                            color = TextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // Minute selector
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Minutos",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FuturisticDropdownTime(
                                selectedValue = selectedMinute,
                                options = (0..59).toList(),
                                onValueSelected = { selectedMinute = it },
                                modifier = Modifier.width(80.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "${selectedHour.toString().padStart(2, '0')}:${selectedMinute.toString().padStart(2, '0')}",
                        color = TextAccent,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = FuturisticSurface,
            titleContentColor = TextAccent,
            textContentColor = TextPrimary
        )
    }
}

@Composable
private fun FuturisticDropdownTime(
    selectedValue: Int,
    options: List<Int>,
    onValueSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .futuristicInput(
                    glowColor = if (expanded) Primary else BorderGlow.copy(alpha = 0.3f),
                    isFocused = expanded
                )
                .clickable { expanded = !expanded }
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = selectedValue.toString().padStart(2, '0'),
                color = TextPrimary,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(
                    color = FuturisticSurface,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                )
                .border(
                    width = 1.dp,
                    color = BorderGlow.copy(alpha = 0.5f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                )
                .heightIn(max = 200.dp)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = option.toString().padStart(2, '0'),
                            color = TextPrimary,
                            fontSize = 14.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        ) 
                    },
                    onClick = {
                        onValueSelected(option)
                        expanded = false
                    },
                    modifier = Modifier
                        .background(
                            color = if (option == selectedValue) SurfaceElevated else androidx.compose.ui.graphics.Color.Transparent,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        )
                )
            }
        }
    }
}