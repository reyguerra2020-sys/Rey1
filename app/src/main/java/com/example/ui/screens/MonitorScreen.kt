package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.SystemShortcuts
import com.example.model.CellDetails
import com.example.ui.components.MetricItem
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.SignalGreen
import com.example.ui.theme.SignalGreenContainer
import com.example.ui.theme.SignalGreenText
import com.example.ui.theme.WarningAmber

@Composable
fun MonitorScreen(
    cellDetails: CellDetails,
    onRefresh: () -> Unit,
    onNavigateToForce: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        onRefresh()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Location permission banner if cell data needs permission
        if (cellDetails.isSimulated && !hasPermission) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(WarningAmber, CyanPrimary))),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("permission_banner")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CellTower,
                                contentDescription = null,
                                tint = WarningAmber
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Permiso de Antenas Celulares",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Android requiere el permiso de ubicación precisa para leer el ID de antena (PCI), frecuencia EARFCN y portadores agregados del módem físico.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            modifier = Modifier.testTag("grant_permission_button")
                        ) {
                            Text("Habilitar Lectura Real de Antenas")
                        }
                    }
                }
            }
        }

        // Hero Status Card
        item {
            val isCa = cellDetails.isCarrierAggregationActive
            val gradientBrush = if (isCa) {
                Brush.verticalGradient(
                    colors = listOf(
                        SignalGreenContainer.copy(alpha = 0.8f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            } else {
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.surface
                    )
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.5.dp,
                        brush = if (isCa) Brush.horizontalGradient(listOf(SignalGreen, CyanPrimary))
                        else Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outline)),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .testTag("hero_status_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(gradientBrush)
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusBadge(isCaActive = isCa)

                            IconButton(
                                onClick = onRefresh,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                    .testTag("refresh_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Actualizar estado",
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = cellDetails.operatorName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Código de red: ${cellDetails.mccMnc} | Ranura SIM: ${cellDetails.simSlot}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Carrier aggregation state highlight
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (isCa) SignalGreenContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Icon(
                                imageVector = if (isCa) Icons.Default.Speed else Icons.Default.NetworkCell,
                                contentDescription = null,
                                tint = if (isCa) SignalGreen else CyanPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isCa) "Agregación de Portadores Activa" else "Portador Único (Sin 4G+)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isCa) SignalGreenText else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isCa) "${cellDetails.aggregatedCarriersCount} portadores combinados para máxima velocidad."
                                    else "El módem está en reposo. Activa el Forzador para despertar 4G+.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (!isCa) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onNavigateToForce,
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("activate_ca_shortcut_button")
                            ) {
                                Text(
                                    text = "⚡ Forzar Agregación LTE+ Ahora",
                                    color = Color(0xFF00363D),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Primary Serving Cell (PCell)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(CyanPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CellTower,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Portador Principal (PCell)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = cellDetails.primaryBandName,
                                style = MaterialTheme.typography.bodySmall,
                                color = CyanPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricItem(
                            label = "Ancho Banda",
                            value = "${cellDetails.bandwidthMhz.toInt()} MHz",
                            modifier = Modifier.weight(1f),
                            highlight = true
                        )
                        MetricItem(
                            label = "EARFCN",
                            value = "${cellDetails.earfcn}",
                            modifier = Modifier.weight(1f)
                        )
                        MetricItem(
                            label = "PCI (Cell ID)",
                            value = "${cellDetails.pci}",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricItem(
                            label = "Bajada (DL)",
                            value = "${cellDetails.downlinkMhz} MHz",
                            modifier = Modifier.weight(1f)
                        )
                        MetricItem(
                            label = "Subida (UL)",
                            value = "${cellDetails.uplinkMhz} MHz",
                            modifier = Modifier.weight(1f)
                        )
                        MetricItem(
                            label = "TAC (Área)",
                            value = "${cellDetails.tac}",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Secondary Serving Cells (SCell) - Carrier Aggregation
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Portadores Secundarios (SCell / Agregación)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Bandas auxiliares combinadas por el módem celular:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (cellDetails.secondaryCells.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sin portadores secundarios detectados. Usa la pestaña 'Forzar 4G+' para despertar los canales adicionales.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        cellDetails.secondaryCells.forEach { scell ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = if (scell.state.contains("ACTIVO")) SignalGreenContainer.copy(alpha = 0.35f)
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (scell.state.contains("ACTIVO")) SignalGreen.copy(alpha = 0.6f)
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Portador #${scell.scellIndex}: Banda ${scell.band}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (scell.state.contains("ACTIVO")) SignalGreenText else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "EARFCN ${scell.earfcn} | PCI: ${scell.pci} | ${scell.bandwidthMhz.toInt()} MHz",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = if (scell.state.contains("ACTIVO")) SignalGreen else MaterialTheme.colorScheme.outline,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = scell.state,
                                        color = if (scell.state.contains("ACTIVO")) Color(0xFF003920) else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // RF Quality & Signal Meters
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Calidad de Radiofrecuencia (RF)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricItem(
                            label = "RSRP (Potencia)",
                            value = "${cellDetails.rsrpDbm} dBm",
                            modifier = Modifier.weight(1f),
                            highlight = cellDetails.rsrpDbm > -90
                        )
                        MetricItem(
                            label = "RSRQ (Calidad)",
                            value = "${cellDetails.rsrqDb} dB",
                            modifier = Modifier.weight(1f)
                        )
                        MetricItem(
                            label = "RSSNR (Ruido)",
                            value = "${cellDetails.rssnrDb} dB",
                            modifier = Modifier.weight(1f),
                            highlight = cellDetails.rssnrDb > 15.0
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricItem(
                            label = "CQI (Canal)",
                            value = "${cellDetails.cqi}/15",
                            modifier = Modifier.weight(1f)
                        )
                        MetricItem(
                            label = "Timing Advance",
                            value = if (cellDetails.timingAdvance >= 0) "${cellDetails.timingAdvance}" else "N/A",
                            modifier = Modifier.weight(1f)
                        )
                        MetricItem(
                            label = "Nivel Señal",
                            value = "${cellDetails.signalLevel}/4 Barras",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Quick System Settings Actions
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Acciones Rápidas del Módem",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { SystemShortcuts.openRadioInfo(context) },
                            modifier = Modifier.weight(1f).testTag("open_radio_info_quick")
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("RadioInfo (*#*#4636#*#*)", fontSize = 11.sp, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = { SystemShortcuts.openAirplaneModeSettings(context) },
                            modifier = Modifier.weight(1f).testTag("reset_radio_quick")
                        ) {
                            Icon(Icons.Default.AirplanemodeActive, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reiniciar Módem", fontSize = 11.sp, maxLines = 1)
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
