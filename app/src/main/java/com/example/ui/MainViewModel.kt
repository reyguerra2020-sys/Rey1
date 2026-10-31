package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.CaKeepaliveEngine
import com.example.engine.KeepaliveStats
import com.example.engine.SpeedTestEngine
import com.example.engine.SpeedTestState
import com.example.engine.SystemShortcuts
import com.example.model.CaIntensity
import com.example.model.CellDetails
import com.example.model.LteBandDatabase
import com.example.telephony.TelephonyScanner
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    MONITOR("Monitor"),
    FORCE_CA("Forzar 4G+"),
    CODES_TOOLS("Herramientas"),
    COMBOS("Combos CA")
}

data class EarfcnCalcResult(
    val earfcn: Int = 1650,
    val band: Int = 3,
    val bandLabel: String = "B3 (1800 MHz DCS)",
    val downlinkMhz: Double = 1850.0,
    val uplinkMhz: Double = 1755.0,
    val duplex: String = "FDD"
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val telephonyScanner = TelephonyScanner(application)
    private val speedTestEngine = SpeedTestEngine()
    private val caKeepaliveEngine = CaKeepaliveEngine { isForced ->
        telephonyScanner.setSimulationCaState(isForced)
        telephonyScanner.refreshNetworkInfo()
    }

    val cellDetails: StateFlow<CellDetails> = telephonyScanner.cellDetails
    val keepaliveStats: StateFlow<KeepaliveStats> = caKeepaliveEngine.stats
    val speedTestState: StateFlow<SpeedTestState> = speedTestEngine.state

    private val _currentTab = MutableStateFlow(AppTab.MONITOR)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _earfcnInput = MutableStateFlow("1650")
    val earfcnInput: StateFlow<String> = _earfcnInput.asStateFlow()

    private val _earfcnResult = MutableStateFlow(EarfcnCalcResult())
    val earfcnResult: StateFlow<EarfcnCalcResult> = _earfcnResult.asStateFlow()

    private val _detectedBrand = MutableStateFlow(SystemShortcuts.getDetectedBrand())
    val detectedBrand: StateFlow<String> = _detectedBrand.asStateFlow()

    init {
        refreshNetworkInfo()
        calculateEarfcn("1650")

        // Periodic background poll for cell info updates
        viewModelScope.launch {
            while (isActive) {
                delay(3000)
                telephonyScanner.refreshNetworkInfo()
            }
        }
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun refreshNetworkInfo() {
        telephonyScanner.refreshNetworkInfo()
    }

    fun toggleCaForce() {
        caKeepaliveEngine.toggle()
    }

    fun setCaIntensity(intensity: CaIntensity) {
        caKeepaliveEngine.setIntensity(intensity)
    }

    fun setCaDurationMinutes(minutes: Int) {
        caKeepaliveEngine.setDuration(minutes)
    }

    fun runSpeedTest() {
        val isCa = cellDetails.value.isCarrierAggregationActive || keepaliveStats.value.isRunning
        speedTestEngine.runTest(isCa)
    }

    fun resetSpeedTest() {
        speedTestEngine.reset()
    }

    fun onEarfcnInputChanged(input: String) {
        _earfcnInput.value = input.filter { it.isDigit() }
        calculateEarfcn(_earfcnInput.value)
    }

    private fun calculateEarfcn(raw: String) {
        val earfcn = raw.toIntOrNull() ?: 0
        if (earfcn > 0) {
            val (band, label) = LteBandDatabase.getBandFromEarfcn(earfcn)
            val (dl, ul) = LteBandDatabase.calculateFrequencies(earfcn)
            val bandInfo = LteBandDatabase.getBandInfo(band)
            _earfcnResult.value = EarfcnCalcResult(
                earfcn = earfcn,
                band = band,
                bandLabel = label,
                downlinkMhz = dl,
                uplinkMhz = ul,
                duplex = bandInfo.duplex
            )
        }
    }
}
