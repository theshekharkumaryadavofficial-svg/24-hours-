package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep_days ORDER BY dayIndex ASC")
    fun observeAllDays(): Flow<List<SleepDayEntity>>

    @Query("SELECT * FROM sleep_days WHERE dayIndex = :dayIndex")
    suspend fun getDayOnce(dayIndex: Int): SleepDayEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDay(day: SleepDayEntity)

    @Query("DELETE FROM sleep_days WHERE dayIndex = :dayIndex")
    suspend fun deleteDay(dayIndex: Int)

    @Query("SELECT * FROM sleep_goal WHERE id = 1")
    fun observeGoal(): Flow<SleepGoalEntity?>

    @Query("SELECT * FROM sleep_goal WHERE id = 1")
    suspend fun getGoalOnce(): SleepGoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGoal(goal: SleepGoalEntity)

    @Query("SELECT * FROM active_sleep_session WHERE id = 1")
    fun observeActiveSession(): Flow<ActiveSleepSessionEntity?>

    @Query("SELECT * FROM active_sleep_session WHERE id = 1")
    suspend fun getActiveSessionOnce(): ActiveSleepSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertActiveSession(session: ActiveSleepSessionEntity)
}
