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
fun InterventionScreen(
    onNavigateBack: () -> Unit = {}
) {
    Scaffold(
        containerColor = FuturisticBackground,
        contentColor = TextPrimary,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Script de Intervención",
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
                
                InterventionTextField(
                    label = "Folio",
                    placeholder = "Ingresa el folio"
                )
                
                InterventionTextField(
                    label = "Cuenta",
                    placeholder = "Ingresa la cuenta"
                )
                
                InterventionTextField(
                    label = "OT",
                    placeholder = "Ingresa la OT"
                )
                
                InterventionTextField(
                    label = "Cliente",
                    placeholder = "Ingresa el cliente"
                )
                
                InterventionDropdown(
                    label = "Tipo de intervención",
                    options = listOf(
                        "Corte de Fibra óptica",
                        "Soporte",
                        "Migración del servicio",
                        "Instalacion nueva",
                        "Cambio de domicilio",
                        "Na"
                    ),
                    placeholder = "Selecciona tipo de intervención"
                )
                
                InterventionDropdown(
                    label = "Cuadrilla",
                    options = listOf(
                        "Xalapa cuadrilla 4",
                        "Xalapa cuadrilla 5",
                        "Xalapa cuadrilla 6",
                        "Na"
                    ),
                    placeholder = "Selecciona cuadrilla"
                )
                
                InterventionTextField(
                    label = "Supervisor",
                    defaultValue = "Iván de Jesús Jiménez Peña",
                    placeholder = "Supervisor asignado"
                )
            }       
     
            // Equipos y Materiales
            FuturisticCard(
                glowColor = NeonGreen,
                cornerRadius = 16.dp
            ) {
                Text(
                    text = "Equipos y Materiales",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextAccent,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                InterventionDropdown(
                    label = "Preconectorizado/Bobina",
                    options = listOf(
                        "Drop Preconectorizado",
                        "Bobina",
                        "Na"
                    ),
                    placeholder = "Selecciona tipo"
                )
                
                InterventionDropdown(
                    label = "Se utilizo acoplador (Si/No)",
                    options = listOf("Si", "No", "Na"),
                    placeholder = "Selecciona opción"
                )
                
                InterventionDropdown(
                    label = "Se realizó Detención (Si/No)",
                    options = listOf("Si", "No", "Na"),
                    placeholder = "Selecciona opción"
                )
                
                InterventionTextField(
                    label = "Marca del preconectorizado/Bobina",
                    placeholder = "Ingresa la marca"
                )
                
                InterventionTextField(
                    label = "Número de serie",
                    placeholder = "Ingresa el número de serie"
                )
                
                InterventionDropdown(
                    label = "Tipo de Drop",
                    options = listOf(
                        "350 mts",
                        "250 mts",
                        "150 mts",
                        "100 mts",
                        "50 mts",
                        "Na"
                    ),
                    placeholder = "Selecciona tipo de drop"
                )
            }    
        
            // Mediciones Técnicas
            FuturisticCard(
                glowColor = NeonCyan,
                cornerRadius = 16.dp
            ) {
                Text(
                    text = "Mediciones Técnicas",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextAccent,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                InterventionTextField(
                    label = "Metraje Ocupado",
                    placeholder = "Ingresa metraje ocupado"
                )
                
                InterventionTextField(
                    label = "Excedente en Gasa",
                    placeholder = "Ingresa excedente en gasa"
                )
                
                InterventionTextField(
                    label = "Lugar donde se deja gasa",
                    placeholder = "Ingresa lugar donde se deja gasa"
                )
                
                InterventionTextField(
                    label = "Metraje Bobina",
                    placeholder = "Ingresa metraje de bobina"
                )
                
                InterventionTextField(
                    label = "Spliter QR",
                    placeholder = "Ingresa spliter QR"
                )
                
                InterventionPowerField(
                    label = "Potencia del splitter",
                    placeholder = "16.80"
                )
                
                InterventionPowerField(
                    label = "Potencia en Bobina domicilio",
                    placeholder = "18.50"
                )
                
                InterventionPowerField(
                    label = "Potencia del preconectorizado",
                    placeholder = "15.20"
                )
                
                InterventionTextField(
                    label = "Metraje interno",
                    placeholder = "Ingresa metraje interno"
                )
                
                InterventionTextField(
                    label = "Metraje externo",
                    placeholder = "Ingresa metraje externo"
                )
            }  
          
            // Coordenadas GPS y Distancia
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
                
                InterventionCoordinatesField(
                    label = "Coordenadas del cliente",
                    placeholder = "Coordenadas GPS del cliente"
                )
                
                InterventionCoordinatesField(
                    label = "Coordenadas de splitter a red closterizada",
                    placeholder = "Coordenadas GPS del splitter"
                )
                
                InterventionCoordinatesField(
                    label = "Coordenadas de spliter red compartida",
                    placeholder = "Coordenadas GPS del spliter compartido"
                )
                
                InterventionTextField(
                    label = "Distancia de site a spliter red compartida",
                    placeholder = "Ingresa distancia"
                )
            }
            
            // Observaciones
            FuturisticCard(
                glowColor = NeonGreen,
                cornerRadius = 16.dp
            ) {
                Text(
                    text = "Observaciones",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextAccent,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                InterventionTextArea(
                    label = "Motivo para no usar preconectorizado",
                    placeholder = "Describe el motivo para no usar preconectorizado"
                )
                
                InterventionTextArea(
                    label = "Comentarios",
                    placeholder = "Ingresa comentarios adicionales"
                )
            }
            
            // Espacio adicional al final
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InterventionTextField(
    label: String,
    placeholder: String,
    defaultValue: String = "",
    modifier: Modifier = Modifier
) {
    var value by remember { mutableStateOf(defaultValue) }
    
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
private fun InterventionDropdown(
    label: String,
    options: List<String>,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var selectedValue by remember { mutableStateOf("") }
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
                            selectedValue = option
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
private fun InterventionCoordinatesField(
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FuturisticTextField(
                value = value,
                onValueChange = { value = it },
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
private fun InterventionPowerField(
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
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Signo negativo fijo
            Text(
                text = "-",
                fontSize = 16.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(end = 4.dp)
            )
            
            // Campo de entrada para el número
            FuturisticTextField(
                value = value,
                onValueChange = { newValue ->
                    // Solo permitir números y punto decimal
                    if (newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                        value = newValue
                    }
                },
                placeholder = placeholder,
                modifier = Modifier.weight(1f)
            )
            
            // Unidad dbm fija
            Text(
                text = "dbm",
                fontSize = 16.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun InterventionTextArea(
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
            maxLines = 4
        )
    }
}