package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.engine.CaKeepaliveEngine
import com.example.model.CaIntensity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CaForegroundService : Service() {

    private val scope = CoroutineScope(Dispatchers.Main)
    private var statsJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        const val CHANNEL_ID = "ca_keepalive_service_channel"
        const val NOTIFICATION_ID = 4421
        const val ACTION_START = "com.example.service.ACTION_START"
        const val ACTION_STOP = "com.example.service.ACTION_STOP"
        const val EXTRA_INTENSITY = "extra_intensity"

        fun startService(context: Context, intensity: CaIntensity = CaIntensity.BALANCED) {
            val intent = Intent(context, CaForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_INTENSITY, intensity.name)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, CaForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Force4GPlus::KeepaliveWakelock")?.apply {
            setReferenceCounted(false)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopKeepalive()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START, null -> {
                val intensityName = intent?.getStringExtra(EXTRA_INTENSITY) ?: CaIntensity.BALANCED.name
                val intensity = try {
                    CaIntensity.valueOf(intensityName)
                } catch (_: Exception) {
                    CaIntensity.BALANCED
                }

                startForeground(NOTIFICATION_ID, buildNotification("Iniciando Forzador 4G+..."))
                acquireWakeLock()
                startKeepalive(intensity)
            }
        }

        return START_STICKY
    }

    private fun acquireWakeLock() {
        try {
            if (wakeLock?.isHeld == false) {
                // Acquire wakelock with 4 hour maximum safe limit
                wakeLock?.acquire(4 * 60 * 60 * 1000L)
            }
        } catch (_: Exception) {
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {
        }
    }

    private fun startKeepalive(intensity: CaIntensity) {
        val engine = CaKeepaliveEngine.shared
        engine.start(intensity)

        statsJob?.cancel()
        statsJob = scope.launch {
            engine.stats.collectLatest { stats ->
                if (!stats.isRunning) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                } else {
                    val content = "Ráfagas: #${stats.burstsCount} | Latencia: ${stats.lastLatencyMs}ms | Tiempo: ${stats.elapsedSeconds / 60}m ${stats.elapsedSeconds % 60}s"
                    val notification = buildNotification(content)
                    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.notify(NOTIFICATION_ID, notification)
                }
            }
        }
    }

    private fun stopKeepalive() {
        statsJob?.cancel()
        statsJob = null
        releaseWakeLock()
        CaKeepaliveEngine.shared.stop()
    }

    private fun buildNotification(contentText: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, CaForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("4G+ / LTE-A Forzado Activo")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(openPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Detener", stopPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Forzador 4G+ en Segundo Plano",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Mantiene el módem celular acoplado a portadores secundarios LTE-A incluso con la app cerrada."
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        stopKeepalive()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
