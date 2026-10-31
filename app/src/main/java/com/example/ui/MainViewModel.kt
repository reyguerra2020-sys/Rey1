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
    SIGNAL("Señal RF"),
    FORCE_CA("Forzar 4G+"),
    BAND_LOCK("Bandas"),
    TOOLS("Herramientas")
}

data class SignalSample(
    val timestamp: Long = System.currentTimeMillis(),
    val rsrp: Int = -85,
    val rsrq: Int = -10,
    val rssnr: Double = 18.0
)

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
    private val caKeepaliveEngine = CaKeepaliveEngine.shared.apply {
        setOnCaStateChangedListener { isForced ->
            telephonyScanner.setSimulationCaState(isForced)
            telephonyScanner.refreshNetworkInfo()
        }
    }

    val cellDetails: StateFlow<CellDetails> = telephonyScanner.cellDetails
    val keepaliveStats: StateFlow<KeepaliveStats> = caKeepaliveEngine.stats
    val speedTestState: StateFlow<SpeedTestState> = speedTestEngine.state
    val shizukuStatus: StateFlow<com.example.shizuku.ShizukuStatus> = com.example.shizuku.ShizukuManager.status

    private val _currentTab = MutableStateFlow(AppTab.MONITOR)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _earfcnInput = MutableStateFlow("1650")
    val earfcnInput: StateFlow<String> = _earfcnInput.asStateFlow()

    private val _earfcnResult = MutableStateFlow(EarfcnCalcResult())
    val earfcnResult: StateFlow<EarfcnCalcResult> = _earfcnResult.asStateFlow()

    private val _detectedBrand = MutableStateFlow(SystemShortcuts.getDetectedBrand())
    val detectedBrand: StateFlow<String> = _detectedBrand.asStateFlow()

    // Signal History for Visual Gauge and live trend chart
    private val _signalHistory = MutableStateFlow<List<SignalSample>>(emptyList())
    val signalHistory: StateFlow<List<SignalSample>> = _signalHistory.asStateFlow()

    // Band Locking interface state (Radio button single-select or Checkbox multi-select)
    private val _isSingleBandMode = MutableStateFlow(false) // false = Checkbox group (Multi-band CA), true = RadioButton group (Single band)
    val isSingleBandMode: StateFlow<Boolean> = _isSingleBandMode.asStateFlow()

    private val _selectedSingleBand = MutableStateFlow(3) // For RadioButton mode (e.g. Band 3)
    val selectedSingleBand: StateFlow<Int> = _selectedSingleBand.asStateFlow()

    private val _selectedBands = MutableStateFlow<Set<Int>>(setOf(3, 7)) // For Checkbox group (e.g. B3 + B7 for 4G+ CA)
    val selectedBands: StateFlow<Set<Int>> = _selectedBands.asStateFlow()

    init {
        refreshNetworkInfo()
        calculateEarfcn("1650")
        com.example.shizuku.ShizukuManager.init()

        // Periodic background poll for cell info updates and signal samples
        viewModelScope.launch {
            while (isActive) {
                telephonyScanner.refreshNetworkInfo()
                val details = telephonyScanner.cellDetails.value
                val rsrp = if (details.rsrpDbm != -999) details.rsrpDbm else -85
                val rsrq = if (details.rsrqDb != -999) details.rsrqDb else -10
                val rssnr = if (details.rssnrDb != -999.0) details.rssnrDb else 18.0

                val sample = SignalSample(
                    timestamp = System.currentTimeMillis(),
                    rsrp = rsrp,
                    rsrq = rsrq,
                    rssnr = rssnr
                )
                val current = _signalHistory.value.takeLast(29) + sample
                _signalHistory.value = current

                delay(2000)
            }
        }
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun refreshNetworkInfo() {
        telephonyScanner.refreshNetworkInfo()
    }

    fun toggleCaForce(context: android.content.Context) {
        if (keepaliveStats.value.isRunning) {
            com.example.service.CaForegroundService.stopService(context)
        } else {
            com.example.service.CaForegroundService.startService(context, keepaliveStats.value.intensity)
        }
    }

    fun setCaIntensity(intensity: CaIntensity) {
        caKeepaliveEngine.setIntensity(intensity)
    }

    fun requestShizukuPermission() {
        com.example.shizuku.ShizukuManager.requestPermission()
    }

    fun checkShizukuStatus() {
        com.example.shizuku.ShizukuManager.checkStatus()
    }

    fun openShizukuApp(context: android.content.Context) {
        com.example.shizuku.ShizukuManager.openShizukuApp(context)
    }

    fun executeShizukuLteOnly(subId: Int = 1) {
        viewModelScope.launch {
            com.example.shizuku.ShizukuManager.setPreferredNetworkTypeLteOnly(subId)
            telephonyScanner.refreshNetworkInfo()
        }
    }

    fun executeShizukuNrLte(subId: Int = 1) {
        viewModelScope.launch {
            com.example.shizuku.ShizukuManager.setPreferredNetworkTypeNrLte(subId)
            telephonyScanner.refreshNetworkInfo()
        }
    }

    fun executeShizukuModemReset() {
        viewModelScope.launch {
            com.example.shizuku.ShizukuManager.resetModemRadio()
            telephonyScanner.refreshNetworkInfo()
        }
    }

    fun executeShizukuCommand(cmd: String) {
        viewModelScope.launch {
            com.example.shizuku.ShizukuManager.executeCommand(cmd)
            telephonyScanner.refreshNetworkInfo()
        }
    }

    fun setBandSelectionMode(single: Boolean) {
        _isSingleBandMode.value = single
    }

    fun selectSingleBand(band: Int) {
        _selectedSingleBand.value = band
        _selectedBands.value = setOf(band)
    }

    fun toggleBand(band: Int) {
        val current = _selectedBands.value.toMutableSet()
        if (current.contains(band)) {
            if (current.size > 1) { // keep at least 1 band selected
                current.remove(band)
            }
        } else {
            current.add(band)
        }
        _selectedBands.value = current
    }

    fun applyBandPreset(bands: Collection<Int>) {
        if (bands.isNotEmpty()) {
            _selectedBands.value = bands.toSet()
            _selectedSingleBand.value = bands.first()
        }
    }

    fun selectAllBands() {
        _selectedBands.value = LteBandDatabase.allBands.map { it.band }.toSet()
    }

    fun clearBands() {
        _selectedBands.value = setOf(3)
        _selectedSingleBand.value = 3
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
