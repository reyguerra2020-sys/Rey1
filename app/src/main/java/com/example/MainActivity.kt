package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.SystemShortcuts
import com.example.ui.AppTab
import com.example.ui.MainViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.screens.CombosScreen
import com.example.ui.screens.ForceCaScreen
import com.example.ui.screens.MonitorScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SignalGreen
import com.example.ui.theme.SignalGreenContainer
import com.example.ui.theme.SignalGreenText

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val cellDetails by viewModel.cellDetails.collectAsStateWithLifecycle()
    val keepaliveStats by viewModel.keepaliveStats.collectAsStateWithLifecycle()
    val speedTestState by viewModel.speedTestState.collectAsStateWithLifecycle()
    val detectedBrand by viewModel.detectedBrand.collectAsStateWithLifecycle()
    val earfcnInput by viewModel.earfcnInput.collectAsStateWithLifecycle()
    val earfcnResult by viewModel.earfcnResult.collectAsStateWithLifecycle()

    val isCaActive = cellDetails.isCarrierAggregationActive || keepaliveStats.isRunning

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    if (isCaActive) SignalGreen else CyanPrimary,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isCaActive) Icons.Default.Bolt else Icons.Default.CellTower,
                                contentDescription = null,
                                tint = Color(0xFF00363D),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FORCE 4G+ LTE",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { SystemShortcuts.openRadioInfo(context) },
                        modifier = Modifier.testTag("topbar_radio_info_action")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Abrir RadioInfo (*#*#4636#*#*)",
                            tint = CyanPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.MONITOR,
                    onClick = { viewModel.selectTab(AppTab.MONITOR) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CellTower,
                            contentDescription = "Monitor"
                        )
                    },
                    label = { Text("Monitor", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00363D),
                        selectedTextColor = CyanPrimary,
                        indicatorColor = CyanPrimary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("tab_monitor")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.FORCE_CA,
                    onClick = { viewModel.selectTab(AppTab.FORCE_CA) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Forzar 4G+"
                        )
                    },
                    label = { Text("Forzar 4G+", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00363D),
                        selectedTextColor = SignalGreenText,
                        indicatorColor = if (isCaActive) SignalGreen else CyanPrimary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("tab_force_ca")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.CODES_TOOLS,
                    onClick = { viewModel.selectTab(AppTab.CODES_TOOLS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Herramientas"
                        )
                    },
                    label = { Text("Menús & Bandas", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00363D),
                        selectedTextColor = CyanPrimary,
                        indicatorColor = CyanPrimary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("tab_tools")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.COMBOS,
                    onClick = { viewModel.selectTab(AppTab.COMBOS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Combos & Test"
                        )
                    },
                    label = { Text("Combos & Test", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00363D),
                        selectedTextColor = CyanPrimary,
                        indicatorColor = CyanPrimary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("tab_combos")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentTab) {
                AppTab.MONITOR -> MonitorScreen(
                    cellDetails = cellDetails,
                    onRefresh = { viewModel.refreshNetworkInfo() },
                    onNavigateToForce = { viewModel.selectTab(AppTab.FORCE_CA) }
                )
                AppTab.FORCE_CA -> ForceCaScreen(
                    cellDetails = cellDetails,
                    keepaliveStats = keepaliveStats,
                    onToggleCa = { viewModel.toggleCaForce() },
                    onSetIntensity = { viewModel.setCaIntensity(it) },
                    onSetDuration = { viewModel.setCaDurationMinutes(it) }
                )
                AppTab.CODES_TOOLS -> ToolsScreen(
                    detectedBrand = detectedBrand,
                    earfcnInput = earfcnInput,
                    earfcnResult = earfcnResult,
                    onEarfcnChanged = { viewModel.onEarfcnInputChanged(it) }
                )
                AppTab.COMBOS -> CombosScreen(
                    cellDetails = cellDetails,
                    speedTestState = speedTestState,
                    onRunSpeedTest = { viewModel.runSpeedTest() },
                    onResetSpeedTest = { viewModel.resetSpeedTest() }
                )
            }
        }
    }
}
