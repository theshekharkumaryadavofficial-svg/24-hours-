package com.example.data

import kotlin.math.abs
import kotlin.math.roundToInt

enum class SleepCycleType {
    DEEP,
    LIGHT,
    REM,
    AWAKE
}

data class SleepCycleStageMetric(
    val type: SleepCycleType,
    val label: String,
    val minutes: Int,
    val percent: Int,
    val optimalRangeLabel: String,
    val statusLabel: String,
    val isOptimal: Boolean
) {
    val formattedDuration: String
        get() {
            val h = minutes / 60
            val m = minutes % 60
            return if (h > 0) "${h}h ${m}m" else "${m}m"
        }
}

data class SleepCycleInsight(
    val title: String,
    val detail: String,
    val isPositive: Boolean
)

data class GoalAlignment(
    val durationProgressFraction: Float,
    val durationProgressPercent: Int,
    val durationDeltaMinutes: Int,
    val bedtimeDiffMinutes: Int,
    val wakeDiffMinutes: Int,
    val isDurationGoalMet: Boolean,
    val isBedtimeOnTrack: Boolean,
    val isWakeOnTrack: Boolean
) {
    val durationDeltaText: String
        get() = when {
            durationDeltaMinutes == 0 -> "Exact target hit"
            durationDeltaMinutes > 0 -> "+${formatMins(durationDeltaMinutes)} above goal"
            else -> "-${formatMins(abs(durationDeltaMinutes))} from goal"
        }

    val bedtimeAlignmentText: String
        get() = when {
            abs(bedtimeDiffMinutes) <= 15 -> "On target"
            bedtimeDiffMinutes > 0 -> "${bedtimeDiffMinutes}m late"
            else -> "${abs(bedtimeDiffMinutes)}m early"
        }

    val wakeAlignmentText: String
        get() = when {
            abs(wakeDiffMinutes) <= 15 -> "On target"
            wakeDiffMinutes > 0 -> "${wakeDiffMinutes}m late"
            else -> "${abs(wakeDiffMinutes)}m early"
        }

    private fun formatMins(mins: Int): String {
        val h = mins / 60
        val m = mins % 60
        return if (h > 0 && m > 0) "${h}h ${m}m" else if (h > 0) "${h}h" else "${m}m"
    }
}

data class SleepCycleAnalysis(
    val day: SleepDayEntity,
    val qualityScore: Int,
    val qualityVerdict: String,
    val completedCycles: Int,
    val stages: List<SleepCycleStageMetric>,
    val goalAlignment: GoalAlignment,
    val insights: List<SleepCycleInsight>
)

data class WeeklySleepSummary(
    val totalWeekMinutes: Int,
    val avgNightMinutes: Int,
    val bestDay: SleepDayEntity?,
    val worstDay: SleepDayEntity?,
    val goalsHitCount: Int,
    val recordedDaysCount: Int,
    val totalDistractionsBlocked: Int,
    val weeklyConsistencyPercent: Int,
    val avgDeepPercent: Int,
    val avgRemPercent: Int,
    val avgAwakeMinutes: Int
) {
    val totalWeekCompact: String
        get() = if (recordedDaysCount == 0) "0h 0m" else "${totalWeekMinutes / 60}h ${totalWeekMinutes % 60}m"

    val avgNightCompact: String
        get() = if (recordedDaysCount == 0) "--" else "${avgNightMinutes / 60}h ${avgNightMinutes % 60}m"
}

object SleepAnalyzer {

    fun analyzeDay(day: SleepDayEntity, goal: SleepGoalEntity): SleepCycleAnalysis {
        val totalInBed = (day.durationMinutes + day.awakeMinutes).coerceAtLeast(1)
        val awakePercent = ((day.awakeMinutes.toFloat() / totalInBed) * 100f).roundToInt().coerceIn(0, 35)
        val sleepPercentPool = (100 - awakePercent).coerceAtLeast(65)

        val rawSum = (day.deepPercent + day.remPercent + day.lightPercent).coerceAtLeast(1)
        val deepPct = ((day.deepPercent.toFloat() / rawSum) * sleepPercentPool).roundToInt()
        val remPct = ((day.remPercent.toFloat() / rawSum) * sleepPercentPool).roundToInt()
        val lightPct = (sleepPercentPool - deepPct - remPct).coerceAtLeast(1)

        val deepMinutes = ((day.durationMinutes * day.deepPercent) / rawSum.toFloat()).roundToInt()
        val remMinutes = ((day.durationMinutes * day.remPercent) / rawSum.toFloat()).roundToInt()
        val lightMinutes = (day.durationMinutes - deepMinutes - remMinutes).coerceAtLeast(0)

        val deepOptimal = deepPct in 18..30
        val remOptimal = remPct in 18..26
        val lightOptimal = lightPct in 40..60
        val awakeOptimal = day.awakeMinutes <= 25

        val stages = listOf(
            SleepCycleStageMetric(
                type = SleepCycleType.DEEP,
                label = "Deep Sleep",
                minutes = deepMinutes,
                percent = deepPct,
                optimalRangeLabel = "18-28%",
                statusLabel = when {
                    deepPct >= 20 -> "Strong recovery"
                    deepPct >= 15 -> "Moderate"
                    else -> "Low deep"
                },
                isOptimal = deepOptimal
            ),
            SleepCycleStageMetric(
                type = SleepCycleType.LIGHT,
                label = "Light Sleep",
                minutes = lightMinutes,
                percent = lightPct,
                optimalRangeLabel = "42-58%",
                statusLabel = if (lightOptimal) "Balanced base" else "Slightly shifted",
                isOptimal = lightOptimal
            ),
            SleepCycleStageMetric(
                type = SleepCycleType.REM,
                label = "REM Sleep",
                minutes = remMinutes,
                percent = remPct,
                optimalRangeLabel = "19-26%",
                statusLabel = when {
                    remPct >= 20 -> "Memory boost"
                    remPct >= 15 -> "Fair focus"
                    else -> "Short REM"
                },
                isOptimal = remOptimal
            ),
            SleepCycleStageMetric(
                type = SleepCycleType.AWAKE,
                label = "Awake Time",
                minutes = day.awakeMinutes,
                percent = awakePercent,
                optimalRangeLabel = "<25m",
                statusLabel = if (awakeOptimal) "Minimal breaks" else "Interrupted",
                isOptimal = awakeOptimal
            )
        )

        val durationProgress =
            (day.durationMinutes.toFloat() / goal.targetDurationMinutes.coerceAtLeast(30)).coerceIn(0f, 1.25f)
        val durationPct = (durationProgress * 100f).roundToInt().coerceAtMost(100)
        val durationDelta = day.durationMinutes - goal.targetDurationMinutes

        val bedDiff = signedClockDiffMinutes(
            actualH = day.bedtimeHour,
            actualM = day.bedtimeMinute,
            targetH = goal.targetBedtimeHour,
            targetM = goal.targetBedtimeMinute
        )
        val wakeDiff = signedClockDiffMinutes(
            actualH = day.wakeHour,
            actualM = day.wakeMinute,
            targetH = goal.targetWakeHour,
            targetM = goal.targetWakeMinute
        )

        val isGoalMet = day.durationMinutes >= (goal.targetDurationMinutes - 15)
        val alignment = GoalAlignment(
            durationProgressFraction = durationProgress.coerceIn(0f, 1f),
            durationProgressPercent = durationPct,
            durationDeltaMinutes = durationDelta,
            bedtimeDiffMinutes = bedDiff,
            wakeDiffMinutes = wakeDiff,
            isDurationGoalMet = isGoalMet,
            isBedtimeOnTrack = abs(bedDiff) <= 20,
            isWakeOnTrack = abs(wakeDiff) <= 20
        )

        val completedCycles = (day.durationMinutes / 90f).roundToInt().coerceAtLeast(1)

        val durationScore = (durationProgress.coerceAtMost(1f) * 45f)
        val deepScore = if (deepOptimal) 20f else if (deepPct >= 14) 14f else 9f
        val remScore = if (remOptimal) 15f else if (remPct >= 15) 11f else 7f
        val consistencyScore = if (abs(bedDiff) <= 15) 12f else if (abs(bedDiff) <= 40) 8f else 4f
        val efficiencyScore = if (day.awakeMinutes <= 20) 8f else if (day.awakeMinutes <= 35) 5f else 2f
        val qualityScore = (durationScore + deepScore + remScore + consistencyScore + efficiencyScore)
            .roundToInt()
            .coerceIn(25, 99)

        val verdict = when {
            qualityScore >= 88 -> "Excellent"
            qualityScore >= 76 -> "Restful"
            qualityScore >= 60 -> "Fair"
            else -> "Short Rest"
        }

        val insights = buildList {
            if (day.distractionBlocks > 0 || day.screenWakeUps > 0) {
                add(
                    SleepCycleInsight(
                        title = "Bedtime Guard Telemetry",
                        detail = "Blocked ${day.distractionBlocks} distracting app open(s) across ${day.screenWakeUps} screen wake-up(s), using ${day.batteryUsedPercent}% battery overnight.",
                        isPositive = day.distractionBlocks == 0
                    )
                )
            }

            if (deepPct >= 20) {
                add(
                    SleepCycleInsight(
                        title = "Deep Cycle Restoration ($deepPct%)",
                        detail = "Uninterrupted stillness delivered ${deepMinutes / 60}h ${deepMinutes % 60}m of deep sleep across ~$completedCycles sleep cycles.",
                        isPositive = true
                    )
                )
            } else {
                add(
                    SleepCycleInsight(
                        title = "Boost Deep Sleep ($deepPct%)",
                        detail = "Keep phone locked during ${goal.targetBedtimeFormatted}-${goal.targetWakeFormatted} so early deep cycles stay uninterrupted.",
                        isPositive = false
                    )
                )
            }

            if (alignment.isBedtimeOnTrack && alignment.isDurationGoalMet) {
                add(
                    SleepCycleInsight(
                        title = "Sleep Window On Track",
                        detail = "Slept ${day.bedtimeFormatted} -> ${day.wakeTimeFormatted}, aligning with your ${goal.targetDurationCompact} daily target.",
                        isPositive = true
                    )
                )
            } else {
                val shiftNote = when {
                    bedDiff > 15 -> "Bedtime started ${bedDiff}m past ${goal.targetBedtimeFormatted}."
                    durationDelta < 0 -> "Finished ${abs(durationDelta)}m short of your ${goal.targetDurationCompact} goal."
                    else -> "Bedtime started ${abs(bedDiff)}m ahead of ${goal.targetBedtimeFormatted}."
                }
                add(
                    SleepCycleInsight(
                        title = "Schedule Alignment",
                        detail = "$shiftNote Auto Bedtime Guard helps lock in consistent sleep hours.",
                        isPositive = false
                    )
                )
            }
        }

        return SleepCycleAnalysis(
            day = day,
            qualityScore = qualityScore,
            qualityVerdict = verdict,
            completedCycles = completedCycles,
            stages = stages,
            goalAlignment = alignment,
            insights = insights
        )
    }

    fun summarizeWeek(days: List<SleepDayEntity>, goal: SleepGoalEntity): WeeklySleepSummary {
        if (days.isEmpty()) {
            return WeeklySleepSummary(
                totalWeekMinutes = 0,
                avgNightMinutes = 0,
                bestDay = null,
                worstDay = null,
                goalsHitCount = 0,
                recordedDaysCount = 0,
                totalDistractionsBlocked = 0,
                weeklyConsistencyPercent = 0,
                avgDeepPercent = 0,
                avgRemPercent = 0,
                avgAwakeMinutes = 0
            )
        }
        val totalMins = days.sumOf { it.durationMinutes }
        val avgMins = totalMins / days.size
        val best = days.maxByOrNull { it.durationMinutes }
        val worst = days.minByOrNull { it.durationMinutes }
        val goalsHit = days.count { it.durationMinutes >= (goal.targetDurationMinutes - 15) }
        val totalBlocked = days.sumOf { it.distractionBlocks }
        val avgBedDiff = days.map {
            abs(signedClockDiffMinutes(it.bedtimeHour, it.bedtimeMinute, goal.targetBedtimeHour, goal.targetBedtimeMinute))
        }.average()
        val consistency = (100.0 - (avgBedDiff * 0.9)).roundToInt().coerceIn(30, 99)

        return WeeklySleepSummary(
            totalWeekMinutes = totalMins,
            avgNightMinutes = avgMins,
            bestDay = best,
            worstDay = worst,
            goalsHitCount = goalsHit,
            recordedDaysCount = days.size,
            totalDistractionsBlocked = totalBlocked,
            weeklyConsistencyPercent = consistency,
            avgDeepPercent = days.map { it.deepPercent }.average().roundToInt(),
            avgRemPercent = days.map { it.remPercent }.average().roundToInt(),
            avgAwakeMinutes = days.map { it.awakeMinutes }.average().roundToInt()
        )
    }

    fun signedClockDiffMinutes(actualH: Int, actualM: Int, targetH: Int, targetM: Int): Int {
        val actual = actualH * 60 + actualM
        val target = targetH * 60 + targetM
        var diff = actual - target
        if (diff > 720) diff -= 1440
        if (diff < -720) diff += 1440
        return diff
    }
}
