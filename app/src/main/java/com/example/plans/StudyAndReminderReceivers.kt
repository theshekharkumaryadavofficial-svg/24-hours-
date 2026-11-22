package com.example.plans

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.PlansRepository
import com.example.data.SleepDatabase
import com.example.data.SleepRepository
import com.example.data.todayDateKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class StudyAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = SleepDatabase.getInstance(context)
                val repo = PlansRepository(db.plansDao())
                val config = repo.getStudyConfigOnce()
                val blocks = repo.getAllBlocksOnce()

                val cal = java.util.Calendar.getInstance()
                val h = cal.get(java.util.Calendar.HOUR_OF_DAY)
                val m = cal.get(java.util.Calendar.MINUTE)
                val anyStrictSlotNow = blocks.any { it.strictAppLockEnabled && it.isCurrentTimeInside(h, m) }
                val insideAutoStudy = config.autoStudyEnabled && config.isTimeInsideStudyWindow(h, m)

                when (action) {
                    StudyScheduler.ACTION_STUDY_START -> {
                        if (config.autoStudyEnabled) StudyGuardService.startStudyMode(context)
                    }
                    StudyScheduler.ACTION_STUDY_END -> {
                        if (!anyStrictSlotNow) StudyGuardService.stopStudyMode(context)
                    }
                    StudyScheduler.ACTION_SLOT_BOUNDARY -> {
                        if (anyStrictSlotNow || insideAutoStudy) {
                            StudyGuardService.startStudyMode(context)
                        } else if (!config.isActiveNow) {
                            StudyGuardService.stopStudyMode(context)
                        }
                    }
                }
                StudyScheduler.syncStudySchedule(context, config)
                StudyScheduler.syncAllSlotSchedules(context, blocks)
            } finally {
                pendingResult.finish()
            }
        }
    }
}

class DisciplineReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID = "discipline_10min_reminder_channel"
        private const val NOTIFICATION_ID = 2600

        fun postDisciplineReminderNow(
            context: Context,
            waterText: String,
            lightMealsLogged: Int,
            lightMealsTarget: Int,
            naamJapToday: Int,
            naamJapTarget: Int
        ) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "10-Min Discipline Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Reminders every 10 minutes for 4L water, light balanced eating, and 20,000 Radha Naam Jap."
                }
                nm.createNotificationChannel(channel)
            }

            val openIntent = PendingIntent.getActivity(
                context,
                26,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val fmt = NumberFormat.getNumberInstance(Locale.US)
            val body = "Water: $waterText  |  Light Meals: $lightMealsLogged/$lightMealsTarget  |  Radha Naam Jap: ${fmt.format(naamJapToday)} / ${fmt.format(naamJapTarget)}"

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("24 Hrs · Stay Disciplined (10m Check-In)")
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(openIntent)
                .build()

            runCatching { nm.notify(NOTIFICATION_ID, notification) }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = SleepDatabase.getInstance(context)
                val plansRepo = PlansRepository(db.plansDao())
                val sleepRepo = SleepRepository(db.sleepDao())

                val config = plansRepo.getDisciplineConfigOnce()
                if (!config.reminderEnabled) return@launch

                // Never disturb the user if Bedtime Sleep Guard is actively running!
                val activeSleep = sleepRepo.getActiveSessionOnce()
                if (!activeSleep.isActive) {
                    val todayNaamJap = db.naamJapDao().getDayOnce(todayDateKey())?.count ?: 0
                    postDisciplineReminderNow(
                        context = context,
                        waterText = config.waterLitersFormatted,
                        lightMealsLogged = config.effectiveLightMeals,
                        lightMealsTarget = config.lightMealsTarget,
                        naamJapToday = todayNaamJap,
                        naamJapTarget = config.naamJapReminderTarget
                    )
                }

                // Self-chain next 10-minute reminder
                StudyScheduler.scheduleNextDisciplineReminder(context, config.reminderIntervalMinutes)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
