package com.example.shizuku

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

data class ShizukuStatus(
    val isAvailable: Boolean = false,
    val isRunning: Boolean = false,
    val isPermissionGranted: Boolean = false,
    val shizukuVersion: Int = 0,
    val lastOutput: String = "",
    val lastCommandSuccess: Boolean? = null
)

object ShizukuManager {

    private const val REQUEST_CODE = 4412
    private val _status = MutableStateFlow(ShizukuStatus())
    val status: StateFlow<ShizukuStatus> = _status.asStateFlow()

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        checkStatus()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        _status.value = _status.value.copy(
            isRunning = false,
            isPermissionGranted = false
        )
    }

    private val permissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == REQUEST_CODE) {
            val granted = grantResult == PackageManager.PERMISSION_GRANTED
            _status.value = _status.value.copy(isPermissionGranted = granted)
        }
    }

    fun init() {
        try {
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Shizuku.addRequestPermissionResultListener(permissionResultListener)
        } catch (_: Throwable) {
        }
        checkStatus()
    }

    fun checkStatus() {
        var isRunning = false
        var isPermissionGranted = false
        var version = 0

        try {
            isRunning = Shizuku.pingBinder()
            if (isRunning) {
                version = Shizuku.getVersion()
                isPermissionGranted = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            }
        } catch (_: Throwable) {
            isRunning = false
            isPermissionGranted = false
        }

        _status.value = _status.value.copy(
            isAvailable = true,
            isRunning = isRunning,
            isPermissionGranted = isPermissionGranted,
            shizukuVersion = version
        )
    }

    fun requestPermission() {
        if (!Shizuku.pingBinder()) {
            checkStatus()
            return
        }
        try {
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                Shizuku.requestPermission(REQUEST_CODE)
            } else {
                _status.value = _status.value.copy(isPermissionGranted = true)
            }
        } catch (_: Throwable) {
            checkStatus()
        }
    }

    suspend fun executeCommand(command: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (!_status.value.isRunning || !_status.value.isPermissionGranted) {
            return@withContext Pair(false, "Shizuku no está en ejecución o el permiso fue denegado.")
        }

        try {
            val method = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            ).apply { isAccessible = true }

            val process = method.invoke(null, arrayOf("sh", "-c", command), null, null) as Process
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errReader = BufferedReader(InputStreamReader(process.errorStream))

            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            while (errReader.readLine().also { line = it } != null) {
                output.append("ERR: ").append(line).append("\n")
            }

            val exitCode = process.waitFor()
            val resultText = output.toString().trim()
            val success = exitCode == 0

            _status.value = _status.value.copy(
                lastOutput = if (resultText.isBlank()) "Comando ejecutado con código $exitCode" else resultText,
                lastCommandSuccess = success
            )

            Pair(success, resultText)
        } catch (t: Throwable) {
            val err = "Error ejecutando comando Shizuku: ${t.localizedMessage}"
            _status.value = _status.value.copy(
                lastOutput = err,
                lastCommandSuccess = false
            )
            Pair(false, err)
        }
    }

    suspend fun setPreferredNetworkTypeLteOnly(subId: Int = 1): Pair<Boolean, String> {
        // Mode 11 corresponds to LTE Only (RAF_LTE = 1 << 14)
        // Command in modern Android: cmd phone set-preferred-network-type <subId> 11
        val cmd = "cmd phone set-preferred-network-type $subId 11"
        return executeCommand(cmd)
    }

    suspend fun setPreferredNetworkTypeNrLte(subId: Int = 1): Pair<Boolean, String> {
        // Mode 22 corresponds to NR/LTE (5G/4G only)
        val cmd = "cmd phone set-preferred-network-type $subId 22"
        return executeCommand(cmd)
    }

    suspend fun resetModemRadio(): Pair<Boolean, String> {
        // Restarts cellular modem radio via elevated command
        val cmd = "cmd phone restart-modem || svc data disable && sleep 1 && svc data enable"
        return executeCommand(cmd)
    }

    fun openShizukuApp(context: Context) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")
            if (intent != null) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            } else {
                openShizukuPlayStore(context)
            }
        } catch (_: Exception) {
            openShizukuPlayStore(context)
        }
    }

    private fun openShizukuPlayStore(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }
}
