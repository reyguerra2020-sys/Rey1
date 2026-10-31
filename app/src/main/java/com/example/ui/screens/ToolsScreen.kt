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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.SpeedTestState
import com.example.engine.SystemShortcuts
import com.example.model.CellDetails
import com.example.model.CodeActionType
import com.example.model.LteBandDatabase
import com.example.ui.EarfcnCalcResult
import com.example.ui.components.MetricItem
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.SignalGreen
import com.example.ui.theme.SignalGreenContainer
import com.example.ui.theme.SignalGreenText

@Composable
fun ToolsScreen(
    detectedBrand: String,
    earfcnInput: String,
    earfcnResult: EarfcnCalcResult,
    cellDetails: CellDetails,
    speedTestState: SpeedTestState,
    onEarfcnChanged: (String) -> Unit,
    onRunSpeedTest: () -> Unit,
    onResetSpeedTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val codes = remember { SystemShortcuts.getRecommendedCodes() }
    val combos = remember { LteBandDatabase.popularCaCombos }
    var selectedToolTabIndex by remember { mutableIntStateOf(0) }
    val toolTabs = listOf("Menús Secretos", "Test & Combos", "Calculadora EARFCN")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Sub-tabs navigation
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TabRow(
                    selectedTabIndex = selectedToolTabIndex,
                    containerColor = Color.Transparent,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedToolTabIndex]),
                            color = CyanPrimary
                        )
                    }
                ) {
                    toolTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedToolTabIndex == index,
                            onClick = { selectedToolTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedToolTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedToolTabIndex == index) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                }
            }
        }

        // Tab 0: Secret Dial Codes & Engineering Menus
        if (selectedToolTabIndex == 0) {
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
                                text = "Dispositivo: $detectedBrand",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Menús de ingeniería compatibles para tu fabricante:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

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
        }

        // Tab 1: Speed Test & Popular CA Combos
        if (selectedToolTabIndex == 1) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.5.dp,
                            color = if (speedTestState.isTesting) CyanPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .testTag("speed_test_card")
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Test de Velocidad 4G / 4G+",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            StatusBadge(isCaActive = cellDetails.isCarrierAggregationActive)
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Digital Speedometer Display
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                .border(
                                    width = 3.dp,
                                    brush = if (speedTestState.isTesting) Brush.sweepGradient(listOf(CyanPrimary, SignalGreen, CyanPrimary))
                                    else Brush.sweepGradient(listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outline)),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (speedTestState.isTesting || speedTestState.isFinished) "${speedTestState.currentMbps}" else "---",
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (cellDetails.isCarrierAggregationActive) SignalGreenText else CyanPrimary
                                )
                                Text(
                                    text = "Mbps de Bajada",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (speedTestState.isFinished) {
                                    Text(
                                        text = "Pico: ${speedTestState.peakMbps} Mbps",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SignalGreenText
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = speedTestState.phase,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (speedTestState.isTesting) {
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { speedTestState.progress },
                                modifier = Modifier.fillMaxWidth().height(6.dp),
                                color = CyanPrimary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricItem(
                                label = "Latencia (Ping)",
                                value = if (speedTestState.pingMs > 0) "${speedTestState.pingMs} ms" else "---",
                                modifier = Modifier.weight(1f)
                            )
                            MetricItem(
                                label = "Jitter",
                                value = if (speedTestState.jitterMs > 0) "${speedTestState.jitterMs} ms" else "---",
                                modifier = Modifier.weight(1f)
                            )
                            MetricItem(
                                label = "Modo de Red",
                                value = if (speedTestState.caModeActiveDuringTest || cellDetails.isCarrierAggregationActive) "4G+ (CA)" else "4G Único",
                                modifier = Modifier.weight(1f),
                                highlight = speedTestState.caModeActiveDuringTest || cellDetails.isCarrierAggregationActive
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onRunSpeedTest,
                                enabled = !speedTestState.isTesting,
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).testTag("start_speed_test_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color(0xFF00363D),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (speedTestState.isTesting) "Midiendo..." else "Iniciar Test",
                                    color = Color(0xFF00363D),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (speedTestState.isFinished) {
                                OutlinedButton(
                                    onClick = onResetSpeedTest,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Popular CA Combos
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Combinaciones CA Más Populares",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        combos.forEach { combo ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = combo.comboName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = CyanPrimary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(SignalGreenContainer, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Hasta ${combo.theoreticalDlMbps} Mbps",
                                            color = SignalGreenText,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = combo.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Operadores: ${combo.commonOperators}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // Tab 2: Interactive EARFCN & Frequency Calculator
        if (selectedToolTabIndex == 2) {
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
                            "1. Ve a la pestaña 'Bandas' en esta app y marca tus bandas preferidas (ej. B3 + B7).",
                            "2. Presiona 'Abrir Selección de Bandas Samsung (*#2263#)' o 'RadioInfo' según tu equipo.",
                            "3. Desmarca las bandas lentas y congestionadas (como B8 o B20) y deja activas solo las rápidas.",
                            "4. Guarda los cambios para que el módem negocie los portadores LTE-A Pro inmediatamente.",
                            "5. Si viajas a zonas rurales sin cobertura de bandas altas, selecciona 'Todas (Automático)'."
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
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
