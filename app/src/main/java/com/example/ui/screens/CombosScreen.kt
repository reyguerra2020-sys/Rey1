package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.SpeedTestState
import com.example.model.CellDetails
import com.example.model.LteBandDatabase
import com.example.ui.components.MetricItem
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.SignalGreen
import com.example.ui.theme.SignalGreenContainer
import com.example.ui.theme.SignalGreenText
import com.example.ui.theme.WarningAmber

@Composable
fun CombosScreen(
    cellDetails: CellDetails,
    speedTestState: SpeedTestState,
    onRunSpeedTest: () -> Unit,
    onResetSpeedTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val combos = remember { LteBandDatabase.popularCaCombos }
    val bands = remember { LteBandDatabase.allBands }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Speed & Latency Benchmark Card
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

        // Popular Carrier Aggregation Combos
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

                    Text(
                        text = "Pares y tríos de bandas agregadas por operadores para ofrecer velocidades 4G+:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

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

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "MIMO: ${combo.mimo}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${combo.carriersCount} Portadores",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CyanPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Operadores habituales: ${combo.commonOperators}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }

        // LTE Band Reference Guide
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Diccionario de Bandas 3GPP LTE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Frecuencias y designaciones oficiales:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    bands.forEach { band ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = band.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = band.commonUsage,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = band.dlFreqRange,
                                fontSize = 11.sp,
                                color = CyanPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
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
