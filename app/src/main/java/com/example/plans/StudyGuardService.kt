package com.example.plans

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
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.bedtime.BedtimeLockActivity
import com.example.bedtime.BedtimeOverlayManager
import com.example.bedtime.BedtimePermissionsHelper
import com.example.data.DailyPlanBlockEntity
import com.example.data.NaamJapRepository
import com.example.data.PlansRepository
import com.example.data.SleepDatabase
import com.example.data.StudyModeConfigEntity
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

/**
 * 24H Slot & Study Shield Service:
 * 1) Enforces UNLIMITED permanent per-slot Allowed-Apps rules from 24H Dincharya
 *    (e.g. 17:00-19:00 Film Watching -> ONLY apps toggled ON for that slot can open;
 *    ANY other app, including newly installed apps, is blocked!).
 * 2) Enforces Study Mode social-media blocking when Study Mode is active.
 * 3) Displays the Full-Screen Red, Black & White RADHA NAAM JAP COUNTER BLOCKER
 *    whenever any disallowed app is opened — saving every tap permanently into Room!
 */
class StudyGuardService : Service() {

    companion object {
        const val ACTION_START_STUDY = "com.example.plans.START_STUDY"
        const val ACTION_STOP_STUDY = "com.example.plans.STOP_STUDY"

        private const val CHANNEL_ID = "study_mode_guard_channel"
        private const val NOTIFICATION_ID = 2500

        fun startStudyMode(context: Context) {
            val intent = Intent(context, StudyGuardService::class.java).apply {
                action = ACTION_START_STUDY
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stopStudyMode(context: Context) {
            val intent = Intent(context, StudyGuardService::class.java).apply {
                action = ACTION_STOP_STUDY
            }
            runCatching { context.startService(intent) }
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var repository: PlansRepository
    private lateinit var naamJapRepository: NaamJapRepository
    private lateinit var overlayManager: BedtimeOverlayManager

    private var screenReceiverRegistered = false
    private var monitorJob: Job? = null
    private var lastBlockedPkg = ""
    private var lastBlockedAt = 0L
    private var lastNotifText = ""

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    monitorJob?.cancel()
                    monitorJob = null
                    serviceScope.launch {
                        withContext(Dispatchers.Main) { overlayManager.dismiss() }
                    }
                }
                Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> {
                    startSlotAndStudyMonitor()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val db = SleepDatabase.getInstance(applicationContext)
        repository = PlansRepository(db.plansDao())
        naamJapRepository = NaamJapRepository(db.naamJapDao())
        overlayManager = BedtimeOverlayManager(applicationContext)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_STUDY -> {
                stopShield()
                return START_NOT_STICKY
            }
            else -> beginShield()
        }
        return START_STICKY
    }

    private fun beginShield() {
        val initialText = "24H Slot & Study Shield Active · Naam Jap Blocker Armed"
        lastNotifText = initialText
        val notification = buildNotification(initialText)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        registerScreenReceiver()
        serviceScope.launch {
            repository.ensureInitialized()
            repository.setStudyModeActive(true)
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (pm.isInteractive) {
                startSlotAndStudyMonitor()
            }
        }
    }

    private fun stopShield() {
        monitorJob?.cancel()
        unregisterScreenReceiver()
        serviceScope.launch {
            repository.setStudyModeActive(false)
            withContext(Dispatchers.Main) {
                overlayManager.dismiss()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun startSlotAndStudyMonitor() {
        if (monitorJob?.isActive == true) return
        monitorJob = serviceScope.launch {
            while (isActive) {
                val config = repository.getStudyConfigOnce()
                val blocks = repository.getAllBlocksOnce()

                val cal = Calendar.getInstance()
                val nowH = cal.get(Calendar.HOUR_OF_DAY)
                val nowM = cal.get(Calendar.MINUTE)

                // Priority 1: Any active permanent 24H Dincharya slot with Strict App Lock ON
                val activeStrictSlots = blocks.filter {
                    it.strictAppLockEnabled && it.isCurrentTimeInside(nowH, nowM)
                }

                // Update ongoing notification concisely if active slot changed
                val dynamicNotif = when {
                    activeStrictSlots.isNotEmpty() -> {
                        val primarySlot = activeStrictSlots.first()
                        val mergedCount = activeStrictSlots.flatMap { it.allowedPackagesSet() }.toSet().size
                        "Slot: ${primarySlot.title} (${primarySlot.timeRangeFormatted}) · $mergedCount apps allowed"
                    }
                    else -> "Study Mode Active (${config.windowFormatted}) · Naam Jap Blocker Armed"
                }
                if (dynamicNotif != lastNotifText) {
                    lastNotifText = dynamicNotif
                    val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    nm.notify(NOTIFICATION_ID, buildNotification(dynamicNotif))
                }

                if (BedtimePermissionsHelper.hasUsageStatsPermission(applicationContext)) {
                    val fgPkg = detectForegroundPackage()
                    if (fgPkg != null && !isSystemSafetyApp(fgPkg)) {
                        val decision = evaluateBlockingDecision(
                            pkg = fgPkg,
                            activeStrictSlots = activeStrictSlots,
                            studyConfig = config,
                            nowHour = nowH,
                            nowMinute = nowM
                        )
                        if (decision != null) {
                            val now = System.currentTimeMillis()
                            val recentlyBlocked = fgPkg == lastBlockedPkg && (now - lastBlockedAt) < 3000L
                            if (!recentlyBlocked) {
                                lastBlockedPkg = fgPkg
                                lastBlockedAt = now
                                val appLabel = resolveAppLabel(fgPkg)
                                val blockedCount = repository.incrementStudyBlockedCount()
                                val (todayJap, allTimeJap) = naamJapRepository.getTodayAndAllTimeOnce()
                                triggerNaamJapBlocker(
                                    appLabel = appLabel,
                                    slotTitle = decision.slotTitle,
                                    windowText = decision.windowText,
                                    badgeText = decision.badgeText,
                                    ruleText = decision.ruleText,
                                    blockedCount = blockedCount,
                                    todayJap = todayJap,
                                    allTimeJap = allTimeJap
                                )
                            }
                        }
                    }
                }
                delay(1000L)
            }
        }
    }

    private data class BlockDecision(
        val slotTitle: String,
        val windowText: String,
        val badgeText: String,
        val ruleText: String
    )

    private fun evaluateBlockingDecision(
        pkg: String,
        activeStrictSlots: List<DailyPlanBlockEntity>,
        studyConfig: StudyModeConfigEntity,
        nowHour: Int,
        nowMinute: Int
    ): BlockDecision? {
        // 1. Strict Per-Slot Allowlist Rule (Permanent for this time slot!)
        // If multiple strict slots overlap, union their explicitly allowed packages;
        // ANY app not toggled ON (including brand-new installed apps!) is BLOCKED!
        if (activeStrictSlots.isNotEmpty()) {
            val allowedPackages = activeStrictSlots.flatMap { it.allowedPackagesSet() }.toSet()
            if (pkg in allowedPackages) {
                return null // Explicitly permitted for this slot!
            }
            val primary = activeStrictSlots.first()
            return BlockDecision(
                slotTitle = primary.title,
                windowText = primary.timeRangeFormatted,
                badgeText = "24 HRS · SLOT APP LOCK ACTIVE",
                ruleText = if (allowedPackages.isEmpty()) {
                    "No external apps are permitted during '${primary.title}'. Chant Radha Naam until your slot ends!"
                } else {
                    "Only your ${allowedPackages.size} selected app(s) can open during '${primary.title}'. Any other or new app opens Radha Naam Jap!"
                }
            )
        }

        // 2. General Study Mode Rule (when Study Mode is active manually or during Study Window)
        val shouldApplyStudy = studyConfig.blockSocialMedia &&
            (studyConfig.isActiveNow || (studyConfig.autoStudyEnabled && studyConfig.isTimeInsideStudyWindow(nowHour, nowMinute)))
        if (shouldApplyStudy && shouldBlockDuringStudy(pkg, studyConfig)) {
            return BlockDecision(
                slotTitle = "Study Focus Shield",
                windowText = studyConfig.windowFormatted,
                badgeText = "24 HRS · STUDY NAAM JAP BLOCKER",
                ruleText = "Social media & non-study apps are blocked during Study Mode (${studyConfig.windowFormatted}). Chant Radha Naam & return to study!"
            )
        }

        return null
    }

    private fun isSystemSafetyApp(pkg: String): Boolean {
        val lowerPkg = pkg.lowercase()
        if (pkg == packageName) return true
        if (lowerPkg == "com.android.systemui" || lowerPkg == "com.android.settings") return true
        if (lowerPkg.contains("dialer") || lowerPkg.contains("telecom")) return true
        if (lowerPkg.contains("deskclock") || lowerPkg.contains("clock")) return true
        if (isDefaultLauncher(pkg)) return true
        return false
    }

    private fun shouldBlockDuringStudy(pkg: String, config: StudyModeConfigEntity): Boolean {
        val lowerPkg = pkg.lowercase()
        val socialMediaPatterns = listOf(
            "instagram", "facebook", "fb.katana", "fb.orca", "whatsapp", "snapchat",
            "tiktok", "musically", "twitter", "x.android", "reddit", "pinterest",
            "threads", "telegram", "discord", "sharechat", "moj", "josh", "youtube",
            "netflix", "hotstar", "primevideo", "spotify", "pubg", "freefire", "bgmi",
            "clash", "ludo", "candycrush"
        )
        if (socialMediaPatterns.any { lowerPkg.contains(it) }) {
            return true
        }
        val allowedKeywords = config.allowedKeywordsList()
        val appLabelLower = resolveAppLabel(pkg).lowercase()
        val isAllowedStudyApp = allowedKeywords.any { kw ->
            lowerPkg.contains(kw) || appLabelLower.contains(kw)
        }
        return !isAllowedStudyApp
    }

    private suspend fun triggerNaamJapBlocker(
        appLabel: String,
        slotTitle: String,
        windowText: String,
        badgeText: String,
        ruleText: String,
        blockedCount: Int,
        todayJap: Int,
        allTimeJap: Int
    ) {
        withContext(Dispatchers.Main) {
            val shown = overlayManager.showNaamJapBlockerOverlay(
                blockedAppLabel = appLabel,
                slotTitle = slotTitle,
                windowText = windowText,
                blockedCount = blockedCount,
                initialTodayNaamJap = todayJap,
                initialAllTimeNaamJap = allTimeJap,
                headerBadgeText = badgeText,
                ruleMessage = ruleText,
                returnButtonText = "GO HOME · BACK TO SCHEDULE",
                endButtonText = "EMERGENCY EXIT SLOT LOCK",
                dimScreenForSleep = false,
                onIncrementNaamJap = {
                    serviceScope.launch { naamJapRepository.incrementToday(1) }
                },
                onReturnHome = { /* Sent safely to Home */ },
                onEndGuard = { stopShield() }
            )
            if (!shown) {
                val lockIntent = Intent(applicationContext, BedtimeLockActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra(BedtimeLockActivity.EXTRA_BLOCKED_APP, appLabel)
                    putExtra(BedtimeLockActivity.EXTRA_SLOT_TITLE, slotTitle)
                    putExtra(BedtimeLockActivity.EXTRA_SLEEP_WINDOW, windowText)
                    putExtra(BedtimeLockActivity.EXTRA_BLOCKED_COUNT, blockedCount)
                    putExtra(BedtimeLockActivity.EXTRA_IS_PREVIEW, false)
                    putExtra(BedtimeLockActivity.EXTRA_IS_STUDY_MODE, true)
                }
                runCatching { startActivity(lockIntent) }
            }
        }
    }

    private fun detectForegroundPackage(): String? {
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
                val resumed = event.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                    @Suppress("DEPRECATION") (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND)
                if (resumed && event.timeStamp >= latestTs) {
                    latestTs = event.timeStamp
                    latestPkg = event.packageName
                }
            }
            latestPkg
        } catch (_: Exception) {
            null
        }
    }

    private fun isDefaultLauncher(pkg: String): Boolean {
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

    private fun registerScreenReceiver() {
        if (screenReceiverRegistered) return
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(screenReceiver, filter)
        screenReceiverRegistered = true
    }

    private fun unregisterScreenReceiver() {
        if (!screenReceiverRegistered) return
        runCatching { unregisterReceiver(screenReceiver) }
        screenReceiverRegistered = false
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "24H Slot & Study Shield",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active 24H Dincharya slot app lock and Study Mode shield using full-screen Naam Jap blocker."
                setShowBadge(false)
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            10,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            this,
            11,
            Intent(this, StudyGuardService::class.java).apply { action = ACTION_STOP_STUDY },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("24 Hrs · Slot & Study Lock Active")
            .setContentText(text)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openIntent)
            .addAction(0, "End Lock", stopIntent)
            .build()
    }

    override fun onDestroy() {
        monitorJob?.cancel()
        unregisterScreenReceiver()
        overlayManager.dismiss()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
