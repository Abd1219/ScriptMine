package com.abdapps.scriptmine.ui.screens

import android.Manifest
import androidx.activity.compose.ManagedActivityResultLauncher
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
import com.abdapps.scriptmine.ui.viewmodel.SplitterViewModel
import com.abdapps.scriptmine.utils.ClipboardHelper
import com.abdapps.scriptmine.utils.LocationHelper
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitterScreen(
    viewModel: SplitterViewModel,
    locationPermissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val locationHelper = remember { LocationHelper(context) }
    
    // Collect all StateFlows from ViewModel
    val cuentaSplitter by viewModel.cuentaSplitter.collectAsStateWithLifecycle()
    val clienteSplitter by viewModel.clienteSplitter.collectAsStateWithLifecycle()
    val splitter by viewModel.splitter.collectAsStateWithLifecycle()
    val qr by viewModel.qr.collectAsStateWithLifecycle()
    val posicion by viewModel.posicion.collectAsStateWithLifecycle()
    val potenciaEnSplitter by viewModel.potenciaEnSplitter.collectAsStateWithLifecycle()
    val potenciaEnDomicilio by viewModel.potenciaEnDomicilio.collectAsStateWithLifecycle()
    val candado by viewModel.candado.collectAsStateWithLifecycle()
    val coordenadasDeSplitter by viewModel.coordenadasDeSplitter.collectAsStateWithLifecycle()
    val coordenadasDelClienteSplitter by viewModel.coordenadasDelClienteSplitter.collectAsStateWithLifecycle()
    val scriptPreview by viewModel.scriptPreview.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val saveSuccess by viewModel.saveSuccess.collectAsStateWithLifecycle()
    val isGettingLocation by viewModel.isGettingLocation.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = FuturisticBackground,
        contentColor = TextPrimary,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Script de Splitter",
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
                // Información Básica
                FuturisticCard(
                    glowColor = NeonBlue,
                    cornerRadius = 16.dp
                ) {
                    Text(
                        text = "Información Básica",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextAccent,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    SplitterTextField(
                        label = "Cuenta",
                        value = cuentaSplitter,
                        onValueChange = viewModel::updateCuentaSplitter,
                        placeholder = "Ingresa la cuenta"
                    )
                    
                    SplitterTextField(
                        label = "Cliente",
                        value = clienteSplitter,
                        onValueChange = viewModel::updateClienteSplitter,
                        placeholder = "Ingresa el cliente"
                    )
                }
                
                // Datos de Conexión
                FuturisticCard(
                    glowColor = NeonGreen,
                    cornerRadius = 16.dp
                ) {
                    Text(
                        text = "DATOS DE CONEXIÓN",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextAccent,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    SplitterTextField(
                        label = "SPLITTER",
                        value = splitter,
                        onValueChange = viewModel::updateSplitter,
                        placeholder = "Ingresa el splitter"
                    )
                    
                    SplitterTextField(
                        label = "QR",
                        value = qr,
                        onValueChange = viewModel::updateQr,
                        placeholder = "Ingresa el QR"
                    )
                    
                    SplitterTextField(
                        label = "Posición",
                        value = posicion,
                        onValueChange = viewModel::updatePosicion,
                        placeholder = "Ingresa la posición"
                    )
                    
                    SplitterTextField(
                        label = "Potencia en splitter",
                        value = potenciaEnSplitter,
                        onValueChange = viewModel::updatePotenciaEnSplitter,
                        placeholder = "Ingresa potencia en splitter"
                    )
                    
                    SplitterTextField(
                        label = "Potencia en domicilio",
                        value = potenciaEnDomicilio,
                        onValueChange = viewModel::updatePotenciaEnDomicilio,
                        placeholder = "Ingresa potencia en domicilio"
                    )
                    
                    SplitterTextField(
                        label = "Candado",
                        value = candado,
                        onValueChange = viewModel::updateCandado,
                        placeholder = "Ingresa el candado"
                    )
                }
                
                // Coordenadas GPS
                FuturisticCard(
                    glowColor = NeonPurple,
                    cornerRadius = 16.dp
                ) {
                    Text(
                        text = "Coordenadas GPS",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextAccent,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    SplitterCoordinatesField(
                        label = "Coordenadas de splitter",
                        value = coordenadasDeSplitter,
                        placeholder = "Coordenadas GPS del splitter",
                        isGettingLocation = isGettingLocation,
                        onLocationClick = {
                            if (locationHelper.hasLocationPermission()) {
                                viewModel.onGetCoordinates("coordenadasDeSplitter")
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        }
                    )
                    
                    SplitterCoordinatesField(
                        label = "Coordenadas del cliente",
                        value = coordenadasDelClienteSplitter,
                        placeholder = "Coordenadas GPS del cliente",
                        isGettingLocation = isGettingLocation,
                        onLocationClick = {
                            if (locationHelper.hasLocationPermission()) {
                                viewModel.onGetCoordinates("coordenadasDelClienteSplitter")
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        }
                    )
                }
                
                // Vista Previa del Script
                FuturisticCard(
                    glowColor = NeonCyan,
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
                            .height(250.dp)
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
                
                // Copy button with feedback
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
private fun SplitterTextField(
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
private fun SplitterCoordinatesField(
    label: String,
    value: String,
    placeholder: String,
    isGettingLocation: Boolean,
    onLocationClick: () -> Unit,
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FuturisticTextField(
                value = value,
                onValueChange = { /* Read-only field */ },
                placeholder = placeholder,
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            FuturisticIconButton(
                onClick = onLocationClick,
                icon = if (isGettingLocation) Icons.Filled.Refresh else Icons.Filled.LocationOn,
                size = 48.dp,
                iconSize = 24.dp,
                glowColor = if (isGettingLocation) NeonBlue else NeonGreen,
                contentDescription = if (isGettingLocation) "Obteniendo ubicación..." else "Obtener ubicación"
            )
        }
    }
}