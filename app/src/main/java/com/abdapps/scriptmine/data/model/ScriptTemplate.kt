package com.abdapps.scriptmine.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class ScriptTemplate(
    val displayName: String,
    val icon: ImageVector,
    val fields: List<ScriptField>
) {
    TIPIFICACION(
        displayName = "Script de tipificación",
        icon = Icons.Filled.Edit,
        fields = listOf(
            ScriptField.TEXT("folio", "Folio", true),
            ScriptField.TEXT("ot", "OT", true),
            ScriptField.TEXT("cliente", "Cliente", true),
            ScriptField.DROPDOWN("tipo_incidencia", "Tipo de Incidencia", true, 
                listOf(
                    "1. Corte o Atenuación de FO (Acometida) / Cambio de acometida, cambio, limpieza o rearmado de conectores",
                    "2. Servicio Activo (Sin intervención técnica) / Validación de servicio / Pruebas",
                    "3. Incidente atribuible al cliente / Reconexión de equipo(s) / Eliminación de Dobleces",
                    "4. Bloqueo de equipo / Puerto / HSU / Reset de equipo y/o Actualización de Firmware",
                    "5. Robo de puerto / Vandalismo / Sabotaje / Validación de splitter y asignación de puerto",
                    "6. Configuración local de ONT / CPE / HSU / Se modifica configuración de equipo en sitio",
                    "7. Aprovisionamiento de ONT / CPE / HSU / Se reaprovisiona el servicio por medio de un INC",
                    "8. Falla de cableado en site (Lado LAN) / Cambio de cable, jumper o conector",
                    "9. Falla de cableado en site (Lado WAN) / Limpieza de conector / Eliminación de Dobleces / Cambio de Jumper",
                    "10. Daño físico en puerto/equipo (Hardware) / Reemplazo de equipo o cambio de puerto",
                    "11. Infraestructura de MW / Se reinstala o se corrige infraestructura",
                    "12. Saturación en puerto PON / Migración de puerto PON/Metro",
                    "13. Splitter atenuado / Sin Potencia / Reparación de potencia en splitter"
                )),
            ScriptField.TEXTAREA("actividades_realizadas", "Actividades realizadas", false),
            ScriptField.TEXTAREA("observaciones", "Observaciones y contratiempos durante la actividad", false)
        )
    ),
    
    INTERVENCION(
        displayName = "Script de intervención",
        icon = Icons.Filled.Warning,
        fields = listOf(
            ScriptField.TEXT("folio", "Folio", true),
            ScriptField.TEXT("cuenta", "Cuenta", true),
            ScriptField.TEXT("ot", "OT", true),
            ScriptField.TEXT("cliente", "Cliente", true),
            ScriptField.TEXT("supervisor", "Supervisor", false, "Iván de Jesús Jiménez Peña"),
            ScriptField.DROPDOWN("tipo_intervencion", "Tipo de intervención", true,
                listOf("Corte de Fibra óptica", "Soporte", "Migración del servicio", "Instalacion nueva", "Cambio de domicilio", "Na")),
            ScriptField.DROPDOWN("cuadrilla", "Cuadrilla", true,
                listOf("Xalapa cuadrilla 4", "Xalapa cuadrilla 5", "Xalapa cuadrilla 6", "Na")),
            ScriptField.TEXT("marca_preconectorizado", "Marca del preconectorizado/Bobina", false),
            ScriptField.TEXT("numero_serie", "Número de serie", false),
            ScriptField.DROPDOWN("preconectorizado_bobina", "Preconectorizado/Bobina", false,
                listOf("Drop Preconectorizado", "Bobina", "Na")),
            ScriptField.TEXT("metraje_ocupado", "Metraje Ocupado (mts)", false),
            ScriptField.TEXT("excedente_gasa", "Excedente en Gasa (mts)", false),
            ScriptField.TEXT("lugar_gasa", "Lugar donde se deja gasa", false),
            ScriptField.TEXT("metraje_bobina", "Metraje Bobina (mts)", false),
            ScriptField.TEXT("spliter_qr", "Spliter QR", false),
            ScriptField.TEXT("potencia_splitter", "Potencia del splitter (dbm)", false),
            ScriptField.TEXT("potencia_bobina", "Potencia en Bobina domicilio (dbm)", false),
            ScriptField.TEXT("potencia_preconectorizado", "Potencia del preconectorizado (dbm)", false),
            ScriptField.TEXT("metraje_interno", "Metraje interno (mts)", false),
            ScriptField.TEXT("metraje_externo", "Metraje externo (mts)", false),
            ScriptField.DROPDOWN("uso_acoplador", "Se utilizo acoplador (Si/No)", false,
                listOf("Si", "No", "Na")),
            ScriptField.DROPDOWN("realizo_detencion", "Se realizó Detención (Si/No)", false,
                listOf("Si", "No", "Na")),
            ScriptField.DROPDOWN("tipo_drop", "Tipo de Drop", false,
                listOf("350 mts", "250 mts", "150 mts", "100 mts", "50 mts", "Na")),
            ScriptField.COORDINATES("coordenadas_cliente", "Coordenadas del cliente", false),
            ScriptField.COORDINATES("coordenadas_splitter_closter", "Coordenadas de splitter a red closterizada", false),
            ScriptField.TEXT("distancia_site_splitter", "Distancia de site a spliter red compartida", false),
            ScriptField.COORDINATES("coordenadas_splitter_compartida", "Coordenadas de spliter red compartida", false),
            ScriptField.TEXTAREA("motivo_no_preconectorizado", "Motivo para no usar preconectorizado", false),
            ScriptField.TEXTAREA("comentarios", "Comentarios", false)
        )
    ),
    
    SOPORTE(
        displayName = "Script de soporte",
        icon = Icons.Filled.Info,
        fields = listOf(
            ScriptField.TIME("hora_inicio", "Hora de inicio", true),
            ScriptField.TIME("hora_termino", "Hora de Termino", true),
            ScriptField.TEXT("tiempo_espera", "Tiempo de espera para accesos", false),
            ScriptField.TEXTAREA("actividades_soporte", "Actividades realizadas en sitio", false),
            ScriptField.TEXTAREA("observaciones_soporte", "Observaciones y contratiempos durante la actividad", false)
        )
    ),
    
    SPLITTER(
        displayName = "Script de splitter",
        icon = Icons.Filled.Build,
        fields = listOf(
            ScriptField.TEXT("cuentaSplitter", "Cuenta", true),
            ScriptField.TEXT("clienteSplitter", "Cliente", true),
            ScriptField.TEXT("splitter", "SPLITTER", true),
            ScriptField.TEXT("qr", "QR", true),
            ScriptField.TEXT("posicion", "Posición", true),
            ScriptField.TEXT("potenciaEnSplitter", "Potencia en splitter", false),
            ScriptField.TEXT("potenciaEnDomicilio", "Potencia en domicilio", false),
            ScriptField.TEXT("candado", "Candado", false),
            ScriptField.COORDINATES("coordenadasDeSplitter", "Coordenadas de splitter", false),
            ScriptField.COORDINATES("coordenadasDelClienteSplitter", "Coordenadas del cliente", false)
        )
    ),
    
    CIERRE_MANUAL(
        displayName = "Script de cierre manual",
        icon = Icons.Filled.Close,
        fields = listOf(
            ScriptField.TEXT("ticket", "Número de ticket", true),
            ScriptField.TEXT("cliente", "Cliente", true),
            ScriptField.DROPDOWN("motivo_cierre", "Motivo de cierre", true,
                listOf("Resuelto", "Duplicado", "No procede", "Cliente no responde", "Otro")),
            ScriptField.TEXT("descripcion_solucion", "Descripción de la solución", true),
            ScriptField.TEXT("tiempo_resolucion", "Tiempo de resolución (horas)", false),
            ScriptField.DROPDOWN("satisfaccion", "Nivel de satisfacción", false,
                listOf("Muy satisfecho", "Satisfecho", "Neutral", "Insatisfecho", "Muy insatisfecho")),
            ScriptField.TEXT("comentarios_adicionales", "Comentarios adicionales", false)
        )
    ),
    
    APOYO_MW_OPS(
        displayName = "Apoyo MW OPS",
        icon = Icons.Filled.Settings,
        fields = listOf(
            ScriptField.TEXT("folio", "Folio", true),
            ScriptField.TEXT("ot", "OT", true),
            ScriptField.TEXT("cliente", "Cliente", true),
            ScriptField.DROPDOWN("tipo_intervencion", "Tipo de intervención", true,
                listOf("Instalación nueva", "Soporte en sitio sd", "Soporte en sitio sf", 
                       "Cambio de domicilio", "Reubicación de equipos", "Corte de fibra Optica", "Otra (especificar)")),
            ScriptField.TEXT("tipo_intervencion_personalizada", "Tipo de intervención personalizada", false),
            ScriptField.DROPDOWN("cliente_inventariado", "Cliente inventariado", true,
                listOf("Si", "No", "Na")),
            ScriptField.TEXT("csp", "CSP", false),
            ScriptField.COORDINATES("coordenadas_cliente", "Coordenadas del cliente", false),
            ScriptField.COORDINATES("coordenadas_splitter", "Coordenadas del splitter", false),
            ScriptField.TEXTAREA("justificacion", "Justificación", false),
            ScriptField.TEXTAREA("pantalla_error", "Pantalla en caso de algún error", false)
        )
    )
}

sealed class ScriptField(
    val key: String,
    val label: String,
    val required: Boolean
) {
    class TEXT(key: String, label: String, required: Boolean, val defaultValue: String = "") : ScriptField(key, label, required)
    class DROPDOWN(key: String, label: String, required: Boolean, val options: List<String>) : ScriptField(key, label, required)
    class COORDINATES(key: String, label: String, required: Boolean) : ScriptField(key, label, required)
    class TEXTAREA(key: String, label: String, required: Boolean) : ScriptField(key, label, required)
    class TIME(key: String, label: String, required: Boolean) : ScriptField(key, label, required)
}