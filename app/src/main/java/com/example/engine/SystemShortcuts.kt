package com.example.engine

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import com.example.model.CodeActionType
import com.example.model.SecretCodeItem

object SystemShortcuts {

    fun openRadioInfo(context: Context): Boolean {
        // Attempt 1: Direct Component launch of RadioInfo (Testing menu)
        val targets = listOf(
            Pair("com.android.settings", "com.android.settings.RadioInfo"),
            Pair("com.android.settings", "com.android.settings.TestingSettings"),
            Pair("com.android.phone", "com.android.phone.settings.RadioInfo")
        )

        for ((pkg, cls) in targets) {
            try {
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    component = ComponentName(pkg, cls)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                Toast.makeText(context, "Abriendo Información de Radio (Menú de Pruebas)", Toast.LENGTH_SHORT).show()
                return true
            } catch (_: Exception) {
                // Try next
            }
        }

        // Attempt 2: Dial *#*#4636#*#*
        return tryDialCode(context, "*#*#4636#*#*")
    }

    fun openSamsungBandSelection(context: Context): Boolean {
        // Attempt hidden network setting activity
        try {
            val intent = Intent().apply {
                component = ComponentName(
                    "com.samsung.android.app.telephonyui",
                    "com.samsung.android.app.telephonyui.hiddennetworksetting.MainActivity"
                )
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Toast.makeText(context, "Abriendo Selección de Bandas Samsung", Toast.LENGTH_SHORT).show()
            return true
        } catch (_: Exception) {
            // Fallback to dial *#2263#
            return tryDialCode(context, "*#2263#")
        }
    }

    fun openNetworkOperatorSettings(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            openWirelessSettings(context)
        }
    }

    fun openDataRoamingSettings(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_DATA_ROAMING_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            openWirelessSettings(context)
        }
    }

    fun openAirplaneModeSettings(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            openWirelessSettings(context)
        }
    }

    private fun openWirelessSettings(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo abrir los ajustes de red", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun tryDialCode(context: Context, code: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${Uri.encode(code)}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Toast.makeText(context, "Código $code enviado al marcador", Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            copyToClipboard(context, code, "Código")
            false
        }
    }

    fun copyToClipboard(context: Context, text: String, label: String = "Código") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copiado al portapapeles: $text", Toast.LENGTH_SHORT).show()
    }

    fun getDetectedBrand(): String {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return when {
            manufacturer.contains("samsung") -> "Samsung"
            manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco") -> "Xiaomi / POCO"
            manufacturer.contains("motorola") || manufacturer.contains("moto") -> "Motorola"
            manufacturer.contains("google") -> "Google Pixel"
            manufacturer.contains("oneplus") -> "OnePlus"
            manufacturer.contains("oppo") -> "OPPO"
            manufacturer.contains("realme") -> "Realme"
            manufacturer.contains("huawei") || manufacturer.contains("honor") -> "Huawei / Honor"
            else -> Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        }
    }

    fun getRecommendedCodes(): List<SecretCodeItem> {
        val brand = getDetectedBrand()
        val allCodes = mutableListOf<SecretCodeItem>()

        // Universal Android Testing code
        allCodes.add(
            SecretCodeItem(
                title = "Menú de Pruebas Android (RadioInfo)",
                code = "*#*#4636#*#*",
                brand = "Universal Android",
                description = "Permite bloquear 'LTE Only', ver estado del módem, ping celular y forzar bandas compatibles.",
                actionType = CodeActionType.LAUNCH_INTENT,
                isDirectAppSupported = true
            )
        )

        // Samsung specific
        allCodes.add(
            SecretCodeItem(
                title = "Samsung ServiceMode (Monitoreo CA y Bandas)",
                code = "*#0011#",
                brand = "Samsung",
                description = "Muestra en tiempo real si Carrier Aggregation está activo (CA: ADDED), portador secundario SCELL 1, SCELL 2, y ancho de banda.",
                actionType = CodeActionType.DIAL_CODE
            )
        )
        allCodes.add(
            SecretCodeItem(
                title = "Samsung Band Selection (Bloqueador de Bandas)",
                code = "*#2263#",
                brand = "Samsung",
                description = "Menú secreto de ingeniería para forzar bandas específicas (ej. solo B3 y B7) e impedir que el teléfono use bandas lentas.",
                actionType = CodeActionType.LAUNCH_INTENT,
                isDirectAppSupported = true
            )
        )

        // Xiaomi
        allCodes.add(
            SecretCodeItem(
                title = "Menú Diagnóstico Hardware CIT (Xiaomi / POCO)",
                code = "*#*#6484#*#*",
                brand = "Xiaomi / POCO",
                description = "Verificación de antenas RF, estado de las ranuras SIM y calibración de módem.",
                actionType = CodeActionType.DIAL_CODE
            )
        )

        // Motorola
        allCodes.add(
            SecretCodeItem(
                title = "Menú de Ingeniería CQA (Motorola)",
                code = "*#*#2486#*#*",
                brand = "Motorola",
                description = "Menú de calibración de módem Qualcomm y pruebas de conectividad LTE.",
                actionType = CodeActionType.DIAL_CODE
            )
        )

        // OnePlus / Oppo / Realme
        allCodes.add(
            SecretCodeItem(
                title = "Engineer Mode (OnePlus / Oppo / Realme)",
                code = "*#899#",
                brand = "OnePlus / Realme / Oppo",
                description = "Acceso a parámetros de radiofrecuencia, bloqueo de red y pruebas de rendimiento de módem.",
                actionType = CodeActionType.DIAL_CODE
            )
        )

        // Huawei
        allCodes.add(
            SecretCodeItem(
                title = "ProjectMenu (Huawei / Honor)",
                code = "*#*#2846579#*#*",
                brand = "Huawei / Honor",
                description = "Consulta de información de red, parámetros de celda y configuración de banda base.",
                actionType = CodeActionType.DIAL_CODE
            )
        )

        // Sort so that the user's detected brand appears first!
        return allCodes.sortedByDescending {
            it.brand.contains(brand, ignoreCase = true) || it.brand == "Universal Android"
        }
    }
}
