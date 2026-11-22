package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlansDao {
    @Query("SELECT * FROM daily_plan_blocks ORDER BY startHour ASC, startMinute ASC")
    fun observeAllBlocks(): Flow<List<DailyPlanBlockEntity>>

    @Query("SELECT * FROM daily_plan_blocks ORDER BY startHour ASC, startMinute ASC")
    suspend fun getAllBlocksOnce(): List<DailyPlanBlockEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBlock(block: DailyPlanBlockEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBlocks(blocks: List<DailyPlanBlockEntity>)

    @Query("DELETE FROM daily_plan_blocks WHERE id = :id")
    suspend fun deleteBlockById(id: Int)

    @Query("DELETE FROM daily_plan_blocks")
    suspend fun clearAllBlocks()

    @Query("SELECT * FROM study_mode_config WHERE id = 1")
    fun observeStudyConfig(): Flow<StudyModeConfigEntity?>

    @Query("SELECT * FROM study_mode_config WHERE id = 1")
    suspend fun getStudyConfigOnce(): StudyModeConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStudyConfig(config: StudyModeConfigEntity)

    @Query("SELECT * FROM daily_discipline_config WHERE id = 1")
    fun observeDisciplineConfig(): Flow<DailyDisciplineConfigEntity?>

    @Query("SELECT * FROM daily_discipline_config WHERE id = 1")
    suspend fun getDisciplineConfigOnce(): DailyDisciplineConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDisciplineConfig(config: DailyDisciplineConfigEntity)
}
