package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_plan_blocks")
data class DailyPlanBlockEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String,              // "STUDY", "WORK", "LEISURE", "HEALTH", "SPIRITUAL", "ROUTINE"
    val startHour: Int,                // 0..23
    val startMinute: Int,              // 0..59
    val endHour: Int,                  // 0..23
    val endMinute: Int,                // 0..59
    val notes: String = "",
    val enablesStudyBlock: Boolean = false,
    val completedDateKey: String = "", // Equals todayDateKey() if completed today
    val strictAppLockEnabled: Boolean = false,
    val allowedPackagesCsv: String = ""
) {
    val startFormatted: String
        get() = format24h(startHour, startMinute)

    val endFormatted: String
        get() = format24h(endHour, endMinute)

    val timeRangeFormatted: String
        get() = "$startFormatted - $endFormatted"

    val startTotalMinutes: Int
        get() = startHour * 60 + startMinute

    fun allowedPackagesSet(): Set<String> =
        allowedPackagesCsv.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()

    val allowedAppsCount: Int
        get() = allowedPackagesSet().size

    fun isPackageAllowed(packageName: String): Boolean =
        packageName in allowedPackagesSet()

    fun isCompletedToday(): Boolean = completedDateKey == todayDateKey()

    fun isCurrentTimeInside(hour: Int, minute: Int): Boolean {
        val now = hour * 60 + minute
        val start = startHour * 60 + startMinute
        val end = endHour * 60 + endMinute
        if (start == end) return false
        return if (start < end) {
            now in start until end
        } else {
            now >= start || now < end
        }
    }
}

@Entity(tableName = "study_mode_config")
data class StudyModeConfigEntity(
    @PrimaryKey val id: Int = 1,
    val startHour: Int = 17,           // Default: 17:00 (5:00 PM)
    val startMinute: Int = 0,
    val endHour: Int = 22,             // Default: 22:00 (10:00 PM)
    val endMinute: Int = 0,
    val autoStudyEnabled: Boolean = true,
    val blockSocialMedia: Boolean = true,
    val isActiveNow: Boolean = false,
    val blockedCountToday: Int = 0,
    val blockedDateKey: String = "",
    val allowedStudyKeywordsCsv: String = "docs,drive,classroom,notes,keep,pdf,notion,khan,unacademy,physicswallah,pw,allen,coursera,udemy,anki,quizlet,calculator,kindle,books,obsidian"
) {
    val startFormatted: String
        get() = format24h(startHour, startMinute)

    val endFormatted: String
        get() = format24h(endHour, endMinute)

    val windowFormatted: String
        get() = "$startFormatted - $endFormatted"

    val durationCompact: String
        get() {
            val mins = computeDurationMinutesAcrossMidnight(startHour, startMinute, endHour, endMinute)
            return "${mins / 60}h ${mins % 60}m"
        }

    val effectiveBlockedToday: Int
        get() = if (blockedDateKey == todayDateKey()) blockedCountToday else 0

    fun allowedKeywordsList(): List<String> =
        allowedStudyKeywordsCsv.split(",")
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }

    fun isTimeInsideStudyWindow(hour: Int, minute: Int): Boolean {
        val now = hour * 60 + minute
        val start = startHour * 60 + startMinute
        val end = endHour * 60 + endMinute
        return if (start < end) {
            now in start until end
        } else {
            now >= start || now < end
        }
    }
}

@Entity(tableName = "daily_discipline_config")
data class DailyDisciplineConfigEntity(
    @PrimaryKey val id: Int = 1,
    val waterGoalMl: Int = 4000,               // 4.0 Liters per day
    val waterConsumedMl: Int = 0,
    val trackerDateKey: String = "",
    val lightMealsTarget: Int = 3,             // 3 balanced light meals (kam & santulit khana)
    val lightMealsLogged: Int = 0,
    val naamJapReminderTarget: Int = 20000,    // 20,000 Naam Jap per day reminder goal
    val reminderEnabled: Boolean = false,      // Every 10 minutes smart reminder
    val reminderIntervalMinutes: Int = 10
) {
    val effectiveWaterMl: Int
        get() = if (trackerDateKey == todayDateKey()) waterConsumedMl else 0

    val effectiveLightMeals: Int
        get() = if (trackerDateKey == todayDateKey()) lightMealsLogged else 0

    val waterProgressFraction: Float
        get() = (effectiveWaterMl.toFloat() / waterGoalMl.coerceAtLeast(500)).coerceIn(0f, 1f)

    val waterLitersFormatted: String
        get() = String.format(java.util.Locale.US, "%.2fL / %.1fL", effectiveWaterMl / 1000f, waterGoalMl / 1000f)
}

data class StudyAppAllowPreset(
    val id: String,
    val label: String,
    val keywords: List<String>
)

val STUDY_APP_PRESETS: List<StudyAppAllowPreset> = listOf(
    StudyAppAllowPreset("docs", "Docs & Drive", listOf("docs", "drive", "sheets", "slides", "office", "wps")),
    StudyAppAllowPreset("pdf", "PDF & Books", listOf("pdf", "adobe", "kindle", "books", "reader")),
    StudyAppAllowPreset("notes", "Notes & Anki", listOf("notes", "keep", "notion", "obsidian", "anki", "quizlet")),
    StudyAppAllowPreset("edtech", "Study Courses", listOf("classroom", "khan", "unacademy", "physicswallah", "pw", "allen", "coursera", "udemy", "byju", "vedantu", "doubtnut")),
    StudyAppAllowPreset("calc", "Calculator & Tools", listOf("calculator", "math", "dictionary", "translate")),
    StudyAppAllowPreset("browser", "Browser (Research)", listOf("chrome", "firefox", "brave", "browser", "edge"))
)
