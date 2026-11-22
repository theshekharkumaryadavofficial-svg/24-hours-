package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NaamJapRepository(private val dao: NaamJapDao) {

    val allHistory: Flow<List<NaamJapDayEntity>> = dao.observeAllHistory()

    val allTimeTotal: Flow<Int> = dao.observeAllTimeTotal()

    val goal: Flow<NaamJapGoalEntity> = dao.observeGoal().map { it ?: NaamJapGoalEntity() }

    fun observeToday(dateKey: String): Flow<NaamJapDayEntity?> = dao.observeDay(dateKey)

    suspend fun ensureGoalInitialized() {
        if (dao.getGoalOnce() == null) {
            dao.upsertGoal(NaamJapGoalEntity())
        }
    }

    suspend fun getTodayAndAllTimeOnce(): Pair<Int, Int> {
        val today = dao.getDayOnce(todayDateKey())?.count ?: 0
        val total = dao.getAllTimeTotalOnce()
        return today to total
    }

    suspend fun incrementToday(delta: Int = 1): Int {
        return dao.incrementDayAtomic(
            dateKey = todayDateKey(),
            displayDate = todayDisplayDate(),
            dayName = todayDayFullName(),
            delta = delta
        )
    }

    suspend fun saveGoal(dailyGoal: Int, hapticEnabled: Boolean) {
        val clamped = dailyGoal.coerceIn(11, 108_000)
        dao.upsertGoal(
            NaamJapGoalEntity(
                id = 1,
                dailyGoal = clamped,
                hapticEnabled = hapticEnabled
            )
        )
        // Update today's goal snapshot if today already has an entry
        val todayKey = todayDateKey()
        val existing = dao.getDayOnce(todayKey)
        if (existing != null) {
            dao.upsertDay(existing.copy(goalAtDay = clamped))
        }
    }
}
