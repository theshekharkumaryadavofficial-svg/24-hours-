package com.example.bedtime

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.NaamJapRepository
import com.example.data.SleepDatabase
import com.example.data.SleepGoalEntity
import com.example.data.SleepRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.sqrt

class BedtimeGuardService : Service() {

    companion object {
        const val ACTION_START_GUARD = "com.example.bedtime.START_GUARD"
        const val ACTION_STOP_GUARD = "com.example.bedtime.STOP_GUARD"
        const val EXTRA_IS_AUTO = "extra_is_auto"

        private const val CHANNEL_ID = "bedtime_guard_channel"
        private const val NOTIFICATION_ID = 2400

        fun startGuard(context: Context, isAutoTriggered: Boolean) {
            val intent = Intent(context, BedtimeGuardService::class.java).apply {
                action = ACTION_START_GUARD
                putExtra(EXTRA_IS_AUTO, isAutoTriggered)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stopGuard(context: Context) {
            val intent = Intent(context, BedtimeGuardService::class.java).apply {
                action = ACTION_STOP_GUARD
            }
            runCatching { context.startService(intent) }
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var repository: SleepRepository
    private lateinit var naamJapRepository: NaamJapRepository
    private lateinit var overlayManager: BedtimeOverlayManager

    private var currentGoal: SleepGoalEntity = SleepGoalEntity()
    private var previousInterruptionFilter: Int? = null
    private var screenReceiverRegistered = false
    private var appMonitorJob: Job? = null
    private var screenTurnedOnAtMillis: Long = 0L
    private var lastBlockedPackage: String = ""
    private var lastBlockedAtMillis: Long = 0L

    // Low-power accelerometer stillness sampler (only runs briefly on screen-off to save battery)
    private var sensorManager: SensorManager? = null
    private var lastAccelMagnitude = 9.81f
    private var stillnessSamplingJob: Job? = null

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> handleScreenOff()
                Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> handleScreenOn()
            }
        }
    }

    private val stillnessListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            val values = event?.values ?: return
            val mag = sqrt(values[0] * values[0] + values[1] * values[1] + values[2] * values[2])
            val delta = abs(mag - lastAccelMagnitude)
            lastAccelMagnitude = mag
            val isStill = delta < 0.45f
            serviceScope.launch {
                repository.recordMotionSample(isStill)
            }
            // Immediately unregister after single check to conserve battery
            sensorManager?.unregisterListener(this)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    override fun onCreate() {
        super.onCreate()
        val db = SleepDatabase.getInstance(applicationContext)
        repository = SleepRepository(db.sleepDao())
        naamJapRepository = NaamJapRepository(db.naamJapDao())
        overlayManager = BedtimeOverlayManager(applicationContext)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_GUARD -> {
                stopBedtimeAndSave()
                return START_NOT_STICKY
            }
            else -> {
                val isAuto = intent?.getBooleanExtra(EXTRA_IS_AUTO, false) ?: false
                beginBedtimeGuard(isAuto)
            }
        }
        return START_STICKY
    }

    private fun beginBedtimeGuard(isAutoTriggered: Boolean) {
        val notification = buildGuardNotification("Silencing calls & SMS · Blocking distractions")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        registerScreenStateReceiver()

        serviceScope.launch {
            repository.ensureGoalInitialized()
            currentGoal = repository.getGoalOnce()
            val existing = repository.getActiveSessionOnce()
            if (!existing.isActive) {
                val now = Calendar.getInstance()
                val (batteryPct, _) = BedtimePermissionsHelper.readRealBatteryState(applicationContext)
                repository.startRealSleepSession(
                    isAutoTriggered = isAutoTriggered,
                    startEpochMillis = System.currentTimeMillis(),
                    startHour = now.get(Calendar.HOUR_OF_DAY),
                    startMinute = now.get(Calendar.MINUTE),
                    startBatteryPercent = batteryPct
                )
            }

            if (currentGoal.silenceCallsAndSms) {
                enableDoNotDisturbSilence()
            }

            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (pm.isInteractive) {
                screenTurnedOnAtMillis = System.currentTimeMillis()
                startForegroundAppMonitor()
            } else {
                startLowPowerStillnessProbe()
            }
        }
    }

    private fun stopBedtimeAndSave() {
        appMonitorJob?.cancel()
        stillnessSamplingJob?.cancel()
        sensorManager?.unregisterListener(stillnessListener)
        unregisterScreenStateReceiver()

        serviceScope.launch {
            if (screenTurnedOnAtMillis > 0L) {
                val secs = (System.currentTimeMillis() - screenTurnedOnAtMillis) / 1000L
                repository.addScreenOnDuration(secs.coerceAtLeast(0L))
                screenTurnedOnAtMillis = 0L
            }

            val now = Calendar.getInstance()
            val (endBattery, _) = BedtimePermissionsHelper.readRealBatteryState(applicationContext)
            repository.finishRealSleepSession(
                endEpochMillis = System.currentTimeMillis(),
                endHour = now.get(Calendar.HOUR_OF_DAY),
                endMinute = now.get(Calendar.MINUTE),
                endBatteryPercent = endBattery
            )

            restoreDoNotDisturb()
            withContext(Dispatchers.Main) {
                overlayManager.dismiss()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun handleScreenOff() {
        // ZERO polling while screen is OFF -> maximum overnight battery saving!
        appMonitorJob?.cancel()
        appMonitorJob = null
        serviceScope.launch {
            if (screenTurnedOnAtMillis > 0L) {
                val secs = (System.currentTimeMillis() - screenTurnedOnAtMillis) / 1000L
                repository.addScreenOnDuration(secs.coerceAtLeast(0L))
                screenTurnedOnAtMillis = 0L
            }
            withContext(Dispatchers.Main) {
                overlayManager.dismiss()
            }
        }
        startLowPowerStillnessProbe()
    }

    private fun handleScreenOn() {
        stillnessSamplingJob?.cancel()
        sensorManager?.unregisterListener(stillnessListener)
        if (screenTurnedOnAtMillis == 0L) {
            screenTurnedOnAtMillis = System.currentTimeMillis()
            serviceScope.launch {
                repository.recordScreenWakeUp()
            }
        }
        startForegroundAppMonitor()
    }

    private fun startLowPowerStillnessProbe() {
        stillnessSamplingJob?.cancel()
        stillnessSamplingJob = serviceScope.launch {
            // Brief single-sample check every 15 minutes while screen is off (negligible battery cost)
            val accel = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return@launch
            while (isActive) {
                delay(15 * 60_000L)
                withContext(Dispatchers.Main) {
                    sensorManager?.registerListener(
                        stillnessListener,
                        accel,
                        SensorManager.SENSOR_DELAY_NORMAL
                    )
                }
            }
        }
    }

    private fun startForegroundAppMonitor() {
        if (appMonitorJob?.isActive == true) return
        appMonitorJob = serviceScope.launch {
            while (isActive) {
                val goal = repository.getGoalOnce()
                currentGoal = goal
                if (goal.blockAppsInBedtime && BedtimePermissionsHelper.hasUsageStatsPermission(applicationContext)) {
                    val foregroundPkg = detectCurrentForegroundPackage()
                    if (foregroundPkg != null && shouldBlockPackage(foregroundPkg)) {
                        val now = System.currentTimeMillis()
                        val recentlyBlockedSame =
                            foregroundPkg == lastBlockedPackage && (now - lastBlockedAtMillis) < 3500L
                        if (!recentlyBlockedSame) {
                            lastBlockedPackage = foregroundPkg
                            lastBlockedAtMillis = now
                            val appLabel = resolveAppLabel(foregroundPkg)
                            val updatedSession = repository.recordBlockedAppAttempt(appLabel)
                            val windowText = "${goal.targetBedtimeFormatted} - ${goal.targetWakeFormatted}"
                            triggerRedForegroundBlocker(
                                appLabel = appLabel,
                                sleepWindow = windowText,
                                blockedCount = updatedSession.blockedAttemptsCount
                            )
                        }
                    }
                }
                delay(1200L)
            }
        }
    }

    private fun detectCurrentForegroundPackage(): String? {
        return try {
            val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val end = System.currentTimeMillis()
            val begin = end - 10_000L
            val events = usm.queryEvents(begin, end)
            val event = UsageEvents.Event()
            var latestPkg: String? = null
            var latestTs = 0L
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                val isResumed = event.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                    @Suppress("DEPRECATION") (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND)
                if (isResumed && event.timeStamp >= latestTs) {
                    latestTs = event.timeStamp
                    latestPkg = event.packageName
                }
            }
            latestPkg
        } catch (_: Exception) {
            null
        }
    }

    private fun shouldBlockPackage(pkg: String): Boolean {
        if (pkg == packageName) return false
        if (pkg == "com.android.systemui") return false
        if (pkg == "com.android.settings") return false
        if (pkg.contains("dialer", ignoreCase = true) || pkg.contains("telecom", ignoreCase = true)) {
            return false // Never block emergency phone calls
        }
        if (pkg.contains("deskclock", ignoreCase = true) || pkg.contains("clock", ignoreCase = true)) {
            return false // Allow system alarm clock
        }
        if (isDefaultLauncherPackage(pkg)) return false
        return true
    }

    private fun isDefaultLauncherPackage(pkg: String): Boolean {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) }
        val resolveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.resolveActivity(homeIntent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()))
        } else {
            @Suppress("DEPRECATION")
            packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
        }
        return resolveInfo?.activityInfo?.packageName == pkg
    }

    private fun resolveAppLabel(pkg: String): String {
        return try {
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getApplicationInfo(pkg, PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getApplicationInfo(pkg, 0)
            }
            packageManager.getApplicationLabel(info).toString()
        } catch (_: Exception) {
            pkg.substringAfterLast('.').replaceFirstChar { it.uppercase() }
        }
    }

    private suspend fun triggerRedForegroundBlocker(
        appLabel: String,
        sleepWindow: String,
        blockedCount: Int
    ) {
        val (todayJap, allTimeJap) = naamJapRepository.getTodayAndAllTimeOnce()
        withContext(Dispatchers.Main) {
            val shownOverlay = overlayManager.showNaamJapBlockerOverlay(
                blockedAppLabel = appLabel,
                slotTitle = "Bedtime Sleep Guard",
                windowText = sleepWindow,
                blockedCount = blockedCount,
                initialTodayNaamJap = todayJap,
                initialAllTimeNaamJap = allTimeJap,
                headerBadgeText = "24 HRS · BEDTIME NAAM JAP BLOCKER",
                ruleMessage = "Calls & SMS are silenced to protect your sleep. Chant Radha Naam calmly and rest.",
                returnButtonText = "RETURN TO SLEEP",
                endButtonText = "END BEDTIME & UNLOCK",
                dimScreenForSleep = true,
                onIncrementNaamJap = {
                    serviceScope.launch { naamJapRepository.incrementToday(1) }
                },
                onReturnHome = { /* User sent cleanly to Home */ },
                onEndGuard = { stopBedtimeAndSave() }
            )
            if (!shownOverlay) {
                val lockIntent = Intent(applicationContext, BedtimeLockActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra(BedtimeLockActivity.EXTRA_BLOCKED_APP, appLabel)
                    putExtra(BedtimeLockActivity.EXTRA_SLOT_TITLE, "Bedtime Sleep Guard")
                    putExtra(BedtimeLockActivity.EXTRA_SLEEP_WINDOW, sleepWindow)
                    putExtra(BedtimeLockActivity.EXTRA_BLOCKED_COUNT, blockedCount)
                    putExtra(BedtimeLockActivity.EXTRA_IS_PREVIEW, false)
                    putExtra(BedtimeLockActivity.EXTRA_IS_STUDY_MODE, false)
                }
                runCatching { startActivity(lockIntent) }
            }
        }
    }

    private fun enableDoNotDisturbSilence() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!nm.isNotificationPolicyAccessGranted) return
        if (previousInterruptionFilter == null) {
            previousInterruptionFilter = nm.currentInterruptionFilter
        }
        // Silences all calls, SMS, and app notifications while still letting morning alarms ring
        runCatching {
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS)
        }
    }

    private fun restoreDoNotDisturb() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!nm.isNotificationPolicyAccessGranted) return
        val restoreFilter = previousInterruptionFilter ?: NotificationManager.INTERRUPTION_FILTER_ALL
        runCatching {
            nm.setInterruptionFilter(restoreFilter)
        }
        previousInterruptionFilter = null
    }

    private fun registerScreenStateReceiver() {
        if (screenReceiverRegistered) return
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(screenReceiver, filter)
        screenReceiverRegistered = true
    }

    private fun unregisterScreenStateReceiver() {
        if (!screenReceiverRegistered) return
        runCatching { unregisterReceiver(screenReceiver) }
        screenReceiverRegistered = false
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notif_channel_bedtime_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notif_channel_bedtime_desc)
                setShowBadge(false)
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildGuardNotification(contentText: String): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, BedtimeGuardService::class.java).apply { action = ACTION_STOP_GUARD },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("24 Hrs · Bedtime Guard Active")
            .setContentText(contentText)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openIntent)
            .addAction(0, "Wake Up & Save", stopIntent)
            .build()
    }

    override fun onDestroy() {
        appMonitorJob?.cancel()
        stillnessSamplingJob?.cancel()
        sensorManager?.unregisterListener(stillnessListener)
        unregisterScreenStateReceiver()
        overlayManager.dismiss()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
