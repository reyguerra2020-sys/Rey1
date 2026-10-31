package com.example.ui.screens

import android.content.Context
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.engine.SystemShortcuts
import com.example.model.CellDetails
import com.example.model.LteBandDatabase
import com.example.model.LteBandInfo
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.SignalGreen
import com.example.ui.theme.SignalGreenContainer
import com.example.ui.theme.SignalGreenText
import com.example.ui.theme.WarningAmber

@Composable
fun BandLockScreen(
    cellDetails: CellDetails,
    detectedBrand: String,
    isSingleBandMode: Boolean,
    selectedSingleBand: Int,
    selectedBands: Set<Int>,
    onSetMode: (Boolean) -> Unit,
    onSelectSingleBand: (Int) -> Unit,
    onToggleBand: (Int) -> Unit,
    onApplyPreset: (Collection<Int>) -> Unit,
    onSelectAllBands: () -> Unit,
    onClearBands: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allBands = remember { LteBandDatabase.allBands }

    // Calculate modem hex mask and AT command
    val effectiveBands = if (isSingleBandMode) setOf(selectedSingleBand) else selectedBands
    val sortedBands = effectiveBands.sorted()
    val atCommand = "AT+QNWPREFCFG=\"lte_band\",${sortedBands.joinToString(":")}"

    var hexMask = 0L
    for (b in effectiveBands) {
        if (b in 1..64) {
            hexMask = hexMask or (1L shl (b - 1))
        }
    }
    val hexMaskStr = "0x" + hexMask.toString(16).uppercase()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Header Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.5.dp,
                        brush = Brush.horizontalGradient(listOf(CyanPrimary, SignalGreen)),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .testTag("band_lock_header_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(CyanPrimary.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "BLOQUEO DE BANDAS LTE",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Fuerza al módem a operar exclusivamente en frecuencias rápidas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Selection Mode Switcher (Radio Button vs Checkbox Group)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().testTag("selection_mode_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tipo de Bloqueo de Frecuencia",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Radio Buttons Mode button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSingleBandMode) CyanPrimary else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onSetMode(true) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Banda Única (Radio)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isSingleBandMode) Color(0xFF00363D) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Checkbox Group Mode button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (!isSingleBandMode) CyanPrimary else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onSetMode(false) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Multibanda 4G+ (Check)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (!isSingleBandMode) Color(0xFF00363D) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isSingleBandMode)
                            "• Modo Radio Button: Bloquea el módem a una sola banda fija. Ideal para descartar bandas congestionadas."
                        else
                            "• Modo Checkbox Group: Selecciona 2 o más bandas para forzar Carrier Aggregation (LTE+). El módem combinará solo las bandas marcadas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Quick Preset Buttons
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Preajustes Rápidos de Bandas",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetChip(
                            label = "Alta Vel (B3+B7)",
                            isSelected = effectiveBands == setOf(3, 7),
                            onClick = {
                                onSetMode(false)
                                onApplyPreset(listOf(3, 7))
                            },
                            modifier = Modifier.weight(1f)
                        )

                        PresetChip(
                            label = "B7 + B28",
                            isSelected = effectiveBands == setOf(7, 28),
                            onClick = {
                                onSetMode(false)
                                onApplyPreset(listOf(7, 28))
                            },
                            modifier = Modifier.weight(1f)
                        )

                        PresetChip(
                            label = "B2 + B4 (América)",
                            isSelected = effectiveBands == setOf(2, 4),
                            onClick = {
                                onSetMode(false)
                                onApplyPreset(listOf(2, 4))
                            },
                            modifier = Modifier.weight(1.1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetChip(
                            label = "Todas (Automático)",
                            isSelected = effectiveBands.size > 8,
                            onClick = {
                                onSetMode(false)
                                onSelectAllBands()
                            },
                            modifier = Modifier.weight(1f)
                        )

                        PresetChip(
                            label = "Solo B3 (1800)",
                            isSelected = isSingleBandMode && selectedSingleBand == 3,
                            onClick = {
                                onSetMode(true)
                                onSelectSingleBand(3)
                            },
                            modifier = Modifier.weight(1f)
                        )

                        PresetChip(
                            label = "Solo B7 (2600)",
                            isSelected = isSingleBandMode && selectedSingleBand == 7,
                            onClick = {
                                onSetMode(true)
                                onSelectSingleBand(7)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Applied Bands Summary & Action Button
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyanPrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .testTag("apply_band_lock_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Bandas Seleccionadas:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (effectiveBands.isEmpty()) "Ninguna" else effectiveBands.sorted().joinToString(", ") { "B$it" },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = SignalGreenText
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(SignalGreen.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isSingleBandMode) "1 BANDA FIJA" else "${effectiveBands.size} BANDAS (4G+)",
                                color = SignalGreenText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action buttons to apply to hardware modem
                    Button(
                        onClick = {
                            if (detectedBrand.contains("samsung", ignoreCase = true)) {
                                SystemShortcuts.openSamsungBandSelection(context)
                            } else {
                                SystemShortcuts.copyToClipboard(context, hexMaskStr, "Máscara de Banda LTE")
                                SystemShortcuts.openRadioInfo(context)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("apply_band_lock_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Launch,
                            contentDescription = null,
                            tint = Color(0xFF00363D),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (detectedBrand.contains("samsung", ignoreCase = true))
                                "Abrir Selección de Bandas Samsung (*#2263#)"
                            else
                                "Aplicar al Módem (RadioInfo / Menú de Red)",
                            color = Color(0xFF00363D),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Modem Mask info box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Máscara NVRAM / AT Command:",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$hexMaskStr | $atCommand",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = CyanPrimary,
                                maxLines = 1
                            )
                        }

                        IconButton(
                            onClick = {
                                SystemShortcuts.copyToClipboard(context, atCommand, "Comando AT Módem")
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copiar comando AT",
                                tint = CyanPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Band Selection List (Radio button or Checkbox items)
        item {
            Text(
                text = "Catálogo de Bandas LTE 3GPP (${if (isSingleBandMode) "Selección Única" else "Selección Múltiple"})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(allBands.size) { index ->
            val band = allBands[index]
            val isCurrentPrimary = cellDetails.primaryBand == band.band
            val isCurrentSecondary = cellDetails.secondaryCells.any { it.band == band.band }
            val isSelected = if (isSingleBandMode) {
                selectedSingleBand == band.band
            } else {
                selectedBands.contains(band.band)
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) CyanPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable {
                        if (isSingleBandMode) {
                            onSelectSingleBand(band.band)
                        } else {
                            onToggleBand(band.band)
                        }
                    }
                    .testTag("band_item_${band.band}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Radio button or Checkbox control
                    if (isSingleBandMode) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onSelectSingleBand(band.band) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = CyanPrimary,
                                unselectedColor = MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier.testTag("radio_band_${band.band}")
                        )
                    } else {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onToggleBand(band.band) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = CyanPrimary,
                                checkmarkColor = Color(0xFF00363D),
                                uncheckedColor = MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier.testTag("checkbox_band_${band.band}")
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Banda ${band.band}",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = band.duplex,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isCurrentPrimary) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(SignalGreen.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "PORTADOR ACTIVO (PCell)",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SignalGreenText
                                    )
                                }
                            } else if (isCurrentSecondary) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(SignalGreen.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "AGREGADO (SCell)",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SignalGreenText
                                    )
                                }
                            }
                        }

                        Text(
                            text = band.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "DL: ${band.dlFreqRange} | UL: ${band.ulFreqRange}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = band.commonUsage,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
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

@Composable
fun PresetChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color(0xFF00363D) else MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}
