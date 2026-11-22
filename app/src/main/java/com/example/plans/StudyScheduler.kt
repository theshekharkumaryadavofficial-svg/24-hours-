package com.example.plans

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.bedtime.BedtimeScheduler
import com.example.data.DailyDisciplineConfigEntity
import com.example.data.DailyPlanBlockEntity
import com.example.data.StudyModeConfigEntity

object StudyScheduler {

    const val ACTION_STUDY_START = "com.example.plans.ACTION_STUDY_START"
    const val ACTION_STUDY_END = "com.example.plans.ACTION_STUDY_END"
    const val ACTION_DISCIPLINE_REMINDER = "com.example.plans.ACTION_DISCIPLINE_REMINDER"
    const val ACTION_SLOT_BOUNDARY = "com.example.plans.ACTION_SLOT_BOUNDARY"

    private const val REQ_STUDY_START = 2501
    private const val REQ_STUDY_END = 2502
    private const val REQ_REMINDER = 2503
    private const val REQ_NEXT_SLOT_BOUNDARY = 2510

    fun syncStudySchedule(context: Context, config: StudyModeConfigEntity) {
        if (!config.autoStudyEnabled) {
            cancelStudySchedule(context)
            return
        }
        scheduleDailyStudyAlarm(
            context = context,
            action = ACTION_STUDY_START,
            requestCode = REQ_STUDY_START,
            hour = config.startHour,
            minute = config.startMinute
        )
        scheduleDailyStudyAlarm(
            context = context,
            action = ACTION_STUDY_END,
            requestCode = REQ_STUDY_END,
            hour = config.endHour,
            minute = config.endMinute
        )
    }

    fun cancelStudySchedule(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(buildStudyPendingIntent(context, ACTION_STUDY_START, REQ_STUDY_START))
        am.cancel(buildStudyPendingIntent(context, ACTION_STUDY_END, REQ_STUDY_END))
    }

    /**
     * Supports UNLIMITED permanent time slots without hitting PendingIntent limits:
     * computes the single earliest upcoming start/end boundary across all strict-locked
     * 24H Dincharya slots and schedules an exact boundary alarm that self-chains.
     */
    fun syncAllSlotSchedules(context: Context, blocks: List<DailyPlanBlockEntity>) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val lockedSlots = blocks.filter { it.strictAppLockEnabled }
        if (lockedSlots.isEmpty()) {
            am.cancel(buildStudyPendingIntent(context, ACTION_SLOT_BOUNDARY, REQ_NEXT_SLOT_BOUNDARY))
            return
        }

        val now = System.currentTimeMillis()
        var earliestBoundary = Long.MAX_VALUE
        for (slot in lockedSlots) {
            val startTrigger = BedtimeScheduler.nextOccurrenceMillis(slot.startHour, slot.startMinute)
            val endTrigger = BedtimeScheduler.nextOccurrenceMillis(slot.endHour, slot.endMinute)
            if (startTrigger > now + 1_000L && startTrigger < earliestBoundary) {
                earliestBoundary = startTrigger
            }
            if (endTrigger > now + 1_000L && endTrigger < earliestBoundary) {
                earliestBoundary = endTrigger
            }
        }

        if (earliestBoundary != Long.MAX_VALUE) {
            val pending = buildStudyPendingIntent(context, ACTION_SLOT_BOUNDARY, REQ_NEXT_SLOT_BOUNDARY)
            scheduleExactOrFallback(am, earliestBoundary, pending)
        }
    }

    fun syncDisciplineReminder(context: Context, config: DailyDisciplineConfigEntity) {
        if (!config.reminderEnabled) {
            cancelDisciplineReminder(context)
            return
        }
        scheduleNextDisciplineReminder(context, config.reminderIntervalMinutes)
    }

    fun scheduleNextDisciplineReminder(context: Context, intervalMinutes: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = System.currentTimeMillis() + (intervalMinutes.coerceIn(5, 60) * 60_000L)
        val pending = buildReminderPendingIntent(context)
        scheduleExactOrFallback(am, triggerAt, pending)
    }

    fun cancelDisciplineReminder(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(buildReminderPendingIntent(context))
    }

    private fun scheduleDailyStudyAlarm(
        context: Context,
        action: String,
        requestCode: Int,
        hour: Int,
        minute: Int
    ) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = BedtimeScheduler.nextOccurrenceMillis(hour, minute)
        val pending = buildStudyPendingIntent(context, action, requestCode)
        scheduleExactOrFallback(am, triggerAt, pending)
    }

    private fun scheduleExactOrFallback(am: AlarmManager, triggerAt: Long, pending: PendingIntent) {
        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            am.canScheduleExactAlarms()
        } else {
            true
        }
        try {
            if (canExact) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            }
        } catch (_: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        }
    }

    private fun buildStudyPendingIntent(context: Context, action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, StudyAlarmReceiver::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildReminderPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, DisciplineReminderReceiver::class.java).apply {
            action = ACTION_DISCIPLINE_REMINDER
        }
        return PendingIntent.getBroadcast(
            context,
            REQ_REMINDER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
