package com.example.bedtime

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.SleepGoalEntity
import java.util.Calendar

object BedtimeScheduler {

    const val ACTION_BEDTIME_START = "com.example.bedtime.ACTION_BEDTIME_START"
    const val ACTION_BEDTIME_END = "com.example.bedtime.ACTION_BEDTIME_END"

    private const val REQ_CODE_START = 2401
    private const val REQ_CODE_END = 2402

    fun syncSchedule(context: Context, goal: SleepGoalEntity) {
        if (!goal.autoBedtimeEnabled) {
            cancelAll(context)
            return
        }
        scheduleNextDailyAlarm(
            context = context,
            action = ACTION_BEDTIME_START,
            requestCode = REQ_CODE_START,
            hour = goal.targetBedtimeHour,
            minute = goal.targetBedtimeMinute
        )
        scheduleNextDailyAlarm(
            context = context,
            action = ACTION_BEDTIME_END,
            requestCode = REQ_CODE_END,
            hour = goal.targetWakeHour,
            minute = goal.targetWakeMinute
        )
    }

    fun cancelAll(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(buildPendingIntent(context, ACTION_BEDTIME_START, REQ_CODE_START))
        am.cancel(buildPendingIntent(context, ACTION_BEDTIME_END, REQ_CODE_END))
    }

    private fun scheduleNextDailyAlarm(
        context: Context,
        action: String,
        requestCode: Int,
        hour: Int,
        minute: Int
    ) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = nextOccurrenceMillis(hour, minute)
        val pendingIntent = buildPendingIntent(context, action, requestCode)

        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            am.canScheduleExactAlarms()
        } else {
            true
        }

        try {
            if (canExact) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        } catch (_: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    private fun buildPendingIntent(context: Context, action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, BedtimeAlarmReceiver::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun nextOccurrenceMillis(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.timeInMillis <= now.timeInMillis + 1500L) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis
    }
}
