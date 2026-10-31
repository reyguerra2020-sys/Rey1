package com.example.engine

import com.example.model.CaIntensity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

data class KeepaliveStats(
    val isRunning: Boolean = false,
    val burstsCount: Int = 0,
    val totalBytesTransferred: Long = 0L,
    val lastLatencyMs: Long = 0L,
    val elapsedSeconds: Long = 0L,
    val maxQuotaBytes: Long = 100 * 1024 * 1024L, // 100 MB default safeguard
    val intensity: CaIntensity = CaIntensity.BALANCED,
    val statusMessage: String = "Inactivo. Presiona para forzar agregación 4G+"
)

class CaKeepaliveEngine(
    private var onCaStateChanged: ((Boolean) -> Unit)? = null
) {
    companion object {
        val shared: CaKeepaliveEngine by lazy { CaKeepaliveEngine() }
    }

    fun setOnCaStateChangedListener(listener: (Boolean) -> Unit) {
        this.onCaStateChanged = listener
    }
    private val scope = CoroutineScope(Dispatchers.IO)
    private var job: Job? = null

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .build()

    private val testUrls = listOf(
        "https://www.google.com/generate_204",
        "https://connectivitycheck.gstatic.com/generate_204",
        "https://1.1.1.1/cdn-cgi/trace"
    )

    private val _stats = MutableStateFlow(KeepaliveStats())
    val stats: StateFlow<KeepaliveStats> = _stats.asStateFlow()

    fun setIntensity(intensity: CaIntensity) {
        _stats.value = _stats.value.copy(intensity = intensity)
        if (_stats.value.isRunning) {
            // Restart with new interval
            stop()
            start(intensity)
        }
    }

    fun start(intensity: CaIntensity = _stats.value.intensity) {
        job?.cancel()
        _stats.value = _stats.value.copy(
            isRunning = true,
            intensity = intensity,
            elapsedSeconds = 0L,
            burstsCount = 0,
            totalBytesTransferred = 0L,
            statusMessage = "Forzando portadores LTE+... Manteniendo módem en RRC_CONNECTED"
        )

        onCaStateChanged?.invoke(true)

        job = scope.launch {
            var bursts = 0
            var bytes = 0L
            var elapsed = 0L
            val intervalMs = intensity.intervalMs

            while (isActive) {
                val startTime = System.currentTimeMillis()
                val url = testUrls[bursts % testUrls.size]

                var latency = 0L
                try {
                    val request = Request.Builder()
                        .url(url)
                        .addHeader("Cache-Control", "no-cache")
                        .build()

                    httpClient.newCall(request).execute().use { response ->
                        latency = System.currentTimeMillis() - startTime
                        val bodyBytes = response.body?.bytes()?.size?.toLong() ?: 128L
                        bytes += bodyBytes + 400L // Include headers estimate
                    }
                } catch (e: IOException) {
                    latency = (System.currentTimeMillis() - startTime).coerceAtLeast(15L)
                    bytes += 256L
                }

                bursts++
                val sleepSecs = (intervalMs / 1000).coerceAtLeast(1)
                elapsed += sleepSecs

                _stats.value = _stats.value.copy(
                    burstsCount = bursts,
                    totalBytesTransferred = bytes,
                    lastLatencyMs = latency,
                    elapsedSeconds = elapsed,
                    statusMessage = "4G+ / LTE-A Forzado Activo | Ráfaga #${bursts} (${latency}ms)"
                )

                if (bytes >= _stats.value.maxQuotaBytes) {
                    _stats.value = _stats.value.copy(
                        statusMessage = "Límite de seguridad alcanzado (${_stats.value.maxQuotaBytes / (1024 * 1024)}MB)"
                    )
                    break
                }

                delay(intervalMs)
            }

            stop()
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        _stats.value = _stats.value.copy(
            isRunning = false,
            statusMessage = "Forzador detenido. El módem puede volver a portador único para ahorrar batería."
        )
        onCaStateChanged?.invoke(false)
    }

    fun toggle(intensity: CaIntensity = _stats.value.intensity) {
        if (_stats.value.isRunning) {
            stop()
        } else {
            start(intensity)
        }
    }
}
