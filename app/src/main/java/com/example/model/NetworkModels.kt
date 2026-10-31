package com.example.model

enum class NetworkGeneration(val displayName: String, val badgeText: String) {
    LTE_PLUS("LTE+ / 4G+ (Carrier Aggregation)", "4G+ LTE-A"),
    LTE("4G LTE (Portador Único)", "4G LTE"),
    NR_5G("5G NR (New Radio)", "5G NR"),
    HSPA("3G / HSPA+", "3G H+"),
    GSM("2G / EDGE", "2G"),
    UNKNOWN("Desconocido", "---"),
    DISCONNECTED("Desconectado / Modo Avión", "OFF")
}

data class SecondaryCellInfo(
    val scellIndex: Int,
    val band: Int,
    val earfcn: Int,
    val pci: Int,
    val bandwidthMhz: Double,
    val rsrpDbm: Int,
    val state: String = "SCELL_ADDED"
)

data class CellDetails(
    val operatorName: String = "Buscando red...",
    val mccMnc: String = "---",
    val simSlot: Int = 1,
    val generation: NetworkGeneration = NetworkGeneration.UNKNOWN,
    val networkTypeName: String = "Desconocido",
    val isCarrierAggregationActive: Boolean = false,
    val aggregatedCarriersCount: Int = 1,
    val primaryBand: Int = 0,
    val primaryBandName: String = "Banda no detectada",
    val earfcn: Int = 0,
    val downlinkMhz: Double = 0.0,
    val uplinkMhz: Double = 0.0,
    val bandwidthMhz: Double = 0.0,
    val pci: Int = 0,
    val tac: Int = 0,
    val cellId: Long = 0L,
    val rsrpDbm: Int = -999, // dBm
    val rsrqDb: Int = -999,  // dB
    val rssnrDb: Double = -999.0, // dB
    val cqi: Int = 0,
    val timingAdvance: Int = -1,
    val signalLevel: Int = 0, // 0 to 4
    val secondaryCells: List<SecondaryCellInfo> = emptyList(),
    val isRoaming: Boolean = false,
    val isDataConnected: Boolean = true,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis(),
    val isSimulated: Boolean = false
)

enum class CaIntensity(val displayName: String, val intervalMs: Long, val description: String) {
    ECO("Eco (10s)", 10000L, "Tráfico mínimo cada 10s para mantener el portador secundario sin alto consumo de batería."),
    BALANCED("Balanceado (5s)", 5000L, "Ráfagas periódicas cada 5s. Óptimo para juegos y navegación continua."),
    TURBO("Turbo (2s)", 2000L, "Ráfagas constantes para forzar a la antena (eNodeB) a mantener la máxima agregación 4G+.")
}

enum class CodeActionType {
    LAUNCH_INTENT,
    DIAL_CODE,
    COPY_CODE
}

data class SecretCodeItem(
    val title: String,
    val code: String,
    val brand: String,
    val description: String,
    val actionType: CodeActionType = CodeActionType.DIAL_CODE,
    val targetIntent: String? = null,
    val isDirectAppSupported: Boolean = false
)

data class CaComboItem(
    val comboName: String,
    val bands: List<Int>,
    val theoreticalDlMbps: Int,
    val theoreticalUlMbps: Int,
    val mimo: String,
    val carriersCount: Int,
    val description: String,
    val commonOperators: String
)
