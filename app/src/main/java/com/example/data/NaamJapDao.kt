package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface NaamJapDao {
    @Query("SELECT * FROM naam_jap_days ORDER BY dateKey DESC")
    fun observeAllHistory(): Flow<List<NaamJapDayEntity>>

    @Query("SELECT COALESCE(SUM(count), 0) FROM naam_jap_days")
    fun observeAllTimeTotal(): Flow<Int>

    @Query("SELECT COALESCE(SUM(count), 0) FROM naam_jap_days")
    suspend fun getAllTimeTotalOnce(): Int

    @Query("SELECT * FROM naam_jap_days WHERE dateKey = :dateKey")
    fun observeDay(dateKey: String): Flow<NaamJapDayEntity?>

    @Query("SELECT * FROM naam_jap_days WHERE dateKey = :dateKey")
    suspend fun getDayOnce(dateKey: String): NaamJapDayEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDay(day: NaamJapDayEntity)

    @Query("SELECT * FROM naam_jap_goal WHERE id = 1")
    fun observeGoal(): Flow<NaamJapGoalEntity?>

    @Query("SELECT * FROM naam_jap_goal WHERE id = 1")
    suspend fun getGoalOnce(): NaamJapGoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGoal(goal: NaamJapGoalEntity)

    @Transaction
    suspend fun incrementDayAtomic(
        dateKey: String,
        displayDate: String,
        dayName: String,
        delta: Int
    ): Int {
        val goal = getGoalOnce()?.dailyGoal ?: 1080
        val existing = getDayOnce(dateKey)
        val newCount = ((existing?.count ?: 0) + delta).coerceAtLeast(0)
        upsertDay(
            NaamJapDayEntity(
                dateKey = dateKey,
                displayDate = existing?.displayDate ?: displayDate,
                dayName = existing?.dayName ?: dayName,
                count = newCount,
                goalAtDay = goal,
                lastUpdatedEpoch = System.currentTimeMillis()
            )
        )
        return newCount
    }
}
