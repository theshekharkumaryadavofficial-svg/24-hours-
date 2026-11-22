package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "naam_jap_days")
data class NaamJapDayEntity(
    @PrimaryKey val dateKey: String,   // "YYYY-MM-DD"
    val displayDate: String,           // e.g. "21 Nov"
    val dayName: String,               // e.g. "Saturday"
    val count: Int = 0,
    val goalAtDay: Int = 1080,
    val lastUpdatedEpoch: Long = System.currentTimeMillis()
) {
    val completedMalas: Int
        get() = count / 108

    val beadInCurrentMala: Int
        get() = count % 108

    val isGoalAchieved: Boolean
        get() = count >= goalAtDay && goalAtDay > 0
}

@Entity(tableName = "naam_jap_goal")
data class NaamJapGoalEntity(
    @PrimaryKey val id: Int = 1,
    val dailyGoal: Int = 1080,         // Default: 10 Malas (1080)
    val hapticEnabled: Boolean = true
) {
    val targetMalas: Int
        get() = (dailyGoal / 108).coerceAtLeast(1)
}

fun todayDisplayDate(): String =
    SimpleDateFormat("dd MMM", Locale.US).format(Date())

fun todayDayFullName(): String =
    SimpleDateFormat("EEEE", Locale.US).format(Date())
