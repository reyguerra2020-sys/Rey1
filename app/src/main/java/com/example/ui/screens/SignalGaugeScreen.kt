package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CellDetails
import com.example.ui.SignalSample
import com.example.ui.components.MetricItem
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.SignalGreen
import com.example.ui.theme.SignalGreenContainer
import com.example.ui.theme.SignalGreenText
import com.example.ui.theme.WarningAmber
import kotlin.math.cos
import kotlin.math.sin

enum class SignalMetricType(
    val title: String,
    val unit: String,
    val minVal: Float,
    val maxVal: Float,
    val description: String
) {
    RSRP(
        title = "RSRP",
        unit = "dBm",
        minVal = -140f,
        maxVal = -44f,
        description = "Reference Signal Received Power: Potencia absoluta recibida de los símbolos de referencia de la torre LTE. Determina la intensidad de cobertura celular."
    ),
    RSRQ(
        title = "RSRQ",
        unit = "dB",
        minVal = -20f,
        maxVal = -3f,
        description = "Reference Signal Received Quality: Calidad de señal recibida, considerando la interferencia cocanal y la carga de tráfico de la celda vecina."
    ),
    SINR(
        title = "SINR",
        unit = "dB",
        minVal = -10f,
        maxVal = 30f,
        description = "Signal-to-Interference-plus-Noise Ratio: Relación entre la señal útil y el ruido/interferencia. Es el factor decisivo para modular en 256QAM y activar 4G+."
    )
}

enum class SignalQuality(val label: String, val color: Color) {
    EXCELLENT("EXCELENTE", Color(0xFF00E676)),
    GOOD("BUENO", Color(0xFF00E5FF)),
    FAIR("REGULAR", Color(0xFFFFB300)),
    POOR("DEFICIENTE", Color(0xFFFF5252))
}

@Composable
fun SignalGaugeScreen(
    cellDetails: CellDetails,
    signalHistory: List<SignalSample>,
    onRefresh: () -> Unit,
    onNavigateToForce: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMetricIndex by remember { mutableIntStateOf(0) }
    var isHeroView by remember { mutableStateOf(false) }

    val rsrpVal = if (cellDetails.rsrpDbm != -999) cellDetails.rsrpDbm.toFloat() else -85f
    val rsrqVal = if (cellDetails.rsrqDb != -999) cellDetails.rsrqDb.toFloat() else -10f
    val sinrVal = if (cellDetails.rssnrDb != -999.0) cellDetails.rssnrDb.toFloat() else 18f

    val rsrpQuality = getRsrpQuality(rsrpVal)
    val rsrqQuality = getRsrqQuality(rsrqVal)
    val sinrQuality = getSinrQuality(sinrVal)

    // Calculate composite link quality score (0 - 100%)
    val rsrpNorm = ((rsrpVal - (-130f)) / (-50f - (-130f))).coerceIn(0f, 1f)
    val rsrqNorm = ((rsrqVal - (-19f)) / (-5f - (-19f))).coerceIn(0f, 1f)
    val sinrNorm = ((sinrVal - (-5f)) / (25f - (-5f))).coerceIn(0f, 1f)
    val overallScore = ((rsrpNorm * 0.35f + rsrqNorm * 0.30f + sinrNorm * 0.35f) * 100).toInt()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Header Card with Live Status & Refresh
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
                    .testTag("signal_header_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(CyanPrimary.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "MONITOR RF EN TIEMPO REAL",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "${cellDetails.operatorName} • ${cellDetails.primaryBandName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CyanPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .size(38.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            .testTag("signal_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar mediciones de señal",
                            tint = CyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // View Mode Switcher Tab
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (!isHeroView) CyanPrimary else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { isHeroView = false }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "3 Indicadores (Vista Resumen)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (!isHeroView) Color(0xFF00363D) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isHeroView) CyanPrimary else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { isHeroView = true }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Indicador Heroico (Detalle)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isHeroView) Color(0xFF00363D) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Main Gauges Display
        if (!isHeroView) {
            // Triple Gauge Overview Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth().testTag("triple_gauge_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Medidores de Radiofrecuencia 4G+",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Lectura continua de potencia, calidad y relación señal/ruido",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3 Visual Gauges side-by-side
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            VisualCircularGauge(
                                title = "RSRP",
                                value = rsrpVal,
                                unit = "dBm",
                                minVal = -140f,
                                maxVal = -44f,
                                quality = rsrpQuality,
                                sizeDp = 100,
                                modifier = Modifier.weight(1f)
                            )

                            VisualCircularGauge(
                                title = "RSRQ",
                                value = rsrqVal,
                                unit = "dB",
                                minVal = -20f,
                                maxVal = -3f,
                                quality = rsrqQuality,
                                sizeDp = 100,
                                modifier = Modifier.weight(1f)
                            )

                            VisualCircularGauge(
                                title = "SINR",
                                value = sinrVal,
                                unit = "dB",
                                minVal = -10f,
                                maxVal = 30f,
                                quality = sinrQuality,
                                sizeDp = 100,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Composite Index Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Índice de Enlace LTE-A",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$overallScore% de Eficiencia RF",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (overallScore >= 75) SignalGreenText else CyanPrimary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (overallScore >= 75) SignalGreen.copy(alpha = 0.2f) else CyanPrimary.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (overallScore >= 75) "ÓPTIMO PARA 4G+" else "MODERADO",
                                    color = if (overallScore >= 75) SignalGreenText else CyanPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Hero Gauge View with Tab Switcher for RSRP, RSRQ, SINR
            item {
                val currentMetric = SignalMetricType.values()[selectedMetricIndex]
                val (currentVal, currentQuality) = when (currentMetric) {
                    SignalMetricType.RSRP -> Pair(rsrpVal, rsrpQuality)
                    SignalMetricType.RSRQ -> Pair(rsrqVal, rsrqQuality)
                    SignalMetricType.SINR -> Pair(sinrVal, sinrQuality)
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth().testTag("hero_gauge_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TabRow(
                            selectedTabIndex = selectedMetricIndex,
                            containerColor = Color.Transparent,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedMetricIndex]),
                                    color = CyanPrimary
                                )
                            }
                        ) {
                            SignalMetricType.values().forEachIndexed { index, metric ->
                                Tab(
                                    selected = selectedMetricIndex == index,
                                    onClick = { selectedMetricIndex = index },
                                    text = {
                                        Text(
                                            text = metric.title,
                                            fontWeight = if (selectedMetricIndex == index) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selectedMetricIndex == index) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Big Hero Gauge
                        VisualCircularGauge(
                            title = currentMetric.title,
                            value = currentVal,
                            unit = currentMetric.unit,
                            minVal = currentMetric.minVal,
                            maxVal = currentMetric.maxVal,
                            quality = currentQuality,
                            sizeDp = 180,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Technical Explanation Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(14.dp)
                        ) {
                            Text(
                                text = currentMetric.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // Live Signal Trend Graph (History)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().testTag("signal_history_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Historial en Vivo (Últimas Muestras)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "RSRP (dBm)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    SignalTrendChart(
                        samples = signalHistory,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "-30s",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Ahora",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Quality Standard Reference Table Card
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
                            text = "Escala Estándar 3GPP para 4G+",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    ThresholdRow(
                        metricName = "RSRP (Potencia)",
                        ranges = listOf(
                            Pair(">= -80", Color(0xFF00E676)),
                            Pair("-80 a -90", Color(0xFF00E5FF)),
                            Pair("-90 a -100", Color(0xFFFFB300)),
                            Pair("< -100", Color(0xFFFF5252))
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ThresholdRow(
                        metricName = "RSRQ (Calidad)",
                        ranges = listOf(
                            Pair(">= -10", Color(0xFF00E676)),
                            Pair("-10 a -15", Color(0xFF00E5FF)),
                            Pair("-15 a -19", Color(0xFFFFB300)),
                            Pair("< -19", Color(0xFFFF5252))
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ThresholdRow(
                        metricName = "SINR (Señal/Ruido)",
                        ranges = listOf(
                            Pair(">= 20 dB", Color(0xFF00E676)),
                            Pair("13 a 20 dB", Color(0xFF00E5FF)),
                            Pair("0 a 13 dB", Color(0xFFFFB300)),
                            Pair("< 0 dB", Color(0xFFFF5252))
                        )
                    )
                }
            }
        }

        // Quick Navigation to Force 4G+
        item {
            Button(
                onClick = onNavigateToForce,
                colors = ButtonDefaults.buttonColors(containerColor = SignalGreen),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().testTag("signal_go_to_force_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = Color(0xFF003920),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Forzar Portadores Secundarios (4G+)",
                    color = Color(0xFF003920),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun VisualCircularGauge(
    title: String,
    value: Float,
    unit: String,
    minVal: Float,
    maxVal: Float,
    quality: SignalQuality,
    sizeDp: Int,
    modifier: Modifier = Modifier
) {
    val normFraction = ((value - minVal) / (maxVal - minVal)).coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = normFraction,
        animationSpec = tween(durationMillis = 600),
        label = "gauge_animation"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(sizeDp.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = size.width * 0.10f
                val arcSize = size.width - strokeWidth
                val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                val startAngle = 145f
                val totalSweep = 250f

                // Draw Track Background
                drawArc(
                    color = Color(0xFF2A3438),
                    startAngle = startAngle,
                    sweepAngle = totalSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Draw Value Sweep Arc
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to Color(0xFFFF5252),
                        0.35f to Color(0xFFFFB300),
                        0.65f to Color(0xFF00E5FF),
                        1.0f to Color(0xFF00E676)
                    ),
                    startAngle = startAngle,
                    sweepAngle = totalSweep * animatedFraction,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Indicator needle/circle at endpoint
                val currentAngleRad = Math.toRadians((startAngle + totalSweep * animatedFraction).toDouble())
                val radius = arcSize / 2f
                val centerX = size.width / 2f
                val centerY = size.height / 2f
                val indicatorX = centerX + (radius * cos(currentAngleRad)).toFloat()
                val indicatorY = centerY + (radius * sin(currentAngleRad)).toFloat()

                drawCircle(
                    color = Color.White,
                    radius = strokeWidth * 0.7f,
                    center = Offset(indicatorX, indicatorY)
                )
                drawCircle(
                    color = quality.color,
                    radius = strokeWidth * 0.45f,
                    center = Offset(indicatorX, indicatorY)
                )
            }

            // Center Digital Readout
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = (sizeDp * 0.1f).dp)
            ) {
                Text(
                    text = String.format("%.1f", value).replace(".0", ""),
                    fontSize = if (sizeDp > 130) 28.sp else 16.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = unit,
                    fontSize = if (sizeDp > 130) 12.sp else 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Quality Badge Pill
        Box(
            modifier = Modifier
                .background(quality.color.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = quality.label,
                color = quality.color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SignalTrendChart(
    samples: List<SignalSample>,
    modifier: Modifier = Modifier
) {
    val displaySamples = if (samples.isEmpty()) {
        List(15) { SignalSample(rsrp = -85 + (it % 3) * 2 - 2) }
    } else samples

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Draw horizontal grid lines
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = height * (i.toFloat() / gridLines)
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
        }

        if (displaySamples.size < 2) return@Canvas

        val minVal = -120f
        val maxVal = -60f
        val xStep = width / (displaySamples.size - 1).coerceAtLeast(1)

        val path = Path()
        val fillPath = Path()

        displaySamples.forEachIndexed { index, sample ->
            val norm = ((sample.rsrp - minVal) / (maxVal - minVal)).coerceIn(0f, 1f)
            val y = height - (norm * height)
            val x = index * xStep

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }

            // Draw point circle
            drawCircle(
                color = CyanPrimary,
                radius = 3.dp.toPx(),
                center = Offset(x, y)
            )
        }

        fillPath.lineTo(width, height)
        fillPath.close()

        // Draw gradient under curve
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    CyanPrimary.copy(alpha = 0.35f),
                    CyanPrimary.copy(alpha = 0.0f)
                )
            )
        )

        // Draw trend line
        drawPath(
            path = path,
            color = CyanPrimary,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun ThresholdRow(
    metricName: String,
    ranges: List<Pair<String, Color>>
) {
    Column {
        Text(
            text = metricName,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ranges.forEach { (range, color) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = range,
                        color = color,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

fun getRsrpQuality(rsrp: Float): SignalQuality {
    return when {
        rsrp >= -80f -> SignalQuality.EXCELLENT
        rsrp >= -90f -> SignalQuality.GOOD
        rsrp >= -100f -> SignalQuality.FAIR
        else -> SignalQuality.POOR
    }
}

fun getRsrqQuality(rsrq: Float): SignalQuality {
    return when {
        rsrq >= -10f -> SignalQuality.EXCELLENT
        rsrq >= -15f -> SignalQuality.GOOD
        rsrq >= -19f -> SignalQuality.FAIR
        else -> SignalQuality.POOR
    }
}

fun getSinrQuality(sinr: Float): SignalQuality {
    return when {
        sinr >= 20f -> SignalQuality.EXCELLENT
        sinr >= 13f -> SignalQuality.GOOD
        sinr >= 0f -> SignalQuality.FAIR
        else -> SignalQuality.POOR
    }
}
