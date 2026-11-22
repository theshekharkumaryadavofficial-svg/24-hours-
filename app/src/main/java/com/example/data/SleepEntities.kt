package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar
import java.util.Locale

@Entity(tableName = "sleep_days")
data class SleepDayEntity(
    @PrimaryKey val dayIndex: Int, // 0 = Mon .. 6 = Sun (current week slots, populated ONLY with real data)
    val dateKey: String,           // e.g. "2026-11-21"
    val dayShort: String,          // "Mon".."Sun"
    val dayFull: String,           // "Monday".."Sunday"
    val bedtimeHour: Int,          // 0..23
    val bedtimeMinute: Int,        // 0..59
    val wakeHour: Int,             // 0..23
    val wakeMinute: Int,           // 0..59
    val durationMinutes: Int,      // Real recorded duration in minutes
    val deepPercent: Int,          // Derived from motionless / uninterrupted sleep spans
    val remPercent: Int,           // Derived from late-cycle uninterrupted spans
    val lightPercent: Int,         // Remaining sleep proportion
    val awakeMinutes: Int,         // Real screen-on / awake interruption minutes
    val distractionBlocks: Int = 0,// Real count of blocked distracting app launches
    val screenWakeUps: Int = 0,    // Real count of screen wake events during bedtime
    val batteryUsedPercent: Int = 0,// Real battery % delta during sleep session
    val source: String = "LIVE_TIMER" // "AUTO_BEDTIME", "LIVE_TIMER", "MANUAL_LOG"
) {
    val bedtimeFormatted: String
        get() = format24h(bedtimeHour, bedtimeMinute)

    val wakeTimeFormatted: String
        get() = format24h(wakeHour, wakeMinute)

    val durationHoursFloat: Float
        get() = durationMinutes / 60f

    val durationCompact: String
        get() {
            val h = durationMinutes / 60
            val m = durationMinutes % 60
            return "${h}h ${m}m"
        }

    val durationDecimalLabel: String
        get() = String.format(Locale.US, "%.1fh", durationMinutes / 60f)

    val sourceBadgeLabel: String
        get() = when (source) {
            "AUTO_BEDTIME" -> "AUTO DETECTED"
            "LIVE_TIMER" -> "LIVE RECORDED"
            else -> "MANUAL LOG"
        }
}

@Entity(tableName = "sleep_goal")
data class SleepGoalEntity(
    @PrimaryKey val id: Int = 1,
    val targetBedtimeHour: Int = 2,      // Example default: 02:00
    val targetBedtimeMinute: Int = 0,
    val targetWakeHour: Int = 7,         // Example default: 07:00
    val targetWakeMinute: Int = 0,
    val targetDurationMinutes: Int = 300,
    val autoBedtimeEnabled: Boolean = true,
    val silenceCallsAndSms: Boolean = true,
    val blockAppsInBedtime: Boolean = true,
    val batterySaverAssist: Boolean = true
) {
    val targetBedtimeFormatted: String
        get() = format24h(targetBedtimeHour, targetBedtimeMinute)

    val targetWakeFormatted: String
        get() = format24h(targetWakeHour, targetWakeMinute)

    val targetHoursFloat: Float
        get() = targetDurationMinutes / 60f

    val targetDurationCompact: String
        get() {
            val h = targetDurationMinutes / 60
            val m = targetDurationMinutes % 60
            return if (m == 0) "${h}h 00m" else "${h}h ${m}m"
        }

    /** Returns true if the given 24h clock time falls inside [targetBedtime, targetWake). */
    fun isTimeWithinBedtimeWindow(hour: Int, minute: Int): Boolean {
        val now = hour * 60 + minute
        val start = targetBedtimeHour * 60 + targetBedtimeMinute
        val end = targetWakeHour * 60 + targetWakeMinute
        return if (start < end) {
            now in start until end
        } else {
            now >= start || now < end
        }
    }
}

@Entity(tableName = "active_sleep_session")
data class ActiveSleepSessionEntity(
    @PrimaryKey val id: Int = 1,
    val isActive: Boolean = false,
    val isAutoTriggered: Boolean = false,
    val startEpochMillis: Long = 0L,
    val startHour: Int = 0,
    val startMinute: Int = 0,
    val startBatteryPercent: Int = -1,
    val screenWakeCount: Int = 0,
    val screenOnSeconds: Long = 0L,
    val blockedAttemptsCount: Int = 0,
    val lastBlockedAppName: String = "",
    val stillSamples: Int = 0,
    val totalMotionSamples: Int = 0
)

data class WeekDaySlot(
    val dayIndex: Int,
    val dayShort: String,
    val dayFull: String
)

val WEEK_DAY_SLOTS: List<WeekDaySlot> = listOf(
    WeekDaySlot(0, "Mon", "Monday"),
    WeekDaySlot(1, "Tue", "Tuesday"),
    WeekDaySlot(2, "Wed", "Wednesday"),
    WeekDaySlot(3, "Thu", "Thursday"),
    WeekDaySlot(4, "Fri", "Friday"),
    WeekDaySlot(5, "Sat", "Saturday"),
    WeekDaySlot(6, "Sun", "Sunday")
)

fun currentMondayZeroDayIndex(): Int {
    return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 0
        Calendar.TUESDAY -> 1
        Calendar.WEDNESDAY -> 2
        Calendar.THURSDAY -> 3
        Calendar.FRIDAY -> 4
        Calendar.SATURDAY -> 5
        else -> 6
    }
}

fun todayDateKey(): String {
    val cal = Calendar.getInstance()
    return String.format(
        Locale.US,
        "%04d-%02d-%02d",
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH) + 1,
        cal.get(Calendar.DAY_OF_MONTH)
    )
}

fun format24h(hour: Int, minute: Int): String =
    String.format(Locale.US, "%02d:%02d", ((hour % 24) + 24) % 24, ((minute % 60) + 60) % 60)

fun computeDurationMinutesAcrossMidnight(
    bedHour: Int,
    bedMinute: Int,
    wakeHour: Int,
    wakeMinute: Int
): Int {
    val bedTotal = bedHour * 60 + bedMinute
    val wakeTotal = wakeHour * 60 + wakeMinute
    val raw = if (wakeTotal > bedTotal) wakeTotal - bedTotal else (24 * 60 - bedTotal) + wakeTotal
    return raw.coerceIn(15, 16 * 60)
}
