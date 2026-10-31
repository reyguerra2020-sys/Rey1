package com.example

import com.example.model.LteBandDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun earfcn_bandMapping_isCorrect() {
        val (bandB3, _) = LteBandDatabase.getBandFromEarfcn(1650)
        assertEquals(3, bandB3)

        val (bandB7, _) = LteBandDatabase.getBandFromEarfcn(3100)
        assertEquals(7, bandB7)

        val (bandB28, _) = LteBandDatabase.getBandFromEarfcn(9360)
        assertEquals(28, bandB28)

        val (bandB1, _) = LteBandDatabase.getBandFromEarfcn(100)
        assertEquals(1, bandB1)
    }

    @Test
    fun frequencies_calculation_isAccurate() {
        val (dl, ul) = LteBandDatabase.calculateFrequencies(1650)
        // Band 3: Fdl = 1805 + 0.1*(1650-1200) = 1805 + 45.0 = 1850.0
        assertEquals(1850.0, dl, 0.1)
        // Band 3 duplex spacing is 95 MHz
        assertEquals(1755.0, ul, 0.1)
    }

    @Test
    fun popularCombos_hasValidEntries() {
        val combos = LteBandDatabase.popularCaCombos
        assertTrue(combos.isNotEmpty())
        assertTrue(combos.any { it.bands.contains(3) && it.bands.contains(7) })
    }

    @Test
    fun signalQuality_thresholds_match3Gpp() {
        // RSRP
        assertEquals(com.example.ui.screens.SignalQuality.EXCELLENT, com.example.ui.screens.getRsrpQuality(-75f))
        assertEquals(com.example.ui.screens.SignalQuality.GOOD, com.example.ui.screens.getRsrpQuality(-85f))
        assertEquals(com.example.ui.screens.SignalQuality.FAIR, com.example.ui.screens.getRsrpQuality(-95f))
        assertEquals(com.example.ui.screens.SignalQuality.POOR, com.example.ui.screens.getRsrpQuality(-110f))

        // RSRQ
        assertEquals(com.example.ui.screens.SignalQuality.EXCELLENT, com.example.ui.screens.getRsrqQuality(-8f))
        assertEquals(com.example.ui.screens.SignalQuality.POOR, com.example.ui.screens.getRsrqQuality(-20f))

        // SINR
        assertEquals(com.example.ui.screens.SignalQuality.EXCELLENT, com.example.ui.screens.getSinrQuality(22f))
        assertEquals(com.example.ui.screens.SignalQuality.POOR, com.example.ui.screens.getSinrQuality(-3f))
    }

    @Test
    fun keepalive_stats_hasNoTimerDuration() {
        val stats = com.example.engine.KeepaliveStats()
        assertEquals(0L, stats.elapsedSeconds)
        assertTrue(stats.maxQuotaBytes > 0)
    }
}
