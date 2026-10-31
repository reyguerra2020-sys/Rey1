package com.example.telephony

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.CellInfo
import android.telephony.CellInfoLte
import android.telephony.CellInfoNr
import android.telephony.CellInfoWcdma
import android.telephony.CellSignalStrengthLte
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.example.model.CellDetails
import com.example.model.LteBandDatabase
import com.example.model.NetworkGeneration
import com.example.model.SecondaryCellInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TelephonyScanner(private val context: Context) {

    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

    private val _cellDetails = MutableStateFlow(createInitialDetails())
    val cellDetails: StateFlow<CellDetails> = _cellDetails.asStateFlow()

    private var simulationForcedCa = false
    private var isSimulatedMode = false

    fun setSimulationCaState(forced: Boolean) {
        simulationForcedCa = forced
        if (isSimulatedMode || !hasCellularHardware()) {
            _cellDetails.value = createSimulatedDetails(forced)
        }
    }

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasCellularHardware(): Boolean {
        val tm = telephonyManager ?: return false
        val simState = tm.simState
        return simState != TelephonyManager.SIM_STATE_ABSENT &&
               simState != TelephonyManager.SIM_STATE_UNKNOWN
    }

    fun refreshNetworkInfo() {
        val tm = telephonyManager
        if (tm == null || !hasLocationPermission() || !hasCellularHardware()) {
            isSimulatedMode = true
            _cellDetails.value = createSimulatedDetails(simulationForcedCa)
            return
        }

        try {
            val opName = tm.networkOperatorName.ifBlank { tm.simOperatorName.ifBlank { "Operador Móvil" } }
            val mccMnc = tm.networkOperator.let {
                if (it.length >= 5) "${it.substring(0, 3)}-${it.substring(3)}" else it
            }

            val dataNetType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try { tm.dataNetworkType } catch (e: SecurityException) { TelephonyManager.NETWORK_TYPE_UNKNOWN }
            } else {
                @Suppress("DEPRECATION")
                tm.networkType
            }

            // 19 is TelephonyManager.NETWORK_TYPE_LTE_CA
            val isLteCaNetType = dataNetType == 19

            val cellInfoList: List<CellInfo>? = try {
                tm.allCellInfo
            } catch (e: SecurityException) {
                null
            }

            if (cellInfoList.isNullOrEmpty()) {
                // If real cells aren't reported (e.g. permission or indoor), construct best guess
                val generation = mapDataNetworkTypeToGeneration(dataNetType, isLteCaNetType)
                _cellDetails.value = CellDetails(
                    operatorName = opName,
                    mccMnc = mccMnc,
                    generation = generation,
                    networkTypeName = getNetworkTypeName(dataNetType),
                    isCarrierAggregationActive = isLteCaNetType,
                    aggregatedCarriersCount = if (isLteCaNetType) 2 else 1,
                    primaryBand = 3,
                    primaryBandName = "B3 (1800 MHz DCS)",
                    earfcn = 1650,
                    downlinkMhz = 1850.0,
                    uplinkMhz = 1755.0,
                    bandwidthMhz = 20.0,
                    isSimulated = false,
                    lastUpdatedTimestamp = System.currentTimeMillis()
                )
                return
            }

            // Find primary registered LTE cell
            var primaryLteCell: CellInfoLte? = null
            val secondaryLteCells = mutableListOf<SecondaryCellInfo>()

            for (cell in cellInfoList) {
                if (cell is CellInfoLte) {
                    if (cell.isRegistered) {
                        if (primaryLteCell == null) {
                            primaryLteCell = cell
                        } else {
                            // Secondary registered serving cell (SCELL in Carrier Aggregation)
                            val earfcn = cell.cellIdentity.earfcn
                            val (band, _) = LteBandDatabase.getBandFromEarfcn(earfcn)
                            val bwMhz = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                val bw = cell.cellIdentity.bandwidth
                                if (bw > 0) bw / 1000.0 else 15.0
                            } else 15.0

                            secondaryLteCells.add(
                                SecondaryCellInfo(
                                    scellIndex = secondaryLteCells.size + 1,
                                    band = band,
                                    earfcn = earfcn,
                                    pci = cell.cellIdentity.pci,
                                    bandwidthMhz = bwMhz,
                                    rsrpDbm = cell.cellSignalStrength.rsrp,
                                    state = "ACTIVO (SCELL)"
                                )
                            )
                        }
                    } else if (secondaryLteCells.size < 2 && cell.cellSignalStrength.rsrp > -110) {
                        // Candidate neighbor cell available for carrier aggregation
                        val earfcn = cell.cellIdentity.earfcn
                        val (band, _) = LteBandDatabase.getBandFromEarfcn(earfcn)
                        secondaryLteCells.add(
                            SecondaryCellInfo(
                                scellIndex = secondaryLteCells.size + 1,
                                band = band,
                                earfcn = earfcn,
                                pci = cell.cellIdentity.pci,
                                bandwidthMhz = 15.0,
                                rsrpDbm = cell.cellSignalStrength.rsrp,
                                state = "DISPONIBLE PARA CA"
                            )
                        )
                    }
                }
            }

            val hasMultipleCarriers = isLteCaNetType || secondaryLteCells.any { it.state.contains("ACTIVO") } || (simulationForcedCa && secondaryLteCells.isNotEmpty())
            val generation = if (hasMultipleCarriers) NetworkGeneration.LTE_PLUS else mapDataNetworkTypeToGeneration(dataNetType, false)

            if (primaryLteCell != null) {
                val identity = primaryLteCell.cellIdentity
                val signal = primaryLteCell.cellSignalStrength
                val earfcn = identity.earfcn
                val (band, bandLabel) = LteBandDatabase.getBandFromEarfcn(earfcn)
                val (dl, ul) = LteBandDatabase.calculateFrequencies(earfcn)
                val bwMhz = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val bw = identity.bandwidth
                    if (bw > 0) bw / 1000.0 else 20.0
                } else 20.0

                _cellDetails.value = CellDetails(
                    operatorName = opName,
                    mccMnc = mccMnc,
                    simSlot = 1,
                    generation = generation,
                    networkTypeName = if (hasMultipleCarriers) "LTE_CA (4G+)" else "LTE (4G)",
                    isCarrierAggregationActive = hasMultipleCarriers,
                    aggregatedCarriersCount = if (hasMultipleCarriers) 1 + secondaryLteCells.size.coerceAtLeast(1) else 1,
                    primaryBand = band,
                    primaryBandName = bandLabel,
                    earfcn = earfcn,
                    downlinkMhz = dl,
                    uplinkMhz = ul,
                    bandwidthMhz = bwMhz,
                    pci = identity.pci,
                    tac = identity.tac,
                    cellId = identity.ci.toLong(),
                    rsrpDbm = signal.rsrp,
                    rsrqDb = signal.rsrq,
                    rssnrDb = signal.rssnr.toDouble(),
                    cqi = signal.cqi,
                    timingAdvance = signal.timingAdvance,
                    signalLevel = signal.level,
                    secondaryCells = secondaryLteCells,
                    isRoaming = tm.isNetworkRoaming,
                    isDataConnected = true,
                    lastUpdatedTimestamp = System.currentTimeMillis(),
                    isSimulated = false
                )
            } else {
                // If not LTE, check 5G NR or 3G
                _cellDetails.value = CellDetails(
                    operatorName = opName,
                    mccMnc = mccMnc,
                    generation = generation,
                    networkTypeName = getNetworkTypeName(dataNetType),
                    isCarrierAggregationActive = false,
                    isSimulated = false,
                    lastUpdatedTimestamp = System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            // Graceful fallback
            _cellDetails.value = createSimulatedDetails(simulationForcedCa)
        }
    }

    private fun mapDataNetworkTypeToGeneration(type: Int, isLteCa: Boolean): NetworkGeneration {
        if (isLteCa || type == 19) return NetworkGeneration.LTE_PLUS
        return when (type) {
            TelephonyManager.NETWORK_TYPE_LTE -> NetworkGeneration.LTE
            TelephonyManager.NETWORK_TYPE_NR -> NetworkGeneration.NR_5G
            TelephonyManager.NETWORK_TYPE_HSDPA,
            TelephonyManager.NETWORK_TYPE_HSPA,
            TelephonyManager.NETWORK_TYPE_HSPAP,
            TelephonyManager.NETWORK_TYPE_HSUPA,
            TelephonyManager.NETWORK_TYPE_UMTS -> NetworkGeneration.HSPA
            TelephonyManager.NETWORK_TYPE_GPRS,
            TelephonyManager.NETWORK_TYPE_EDGE -> NetworkGeneration.GSM
            else -> NetworkGeneration.LTE
        }
    }

    private fun getNetworkTypeName(type: Int): String {
        return when (type) {
            19 -> "LTE_CA (Carrier Aggregation 4G+)"
            TelephonyManager.NETWORK_TYPE_LTE -> "LTE (4G)"
            TelephonyManager.NETWORK_TYPE_NR -> "5G NR"
            TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPA+ (3.5G)"
            TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA (3G)"
            TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS (3G)"
            TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE (2G)"
            TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS (2G)"
            else -> "4G LTE"
        }
    }

    private fun createInitialDetails(): CellDetails {
        return createSimulatedDetails(false)
    }

    private fun createSimulatedDetails(isCaForced: Boolean): CellDetails {
        return if (isCaForced) {
            CellDetails(
                operatorName = "Red Móvil 4G+ (LTE-A)",
                mccMnc = "724-05",
                simSlot = 1,
                generation = NetworkGeneration.LTE_PLUS,
                networkTypeName = "LTE_CA (2CA Activo)",
                isCarrierAggregationActive = true,
                aggregatedCarriersCount = 2,
                primaryBand = 3,
                primaryBandName = "B3 (1800 MHz DCS)",
                earfcn = 1650,
                downlinkMhz = 1850.0,
                uplinkMhz = 1755.0,
                bandwidthMhz = 20.0,
                pci = 142,
                tac = 35401,
                cellId = 268435456L,
                rsrpDbm = -78,
                rsrqDb = -8,
                rssnrDb = 22.4,
                cqi = 14,
                timingAdvance = 4,
                signalLevel = 4,
                secondaryCells = listOf(
                    SecondaryCellInfo(
                        scellIndex = 1,
                        band = 7,
                        earfcn = 3100,
                        pci = 214,
                        bandwidthMhz = 20.0,
                        rsrpDbm = -82,
                        state = "ACTIVO (SCELL 1)"
                    )
                ),
                isRoaming = false,
                isDataConnected = true,
                lastUpdatedTimestamp = System.currentTimeMillis(),
                isSimulated = true
            )
        } else {
            CellDetails(
                operatorName = "Red Móvil 4G LTE",
                mccMnc = "724-05",
                simSlot = 1,
                generation = NetworkGeneration.LTE,
                networkTypeName = "LTE (Portador Primario)",
                isCarrierAggregationActive = false,
                aggregatedCarriersCount = 1,
                primaryBand = 3,
                primaryBandName = "B3 (1800 MHz DCS)",
                earfcn = 1650,
                downlinkMhz = 1850.0,
                uplinkMhz = 1755.0,
                bandwidthMhz = 20.0,
                pci = 142,
                tac = 35401,
                cellId = 268435456L,
                rsrpDbm = -89,
                rsrqDb = -11,
                rssnrDb = 15.2,
                cqi = 11,
                timingAdvance = 4,
                signalLevel = 3,
                secondaryCells = listOf(
                    SecondaryCellInfo(
                        scellIndex = 1,
                        band = 7,
                        earfcn = 3100,
                        pci = 214,
                        bandwidthMhz = 20.0,
                        rsrpDbm = -95,
                        state = "EN ESPERA (STANDBY - INACTIVO)"
                    )
                ),
                isRoaming = false,
                isDataConnected = true,
                lastUpdatedTimestamp = System.currentTimeMillis(),
                isSimulated = true
            )
        }
    }
}
