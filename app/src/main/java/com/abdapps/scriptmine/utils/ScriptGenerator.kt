package com.abdapps.scriptmine.utils

import com.abdapps.scriptmine.data.model.ScriptTemplate
import java.text.SimpleDateFormat
import java.util.*

object ScriptGenerator {
    
    fun generateScript(template: ScriptTemplate, data: Map<String, String>): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        
        return when (template) {
            ScriptTemplate.TIPIFICACION -> generateTipificacionScript(data, currentDate)
            ScriptTemplate.INTERVENCION -> generateIntervencionScript(data, currentDate)
            ScriptTemplate.SOPORTE -> generateSoporteScript(data, currentDate)
            ScriptTemplate.SPLITTER -> generateSplitterScript(data, currentDate)
            ScriptTemplate.CIERRE_MANUAL -> generateCierreManualScript(data, currentDate)
            ScriptTemplate.APOYO_MW_OPS -> generateApoyoMwOpsScript(data, currentDate)
        }
    }
    
    fun generateNormalizedScript(template: ScriptTemplate, data: Map<String, String>): String {
        val originalScript = generateScript(template, data)
        return normalizeText(originalScript)
    }
    
    private fun normalizeText(text: String): String {
        if (text.isEmpty()) return ""
        
        return text
            // Remove line breaks and replace with spaces
            .replace("\n", " ")
            .replace("\r", " ")
            // Convert to lowercase
            .lowercase()
            // Remove accents and special characters
            .replace("á", "a").replace("à", "a").replace("ä", "a").replace("â", "a")
            .replace("é", "e").replace("è", "e").replace("ë", "e").replace("ê", "e")
            .replace("í", "i").replace("ì", "i").replace("ï", "i").replace("î", "i")
            .replace("ó", "o").replace("ò", "o").replace("ö", "o").replace("ô", "o")
            .replace("ú", "u").replace("ù", "u").replace("ü", "u").replace("û", "u")
            .replace("ñ", "n")
            .replace("ç", "c")
            // Remove punctuation and special characters
            .replace(Regex("[.,;:!?¡¿\"'()\\[\\]{}\\-_+=*&%$#@|\\\\/<>~`^]"), "")
            // Replace multiple spaces with single space
            .replace(Regex("\\s+"), " ")
            // Trim whitespace
            .trim()
    }
    
    private fun generateTipificacionScript(data: Map<String, String>, date: String): String {
        return buildString {
            append("Folio: ${data["folio"] ?: ""}")
            appendLine()
            append("OT: ${data["ot"] ?: ""}")
            appendLine()
            append("Cliente: ${data["cliente"] ?: ""}")
            appendLine()
            append("Diagnóstico/Solución: ${data["tipo_incidencia"] ?: ""}")
            appendLine()
            append("Actividades Realizadas: ${data["actividades_realizadas"] ?: ""}")
            appendLine()
            append("Observaciones y contratiempos durante la actividad: ${data["observaciones"] ?: ""}")
        }
    }
    
    private fun generateIntervencionScript(data: Map<String, String>, date: String): String {
        return buildString {
            append("Folio: ${data["folio"] ?: ""}")
            appendLine()
            append("Cuenta: ${data["cuenta"] ?: ""}")
            appendLine()
            append("OT: ${data["ot"] ?: ""}")
            appendLine()
            append("Cliente: ${data["cliente"] ?: ""}")
            appendLine()
            append("Supervisor: ${data["supervisor"] ?: ""}")
            appendLine()
            append("Tipo de intervención: ${data["tipo_intervencion"] ?: ""}")
            appendLine()
            append("Cuadrilla: ${data["cuadrilla"] ?: ""}")
            appendLine()
            append("Marca del preconectorizado/Bobina: ${data["marca_preconectorizado"] ?: ""}")
            appendLine()
            append("Número de serie: ${data["numero_serie"] ?: ""}")
            appendLine()
            append("Preconectorizado/Bobina: ${data["preconectorizado_bobina"] ?: ""}")
            appendLine()
            append("Metraje Ocupado (mts): ${data["metraje_ocupado"] ?: ""}")
            appendLine()
            append("Excedente en Gasa (mts): ${data["excedente_gasa"] ?: ""}")
            appendLine()
            append("Lugar donde se deja gasa: ${data["lugar_gasa"] ?: ""}")
            appendLine()
            append("Metraje Bobina (mts): ${data["metraje_bobina"] ?: ""}")
            appendLine()
            append("Spliter QR: ${data["spliter_qr"] ?: ""}")
            appendLine()
            append("Potencia del splitter (dbm): ${data["potencia_splitter"] ?: ""}")
            appendLine()
            append("Potencia en Bobina domicilio (dbm): ${data["potencia_bobina"] ?: ""}")
            appendLine()
            append("Potencia del preconectorizado (dbm): ${data["potencia_preconectorizado"] ?: ""}")
            appendLine()
            append("Metraje interno (mts): ${data["metraje_interno"] ?: ""}")
            appendLine()
            append("Metraje externo (mts): ${data["metraje_externo"] ?: ""}")
            appendLine()
            append("Se utilizo acoplador: ${data["uso_acoplador"] ?: ""}")
            appendLine()
            append("Se realizó Detención: ${data["realizo_detencion"] ?: ""}")
            appendLine()
            append("Tipo de Drop: ${data["tipo_drop"] ?: ""}")
            appendLine()
            append("Coordenadas del cliente: ${data["coordenadas_cliente"] ?: ""}")
            appendLine()
            append("Coordenadas de splitter a red closterizada: ${data["coordenadas_splitter_closter"] ?: ""}")
            appendLine()
            append("Distancia de site a spliter red compartida: ${data["distancia_site_splitter"] ?: ""}")
            appendLine()
            append("Coordenadas de spliter red compartida: ${data["coordenadas_splitter_compartida"] ?: ""}")
            appendLine()
            append("Motivo para no usar preconectorizado: ${data["motivo_no_preconectorizado"] ?: ""}")
            appendLine()
            append("Comentarios: ${data["comentarios"] ?: ""}")
        }
    }
    
    private fun generateSoporteScript(data: Map<String, String>, date: String): String {
        return buildString {
            appendLine("Hora de inicio: ${data["hora_inicio"] ?: ""}")
            appendLine("Hora de Termino: ${data["hora_termino"] ?: ""}")
            appendLine("Tiempo de espera para accesos: ${data["tiempo_espera"] ?: ""}")
            appendLine("Actividades realizadas en sitio: ${data["actividades_soporte"] ?: ""}")
            append("Observaciones y contratiempos durante la actividad: ${data["observaciones_soporte"] ?: ""}")
        }
    }
    
    private fun generateSplitterScript(data: Map<String, String>, date: String): String {
        return buildString {
            appendLine("Cuenta: ${data["cuentaSplitter"] ?: ""}")
            appendLine("Cliente: ${data["clienteSplitter"] ?: ""}")
            appendLine()
            appendLine("DATOS DE CONEXIÓN")
            appendLine("SPLITTER: ${data["splitter"] ?: ""}")
            appendLine("QR: ${data["qr"] ?: ""}")
            appendLine("Posición: ${data["posicion"] ?: ""}")
            
            // Add " dbm" suffix to power values if they exist and don't already have it
            val potenciaSplitter = data["potenciaEnSplitter"] ?: ""
            if (potenciaSplitter.isNotEmpty() && !potenciaSplitter.contains("dbm", ignoreCase = true)) {
                appendLine("Potencia en splitter: $potenciaSplitter dbm")
            } else {
                appendLine("Potencia en splitter: $potenciaSplitter")
            }
            
            val potenciaDomicilio = data["potenciaEnDomicilio"] ?: ""
            if (potenciaDomicilio.isNotEmpty() && !potenciaDomicilio.contains("dbm", ignoreCase = true)) {
                appendLine("Potencia en domicilio: $potenciaDomicilio dbm")
            } else {
                appendLine("Potencia en domicilio: $potenciaDomicilio")
            }
            
            appendLine("Candado: ${data["candado"] ?: ""}")
            appendLine("Coordenadas de splitter: ${data["coordenadasDeSplitter"] ?: ""}")
            append("Coordenadas del cliente: ${data["coordenadasDelClienteSplitter"] ?: ""}")
        }
    }
    
    private fun generateCierreManualScript(data: Map<String, String>, date: String): String {
        return buildString {
            appendLine("=== DETALLES DE CIERRE MANUAL ===")
            appendLine("Fecha: $date")
            appendLine()
            appendLine("TIPO DE INTERVENCIÓN:")
            val tipoIntervencion = data["tipo_intervencion"] ?: "N/A"
            appendLine("• $tipoIntervencion")
            
            // Si seleccionó "Otra (especificar)", mostrar el campo personalizado
            if (tipoIntervencion.contains("Otra", ignoreCase = true)) {
                val tipoPersonalizado = data["tipo_intervencion_personalizada"]
                if (!tipoPersonalizado.isNullOrEmpty()) {
                    appendLine("• Especificación: $tipoPersonalizado")
                }
            }
            
            appendLine()
            appendLine("--- Fin del script ---")
        }
    }
    
    private fun generateApoyoMwOpsScript(data: Map<String, String>, date: String): String {
        return buildString {
            appendLine("=== APOYO SOPORTE MW OPS ===")
            appendLine("Fecha: $date")
            appendLine()
            appendLine("INFORMACIÓN DE INTERVENCIÓN:")
            appendLine("• Folio: ${data["folio"] ?: "N/A"}")
            appendLine("• OT: ${data["ot"] ?: "N/A"}")
            appendLine("• Cliente: ${data["cliente"] ?: "N/A"}")
            appendLine("• Tipo de intervención: ${data["tipo_intervencion"] ?: "N/A"}")
            if (data["tipo_intervencion_personalizada"]?.isNotEmpty() == true) {
                appendLine("• Tipo personalizada: ${data["tipo_intervencion_personalizada"]}")
            }
            appendLine("• Cliente inventariado: ${data["cliente_inventariado"] ?: "N/A"}")
            appendLine()
            appendLine("INFORMACIÓN TÉCNICA:")
            if (data["csp"]?.isNotEmpty() == true) {
                appendLine("• CSP: ${data["csp"]}")
            }
            appendLine()
            appendLine("COORDENADAS GPS:")
            if (data["coordenadas_cliente"]?.isNotEmpty() == true) {
                appendLine("• Coordenadas del cliente: ${data["coordenadas_cliente"]}")
            }
            if (data["coordenadas_splitter"]?.isNotEmpty() == true) {
                appendLine("• Coordenadas del splitter: ${data["coordenadas_splitter"]}")
            }
            appendLine()
            if (data["justificacion"]?.isNotEmpty() == true) {
                appendLine("JUSTIFICACIÓN:")
                appendLine("${data["justificacion"]}")
                appendLine()
            }
            if (data["pantalla_error"]?.isNotEmpty() == true) {
                appendLine("PANTALLA EN CASO DE ERROR:")
                appendLine("${data["pantalla_error"]}")
                appendLine()
            }
            appendLine("--- Fin del script ---")
        }
    }
}