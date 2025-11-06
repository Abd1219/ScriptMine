package com.abdapps.scriptmine.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abdapps.scriptmine.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CierreManualScreen(
    onNavigateBack: () -> Unit = {}
) {
    // Estados locales para la lógica visual
    var tipoIntervencion by remember { mutableStateOf("") }
    var clienteInventariado by remember { mutableStateOf("") }
    var tipoIntervencionPersonalizada by remember { mutableStateOf("") }
    var ot by remember { mutableStateOf("") }
    var csp by remember { mutableStateOf("") }
    var coordenadasCliente by remember { mutableStateOf("") }
    var coordenadasSplitter by remember { mutableStateOf("") }
    var justificacion by remember { mutableStateOf("") }
    var pantallaError by remember { mutableStateOf("") }

    Scaffold(
        containerColor = FuturisticBackground,
        contentColor = TextPrimary,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cierre Manual",
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
            // Información de Intervención
            FuturisticCard(
                glowColor = NeonBlue,
                cornerRadius = 16.dp
            ) {
                Text(
                    text = "Información de Intervención",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextAccent,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                CierreManualDropdown(
                    label = "Tipo de intervención",
                    selectedValue = tipoIntervencion,
                    options = listOf(
                        "Instalación nueva",
                        "Soporte en sitio sd",
                        "Soporte en sitio sf",
                        "Cambio de domicilio",
                        "Reubicación de equipos",
                        "Corte de fibra Optica",
                        "Otra (especificar)"
                    ),
                    onValueSelected = { tipoIntervencion = it },
                    placeholder = "Selecciona tipo de intervención"
                )
                
                // Campo condicional para tipo personalizado
                if (tipoIntervencion == "Otra (especificar)") {
                    CierreManualTextField(
                        label = "Tipo de intervención personalizada",
                        value = tipoIntervencionPersonalizada,
                        onValueChange = { tipoIntervencionPersonalizada = it },
                        placeholder = "Especifica el tipo de intervención"
                    )
                }
                
                CierreManualDropdown(
                    label = "Cliente inventariado (Si/No)",
                    selectedValue = clienteInventariado,
                    options = listOf("Si", "No", "Na"),
                    onValueSelected = { clienteInventariado = it },
                    placeholder = "Selecciona opción"
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
                
                CierreManualTextField(
                    label = "OT",
                    value = ot,
                    onValueChange = { ot = it },
                    placeholder = "Ingresa la OT"
                )
                
                CierreManualTextField(
                    label = "CSP",
                    value = csp,
                    onValueChange = { csp = it },
                    placeholder = "Ingresa el CSP"
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
                
                CierreManualCoordinatesField(
                    label = "Coordenadas del cliente",
                    value = coordenadasCliente,
                    onValueChange = { coordenadasCliente = it },
                    placeholder = "Coordenadas GPS del cliente"
                )
                
                CierreManualCoordinatesField(
                    label = "Coordenadas del splitter",
                    value = coordenadasSplitter,
                    onValueChange = { coordenadasSplitter = it },
                    placeholder = "Coordenadas GPS del splitter"
                )
            }
            
            // Observaciones
            FuturisticCard(
                glowColor = NeonCyan,
                cornerRadius = 16.dp
            ) {
                Text(
                    text = "Observaciones",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextAccent,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                CierreManualTextArea(
                    label = "Justificación",
                    value = justificacion,
                    onValueChange = { justificacion = it },
                    placeholder = "Ingresa la justificación del cierre manual"
                )
                
                CierreManualTextArea(
                    label = "Pantalla en caso de algún error",
                    value = pantallaError,
                    onValueChange = { pantallaError = it },
                    placeholder = "Describe la pantalla o error encontrado"
                )
            }
            
            // Espacio adicional al final
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CierreManualTextField(
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
private fun CierreManualDropdown(
    label: String,
    selectedValue: String,
    options: List<String>,
    onValueSelected: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Box(modifier = Modifier.fillMaxWidth()) {
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
                    overflow = TextOverflow.Ellipsis
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
                    .widthIn(max = 400.dp)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = option,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
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
}

@Composable
private fun CierreManualCoordinatesField(
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FuturisticTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = placeholder,
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            FuturisticIconButton(
                onClick = {
                    // Aquí se implementaría la lógica para obtener coordenadas GPS
                    // Por ahora solo es la estructura visual
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

@Composable
private fun CierreManualTextArea(
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