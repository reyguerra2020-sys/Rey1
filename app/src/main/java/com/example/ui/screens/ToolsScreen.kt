package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.SystemShortcuts
import com.example.model.CodeActionType
import com.example.ui.EarfcnCalcResult
import com.example.ui.components.MetricItem
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.SignalGreen
import com.example.ui.theme.SignalGreenText

@Composable
fun ToolsScreen(
    detectedBrand: String,
    earfcnInput: String,
    earfcnResult: EarfcnCalcResult,
    onEarfcnChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val codes = remember { SystemShortcuts.getRecommendedCodes() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Detected Device Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().testTag("detected_brand_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(CyanPrimary.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Dispositivo Detectado: $detectedBrand",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Menús de ingeniería optimizados para tu fabricante:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Secret Dial Codes & Engineering Menus
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Menús Secretos de Red y Módem",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Accede a las pantallas de servicio del módem para forzar bandas o ver agregación:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    codes.forEach { item ->
                        val isBrandMatch = item.brand.contains(detectedBrand, ignoreCase = true) || item.brand == "Universal Android"
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (isBrandMatch) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isBrandMatch) CyanPrimary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = item.brand,
                                        fontSize = 11.sp,
                                        color = CyanPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Text(
                                    text = item.code,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = SignalGreenText
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = {
                                        if (item.code == "*#2263#") {
                                            SystemShortcuts.openSamsungBandSelection(context)
                                        } else if (item.code == "*#*#4636#*#*") {
                                            SystemShortcuts.openRadioInfo(context)
                                        } else {
                                            SystemShortcuts.tryDialCode(context, item.code)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("launch_code_${item.code.filter { it.isLetterOrDigit() }}")
                                ) {
                                    Icon(
                                        imageVector = if (item.isDirectAppSupported) Icons.Default.Launch else Icons.Default.Call,
                                        contentDescription = null,
                                        tint = Color(0xFF00363D),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (item.isDirectAppSupported) "Abrir Menú" else "Marcar Código",
                                        color = Color(0xFF00363D),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(
                                    onClick = { SystemShortcuts.copyToClipboard(context, item.code, item.title) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copiar código",
                                        tint = CyanPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }

        // Band Locking Step-by-Step Tutorial
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Guía: Cómo Bloquear Bandas para 4G+",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val steps = listOf(
                        "1. Accede al menú de selección de bandas de tu teléfono (ej. *#2263# en Samsung o *#*#4636#*#* en Android general).",
                        "2. Comprueba en la pestaña 'Combos CA' qué pares de bandas usa tu operador (las más comunes son B3 + B7 o B2 + B4).",
                        "3. Si tu teléfono se queda en una banda lenta y saturada (ej. B8 o B20), desmárcala en la lista de bandas LTE y deja marcadas únicamente las bandas rápidas (B3 y B7).",
                        "4. Guarda los cambios. Tu teléfono se registrará inmediatamente en la celda de alta velocidad y negociará el portador secundario LTE+.",
                        "5. Si pierdes señal o sales de la ciudad, vuelve a seleccionar 'Automatic / All Bands' para recuperar cobertura en zonas rurales."
                    )

                    steps.forEach { step ->
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }

        // Interactive EARFCN & Frequency Calculator
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Calculadora de Bandas y EARFCN",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Convierte cualquier número de canal EARFCN en frecuencia MHz y Banda 3GPP:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = earfcnInput,
                        onValueChange = onEarfcnChanged,
                        label = { Text("Número EARFCN (ej. 1650, 3100, 9360)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("earfcn_input_field")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricItem(
                            label = "Banda 3GPP",
                            value = earfcnResult.bandLabel,
                            modifier = Modifier.weight(1f),
                            highlight = true
                        )
                        MetricItem(
                            label = "Modo Dúplex",
                            value = earfcnResult.duplex,
                            modifier = Modifier.weight(0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricItem(
                            label = "Bajada (Downlink)",
                            value = "${earfcnResult.downlinkMhz} MHz",
                            modifier = Modifier.weight(1f)
                        )
                        MetricItem(
                            label = "Subida (Uplink)",
                            value = "${earfcnResult.uplinkMhz} MHz",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
