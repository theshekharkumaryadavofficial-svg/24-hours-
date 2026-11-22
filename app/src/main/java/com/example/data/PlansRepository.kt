package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlansRepository(private val dao: PlansDao) {

    val allBlocks: Flow<List<DailyPlanBlockEntity>> = dao.observeAllBlocks()

    val studyConfig: Flow<StudyModeConfigEntity> =
        dao.observeStudyConfig().map { it ?: StudyModeConfigEntity() }

    val disciplineConfig: Flow<DailyDisciplineConfigEntity> =
        dao.observeDisciplineConfig().map { it ?: DailyDisciplineConfigEntity() }

    suspend fun ensureInitialized() {
        if (dao.getStudyConfigOnce() == null) {
            dao.upsertStudyConfig(StudyModeConfigEntity())
        }
        if (dao.getDisciplineConfigOnce() == null) {
            dao.upsertDisciplineConfig(DailyDisciplineConfigEntity(trackerDateKey = todayDateKey()))
        }
    }

    suspend fun getAllBlocksOnce(): List<DailyPlanBlockEntity> = dao.getAllBlocksOnce()

    suspend fun getStudyConfigOnce(): StudyModeConfigEntity =
        dao.getStudyConfigOnce() ?: StudyModeConfigEntity()

    suspend fun getDisciplineConfigOnce(): DailyDisciplineConfigEntity =
        dao.getDisciplineConfigOnce() ?: DailyDisciplineConfigEntity(trackerDateKey = todayDateKey())

    suspend fun saveStudyConfig(
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        autoStudyEnabled: Boolean,
        blockSocialMedia: Boolean,
        allowedKeywordsCsv: String
    ): StudyModeConfigEntity {
        val existing = getStudyConfigOnce()
        val updated = existing.copy(
            startHour = startHour,
            startMinute = startMinute,
            endHour = endHour,
            endMinute = endMinute,
            autoStudyEnabled = autoStudyEnabled,
            blockSocialMedia = blockSocialMedia,
            allowedStudyKeywordsCsv = allowedKeywordsCsv
        )
        dao.upsertStudyConfig(updated)
        return updated
    }

    suspend fun setStudyModeActive(active: Boolean) {
        val existing = getStudyConfigOnce()
        dao.upsertStudyConfig(existing.copy(isActiveNow = active))
    }

    suspend fun incrementStudyBlockedCount(): Int {
        val existing = getStudyConfigOnce()
        val today = todayDateKey()
        val nextCount = if (existing.blockedDateKey == today) existing.blockedCountToday + 1 else 1
        dao.upsertStudyConfig(
            existing.copy(
                blockedCountToday = nextCount,
                blockedDateKey = today
            )
        )
        return nextCount
    }

    suspend fun addWaterMl(deltaMl: Int) {
        val existing = getDisciplineConfigOnce()
        val today = todayDateKey()
        val base = if (existing.trackerDateKey == today) existing.waterConsumedMl else 0
        val mealsBase = if (existing.trackerDateKey == today) existing.lightMealsLogged else 0
        dao.upsertDisciplineConfig(
            existing.copy(
                waterConsumedMl = (base + deltaMl).coerceIn(0, 10_000),
                lightMealsLogged = mealsBase,
                trackerDateKey = today
            )
        )
    }

    suspend fun toggleLightMealCount(nextCount: Int) {
        val existing = getDisciplineConfigOnce()
        val today = todayDateKey()
        val waterBase = if (existing.trackerDateKey == today) existing.waterConsumedMl else 0
        dao.upsertDisciplineConfig(
            existing.copy(
                waterConsumedMl = waterBase,
                lightMealsLogged = nextCount.coerceIn(0, existing.lightMealsTarget),
                trackerDateKey = today
            )
        )
    }

    suspend fun saveDisciplineReminderSettings(
        waterGoalMl: Int,
        lightMealsTarget: Int,
        naamJapTarget: Int,
        reminderEnabled: Boolean,
        intervalMinutes: Int
    ): DailyDisciplineConfigEntity {
        val existing = getDisciplineConfigOnce()
        val updated = existing.copy(
            waterGoalMl = waterGoalMl.coerceIn(1000, 8000),
            lightMealsTarget = lightMealsTarget.coerceIn(1, 5),
            naamJapReminderTarget = naamJapTarget.coerceIn(108, 108_000),
            reminderEnabled = reminderEnabled,
            reminderIntervalMinutes = intervalMinutes.coerceIn(5, 60)
        )
        dao.upsertDisciplineConfig(updated)
        return updated
    }

    suspend fun savePlanBlock(block: DailyPlanBlockEntity) {
        dao.upsertBlock(block)
    }

    suspend fun toggleBlockCompleted(block: DailyPlanBlockEntity) {
        val today = todayDateKey()
        val nextDateKey = if (block.completedDateKey == today) "" else today
        dao.upsertBlock(block.copy(completedDateKey = nextDateKey))
    }

    suspend fun deletePlanBlock(id: Int) {
        dao.deleteBlockById(id)
    }

    /**
     * Generates a structured Smart Full-Day 24-Hour Dincharya tailored to the user's
     * wake-up time, study window (e.g. 17:00-22:00), work block, 4L water, light balanced meals,
     * and 20,000 Radha Naam Jap target.
     */
    suspend fun generateSmartFullDaySchedule(
        wakeHour: Int,
        studyStartHour: Int,
        studyEndHour: Int,
        workStartHour: Int,
        workEndHour: Int
    ) {
        dao.clearAllBlocks()
        val w = wakeHour.coerceIn(4, 9)
        val blocks = listOf(
            DailyPlanBlockEntity(
                title = "Wake Up · 500ml Water & Freshness",
                category = "HEALTH",
                startHour = w,
                startMinute = 0,
                endHour = w,
                endMinute = 30,
                notes = "Start hydration towards 4L daily goal & sunrise breathing"
            ),
            DailyPlanBlockEntity(
                title = "Morning Radha Naam Jap & Sankalp",
                category = "SPIRITUAL",
                startHour = w,
                startMinute = 30,
                endHour = (w + 1) % 24,
                endMinute = 30,
                notes = "Aim for 5,000+ focused Radha Naam count in calm morning hours"
            ),
            DailyPlanBlockEntity(
                title = "Fitness / Walk & Light Balanced Breakfast",
                category = "HEALTH",
                startHour = (w + 1) % 24,
                startMinute = 30,
                endHour = (w + 2) % 24,
                endMinute = 30,
                notes = "Keep breakfast light & nutritious + 500ml water"
            ),
            DailyPlanBlockEntity(
                title = "Core Work & High-Priority Tasks",
                category = "WORK",
                startHour = workStartHour,
                startMinute = 0,
                endHour = (workStartHour + 3).coerceAtMost(workEndHour),
                endMinute = 0,
                notes = "Finish highest-impact work tasks + 1L hydration checkpoint"
            ),
            DailyPlanBlockEntity(
                title = "Light Mindful Lunch & Naam Jap Sprint",
                category = "HEALTH",
                startHour = 13,
                startMinute = 0,
                endHour = 14,
                endMinute = 0,
                notes = "Eat light & balanced portion (no overeating) + 5,000 Naam Jap"
            ),
            DailyPlanBlockEntity(
                title = "Afternoon Work / Notes Revision",
                category = "WORK",
                startHour = 14,
                startMinute = 0,
                endHour = workEndHour.coerceAtLeast(15),
                endMinute = 30,
                notes = "Wrap up pending work & prepare study syllabus + 1L water"
            ),
            DailyPlanBlockEntity(
                title = "Deep Study Mode (Social Media Blocked)",
                category = "STUDY",
                startHour = studyStartHour,
                startMinute = 0,
                endHour = studyEndHour,
                endMinute = 0,
                notes = "Red Foreground Shield locks social media · Only study apps allowed + 1L water",
                enablesStudyBlock = true
            ),
            DailyPlanBlockEntity(
                title = "Light Early Dinner & Evening Naam Jap",
                category = "SPIRITUAL",
                startHour = studyEndHour % 24,
                startMinute = 0,
                endHour = (studyEndHour + 1) % 24,
                endMinute = 0,
                notes = "Complete remaining Naam Jap toward 20,000 & light mindful meal"
            ),
            DailyPlanBlockEntity(
                title = "Night Review & Auto Bedtime Prep",
                category = "ROUTINE",
                startHour = (studyEndHour + 1) % 24,
                startMinute = 0,
                endHour = (studyEndHour + 2) % 24,
                endMinute = 0,
                notes = "Review tomorrow's timetable & put phone away for Sleep Guard"
            )
        )
        dao.upsertBlocks(blocks)
    }
}
