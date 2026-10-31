package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.KeepaliveStats
import com.example.engine.SystemShortcuts
import com.example.model.CaIntensity
import com.example.model.CellDetails
import com.example.shizuku.ShizukuStatus
import com.example.ui.components.MetricItem
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.SignalGreen
import com.example.ui.theme.SignalGreenContainer
import com.example.ui.theme.SignalGreenText
import com.example.ui.theme.WarningAmber

@Composable
fun ForceCaScreen(
    cellDetails: CellDetails,
    keepaliveStats: KeepaliveStats,
    shizukuStatus: ShizukuStatus,
    onToggleCa: () -> Unit,
    onSetIntensity: (CaIntensity) -> Unit,
    onRequestShizukuPermission: () -> Unit,
    onOpenShizukuApp: () -> Unit,
    onExecuteShizukuLteOnly: () -> Unit,
    onExecuteShizukuNrLte: () -> Unit,
    onExecuteShizukuModemReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isRunning = keepaliveStats.isRunning

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Master Force Switch Card
        item {
            val bgBrush = if (isRunning) {
                Brush.verticalGradient(
                    listOf(
                        SignalGreenContainer.copy(alpha = 0.9f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            } else {
                Brush.verticalGradient(
                    listOf(
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
                        width = 2.dp,
                        brush = if (isRunning) Brush.horizontalGradient(listOf(SignalGreen, CyanPrimary))
                        else Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outline)),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .testTag("force_ca_master_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bgBrush)
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(
                                            if (isRunning) SignalGreen else CyanPrimary.copy(alpha = 0.2f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = if (isRunning) Color(0xFF003920) else CyanPrimary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "FORZADOR LTE+ / 4G+",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isRunning) "SERVICIO PERMANENTE ACTIVO" else "INACTIVO",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isRunning) SignalGreenText else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = isRunning,
                                onCheckedChange = { onToggleCa() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = SignalGreen,
                                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.testTag("force_ca_toggle_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Background Service persistence badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (isRunning) SignalGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = if (isRunning) SignalGreenText else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Foreground Service Activo: No se desconecta al cerrar la app o apagar la pantalla.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isRunning) SignalGreenText else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = keepaliveStats.statusMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (isRunning) {
                            Spacer(modifier = Modifier.height(14.dp))
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth().height(4.dp),
                                color = SignalGreen,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MetricItem(
                                    label = "Ráfagas Enviadas",
                                    value = "#${keepaliveStats.burstsCount}",
                                    modifier = Modifier.weight(1f),
                                    highlight = true
                                )
                                MetricItem(
                                    label = "Latencia Módem",
                                    value = "${keepaliveStats.lastLatencyMs} ms",
                                    modifier = Modifier.weight(1f)
                                )
                                MetricItem(
                                    label = "Tiempo Activo",
                                    value = "${keepaliveStats.elapsedSeconds / 60}m ${keepaliveStats.elapsedSeconds % 60}s",
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val transferredKb = keepaliveStats.totalBytesTransferred / 1024
                            val transferredMb = transferredKb / 1024.0
                            Text(
                                text = "Consumo acumulado: ${if (transferredMb >= 1.0) String.format("%.2f MB", transferredMb) else "$transferredKb KB"} (Notificación continua activa)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Shizuku Integration Card (Elevated ADB Permissions without root)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.5.dp,
                        color = if (shizukuStatus.isPermissionGranted) SignalGreen else CyanPrimary,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .testTag("shizuku_integration_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (shizukuStatus.isPermissionGranted) SignalGreen.copy(alpha = 0.2f) else CyanPrimary.copy(alpha = 0.2f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Terminal,
                                    contentDescription = null,
                                    tint = if (shizukuStatus.isPermissionGranted) SignalGreen else CyanPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Permiso Shizuku (ADB Shell)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Comandos de red elevados a nivel de sistema",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Shizuku status pill
                        Box(
                            modifier = Modifier
                                .background(
                                    color = when {
                                        shizukuStatus.isPermissionGranted -> SignalGreen.copy(alpha = 0.2f)
                                        shizukuStatus.isRunning -> WarningAmber.copy(alpha = 0.2f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = when {
                                    shizukuStatus.isPermissionGranted -> "CONECTADO"
                                    shizukuStatus.isRunning -> "AUTORIZAR"
                                    else -> "NO DETECTADO"
                                },
                                color = when {
                                    shizukuStatus.isPermissionGranted -> SignalGreenText
                                    shizukuStatus.isRunning -> WarningAmber
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Con Shizuku, la aplicación puede enviar comandos ADB al subsistema de telefonía ('cmd phone') para forzar la red a solo LTE o NR/5G sin requerir root.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!shizukuStatus.isPermissionGranted) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (shizukuStatus.isRunning) {
                                Button(
                                    onClick = onRequestShizukuPermission,
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("request_shizuku_perm_button")
                                ) {
                                    Text(
                                        text = "Conceder Permiso Shizuku",
                                        color = Color(0xFF00363D),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            } else {
                                Button(
                                    onClick = onOpenShizukuApp,
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("open_shizuku_app_button")
                                ) {
                                    Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF00363D))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Abrir / Iniciar Shizuku",
                                        color = Color(0xFF00363D),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    } else {
                        // Elevated Shizuku Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onExecuteShizukuLteOnly,
                                colors = ButtonDefaults.buttonColors(containerColor = SignalGreen),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("shizuku_lte_only_button")
                            ) {
                                Text(
                                    text = "Fijar 'LTE Only' (ADB)",
                                    color = Color(0xFF003920),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }

                            OutlinedButton(
                                onClick = onExecuteShizukuNrLte,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("shizuku_nr_lte_button")
                            ) {
                                Text(
                                    text = "Fijar 'NR/LTE' (5G)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = onExecuteShizukuModemReset,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("shizuku_reset_modem_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Reiniciar Módem Celular vía Shizuku", fontSize = 11.sp)
                        }

                        if (shizukuStatus.lastOutput.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "Consola Shizuku: ${shizukuStatus.lastOutput}",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (shizukuStatus.lastCommandSuccess == true) SignalGreenText else CyanPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // How it works technical explanation
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "¿Cómo fuerza el 4G+ / LTE+?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "1. Módems y antenas celulares (eNodeB) desactivan los portadores secundarios (SCELL) en milisegundos cuando estás inactivo para no agotar la batería.\n" +
                               "2. Al activar este Forzador, el Foreground Service envía micro-ráfagas controladas de paquetes en segundo plano que mantienen el módem en estado RRC_CONNECTED sin importar si cierras la app.\n" +
                               "3. El planificador de la antena detecta demanda de red continua y mantiene el enlace LTE-Advanced (4G+) acoplado permanentemente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Intensity Configuration
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Intensidad de Forzado de Portadores",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Frecuencia de las ráfagas para activar el agregador:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    CaIntensity.values().forEach { intensity ->
                        val isSelected = keepaliveStats.intensity == intensity
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (isSelected) CyanPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onSetIntensity(intensity) }
                                .padding(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(
                                        if (isSelected) CyanPrimary else Color.Transparent,
                                        CircleShape
                                    )
                                    .border(2.dp, if (isSelected) CyanPrimary else MaterialTheme.colorScheme.outline, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = intensity.displayName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = intensity.description,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // System Modem Lock (RadioInfo Shortcut)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Bloquear 'Solo LTE' (RadioInfo)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "En el Menú de Pruebas nativo de Android puedes fijar el tipo de red en 'LTE Only' o 'NR/LTE'. Esto evita que tu teléfono se degrade a redes lentas 3G (HSPA) o 2G (EDGE) cuando la señal LTE+ fluctúa.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { SystemShortcuts.openRadioInfo(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("open_radio_info_button")
                    ) {
                        Text(
                            text = "Abrir RadioInfo (*#*#4636#*#*)",
                            color = Color(0xFF00363D),
                            fontWeight = FontWeight.Bold
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
