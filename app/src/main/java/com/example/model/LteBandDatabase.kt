package com.example.model

data class LteBandInfo(
    val band: Int,
    val name: String,
    val duplex: String, // FDD or TDD
    val dlFreqRange: String,
    val ulFreqRange: String,
    val commonUsage: String
)

object LteBandDatabase {

    fun getBandInfo(bandNumber: Int): LteBandInfo {
        return allBands.find { it.band == bandNumber } ?: LteBandInfo(
            band = bandNumber,
            name = "Banda $bandNumber",
            duplex = "FDD",
            dlFreqRange = "Variable",
            ulFreqRange = "Variable",
            commonUsage = "Banda LTE estándar 3GPP"
        )
    }

    fun getBandFromEarfcn(earfcn: Int): Pair<Int, String> {
        return when (earfcn) {
            in 0..599 -> Pair(1, "B1 (2100 MHz)")
            in 600..1199 -> Pair(2, "B2 (1900 MHz PCS)")
            in 1200..1949 -> Pair(3, "B3 (1800 MHz DCS)")
            in 1950..2399 -> Pair(4, "B4 (AWS-1 1700/2100)")
            in 2400..2649 -> Pair(5, "B5 (850 MHz CLR)")
            in 2750..3449 -> Pair(7, "B7 (2600 MHz IMT-E)")
            in 3450..3799 -> Pair(8, "B8 (900 MHz E-GSM)")
            in 5010..5179 -> Pair(12, "B12 (700 MHz Lower)")
            in 5180..5279 -> Pair(13, "B13 (700 MHz Upper C)")
            in 5730..5849 -> Pair(17, "B17 (700 MHz b/c)")
            in 6000..6149 -> Pair(19, "B19 (800 MHz Japón)")
            in 6150..6449 -> Pair(20, "B20 (800 MHz Dividendo Digital)")
            in 9210..9659 -> Pair(28, "B28 (700 MHz APT)")
            in 37750..38249 -> Pair(38, "B38 (2600 MHz TDD)")
            in 38650..39649 -> Pair(40, "B40 (2300 MHz TDD)")
            in 39650..41589 -> Pair(41, "B41 (2500 MHz BRS/EBS)")
            in 66436..67335 -> Pair(66, "B66 (AWS Extendido 1700/2100)")
            in 68586..68935 -> Pair(71, "B71 (600 MHz T-Mobile)")
            else -> Pair(0, if (earfcn > 0) "EARFCN $earfcn" else "Desconocido")
        }
    }

    fun calculateFrequencies(earfcn: Int): Pair<Double, Double> {
        val (band, _) = getBandFromEarfcn(earfcn)
        val dl = when (band) {
            1 -> 2110.0 + 0.1 * (earfcn - 0)
            2 -> 1930.0 + 0.1 * (earfcn - 600)
            3 -> 1805.0 + 0.1 * (earfcn - 1200)
            4 -> 2110.0 + 0.1 * (earfcn - 1950)
            5 -> 869.0 + 0.1 * (earfcn - 2400)
            7 -> 2620.0 + 0.1 * (earfcn - 2750)
            8 -> 925.0 + 0.1 * (earfcn - 3450)
            12 -> 729.0 + 0.1 * (earfcn - 5010)
            13 -> 746.0 + 0.1 * (earfcn - 5180)
            17 -> 734.0 + 0.1 * (earfcn - 5730)
            20 -> 791.0 + 0.1 * (earfcn - 6150)
            28 -> 758.0 + 0.1 * (earfcn - 9210)
            38 -> 2570.0 + 0.1 * (earfcn - 37750)
            40 -> 2300.0 + 0.1 * (earfcn - 38650)
            41 -> 2496.0 + 0.1 * (earfcn - 39650)
            66 -> 2110.0 + 0.1 * (earfcn - 66436)
            71 -> 617.0 + 0.1 * (earfcn - 68586)
            else -> 0.0
        }

        // Standard LTE FDD duplex spacing
        val ul = when (band) {
            1 -> dl - 190.0
            2 -> dl - 80.0
            3 -> dl - 95.0
            4 -> dl - 400.0
            5 -> dl - 45.0
            7 -> dl - 120.0
            8 -> dl - 45.0
            12 -> dl - 30.0
            13 -> dl - 31.0
            20 -> dl + 41.0
            28 -> dl - 55.0
            38, 40, 41 -> dl // TDD uses same frequency for DL/UL
            66 -> dl - 400.0
            71 -> dl - 46.0
            else -> 0.0
        }

        return Pair(dl, ul)
    }

    val allBands = listOf(
        LteBandInfo(1, "B1 (2100 MHz)", "FDD", "2110-2170 MHz", "1920-1980 MHz", "Común en Europa, Asia y operadores globales"),
        LteBandInfo(2, "B2 (1900 MHz PCS)", "FDD", "1930-1990 MHz", "1850-1910 MHz", "Principal en EE.UU., México, Colombia, Argentina"),
        LteBandInfo(3, "B3 (1800 MHz DCS)", "FDD", "1805-1880 MHz", "1710-1785 MHz", "Muy usada para Carrier Aggregation con B7 y B20"),
        LteBandInfo(4, "B4 (AWS-1 1700/2100)", "FDD", "2110-2155 MHz", "1710-1755 MHz", "Columna vertebral 4G en toda América"),
        LteBandInfo(5, "B5 (850 MHz CLR)", "FDD", "869-894 MHz", "824-849 MHz", "Banda baja de penetración en interiores"),
        LteBandInfo(7, "B7 (2600 MHz IMT-E)", "FDD", "2620-2690 MHz", "2500-2570 MHz", "Banda de alta capacidad; clave para LTE+ y 4G+"),
        LteBandInfo(8, "B8 (900 MHz)", "FDD", "925-960 MHz", "880-915 MHz", "Cobertura rural y larga distancia"),
        LteBandInfo(12, "B12 (700 MHz)", "FDD", "729-746 MHz", "699-716 MHz", "Banda baja de penetración en Norteamérica"),
        LteBandInfo(20, "B20 (800 MHz DD)", "FDD", "791-821 MHz", "832-862 MHz", "Dividendo Digital en Europa para cobertura profunda"),
        LteBandInfo(28, "B28 (700 MHz APT)", "FDD", "758-803 MHz", "703-748 MHz", "Banda reina de cobertura 4G en Latinoamérica"),
        LteBandInfo(38, "B38 (2600 MHz TDD)", "TDD", "2570-2620 MHz", "2570-2620 MHz", "Capacidad extra en ciudades densas"),
        LteBandInfo(40, "B40 (2300 MHz TDD)", "TDD", "2300-2400 MHz", "2300-2400 MHz", "Utilizada para internet móvil y alta densidad"),
        LteBandInfo(41, "B41 (2500 MHz TDD)", "TDD", "2496-2690 MHz", "2496-2690 MHz", "Amplio ancho de banda hasta 100MHz"),
        LteBandInfo(66, "B66 (AWS Extendido)", "FDD", "2110-2200 MHz", "1710-1780 MHz", "Evolución de Banda 4 con mayor espectro agregable"),
        LteBandInfo(71, "B71 (600 MHz)", "FDD", "617-652 MHz", "663-698 MHz", "Penetración ultra profunda en edificios")
    )

    val popularCaCombos = listOf(
        CaComboItem(
            comboName = "2CA: B3 + B7 (1800 + 2600)",
            bands = listOf(3, 7),
            theoreticalDlMbps = 300,
            theoreticalUlMbps = 75,
            mimo = "2x2 / 4x4 MIMO",
            carriersCount = 2,
            description = "La combinación más común y potente de agregación en Europa y Latinoamérica. Ofrece hasta 40 MHz combinados.",
            commonOperators = "Movistar, Claro, Vodafone, Orange, Personal, Entel"
        ),
        CaComboItem(
            comboName = "2CA: B7 + B28 (2600 + 700)",
            bands = listOf(7, 28),
            theoreticalDlMbps = 250,
            theoreticalUlMbps = 50,
            mimo = "2x2 MIMO",
            carriersCount = 2,
            description = "Excelente balance entre velocidad máxima (B7) y penetración profunda en edificios o zonas suburbanas (B28).",
            commonOperators = "Claro, Entel, WOM, TIM Brasil, Vivo"
        ),
        CaComboItem(
            comboName = "2CA: B2 + B4 / B66 (1900 + AWS)",
            bands = listOf(2, 4),
            theoreticalDlMbps = 300,
            theoreticalUlMbps = 75,
            mimo = "2x2 / 4x4 MIMO",
            carriersCount = 2,
            description = "Estándar de Carrier Aggregation en Norteamérica, México y varios países de Sudamérica.",
            commonOperators = "Telcel, AT&T, T-Mobile, Movistar Colombia/México"
        ),
        CaComboItem(
            comboName = "2CA: B1 + B3 (2100 + 1800)",
            bands = listOf(1, 3),
            theoreticalDlMbps = 300,
            theoreticalUlMbps = 75,
            mimo = "2x2 MIMO",
            carriersCount = 2,
            description = "Ampliamente soportada por modems Qualcomm Snapdragon y Mediatek Dimensity sin alto consumo de batería.",
            commonOperators = "Vodafone, MasOrange, TIM, Singtel, SoftBank"
        ),
        CaComboItem(
            comboName = "3CA: B1 + B3 + B7",
            bands = listOf(1, 3, 7),
            theoreticalDlMbps = 450,
            theoreticalUlMbps = 100,
            mimo = "4x4 MIMO 256QAM",
            carriersCount = 3,
            description = "Triple portador LTE-A Pro. Permite superar los 400 Mbps reales en zonas céntricas y aeropuertos.",
            commonOperators = "Movistar España, Vodafone, Claro Chile/Perú, Entel"
        ),
        CaComboItem(
            comboName = "3CA: B3 + B7 + B20",
            bands = listOf(3, 7, 20),
            theoreticalDlMbps = 400,
            theoreticalUlMbps = 75,
            mimo = "2x2 / 4x4 MIMO",
            carriersCount = 3,
            description = "Fórmula clásica europea que combina máxima velocidad con cobertura del dividendo digital 800MHz.",
            commonOperators = "Telefónica, Deutsche Telekom, Swisscom"
        ),
        CaComboItem(
            comboName = "4CA: B1 + B3 + B7 + B28",
            bands = listOf(1, 3, 7, 28),
            theoreticalDlMbps = 600,
            theoreticalUlMbps = 150,
            mimo = "4x4 MIMO 256QAM",
            carriersCount = 4,
            description = "Cuádruple agregación de portadores Gigabit LTE. Activa categoría 16/18 en el módem del smartphone.",
            commonOperators = "Operadores con redes LTE-Advanced Pro de última generación"
        )
    )
}
