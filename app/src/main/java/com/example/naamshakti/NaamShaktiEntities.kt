package com.example.naamshakti

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "naam_shakti_profile")
data class NaamShaktiProfileEntity(
    @PrimaryKey val id: Int = 1,
    val selectedWarriorId: String = "vajra_arjun",
    val unlockedWarriorsCsv: String = "vajra_arjun",
    val unlockedBattlefield: Boolean = false,
    val koRecoveryActive: Boolean = false,
    val currentShaktiEnergy: Int = 0,
    val lifetimeShaktiEarned: Int = 0,
    val lifetimeShaktiSpent: Int = 0,
    val syncedAllTimeNaam: Int = 0,
    val koBaselineAllTimeNaam: Int = 0,
    val battlesWon: Int = 0,
    val battlesPlayed: Int = 0,
    val highestMissionCleared: Int = 0,
    val attackLevel: Int = 1,
    val defenseLevel: Int = 1,
    val auraLevel: Int = 1,
    val transformationStage: Int = 0,
    val warriorXp: Int = 0,
    val lastUpdatedEpoch: Long = System.currentTimeMillis()
)

@Entity(tableName = "shakti_history_events")
data class ShaktiHistoryEntity(
    @PrimaryKey val eventId: String,
    val eventType: String,
    val title: String,
    val subtitle: String,
    val shaktiDelta: Int,
    val naamCountSnapshot: Int,
    val warriorId: String,
    val createdEpoch: Long = System.currentTimeMillis()
)

@Dao
interface NaamShaktiDao {
    @Query("SELECT * FROM naam_shakti_profile WHERE id = 1 LIMIT 1")
    fun observeProfile(): Flow<NaamShaktiProfileEntity?>

    @Query("SELECT * FROM naam_shakti_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileOnce(): NaamShaktiProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: NaamShaktiProfileEntity)

    @Query("SELECT * FROM shakti_history_events ORDER BY createdEpoch DESC LIMIT 120")
    fun observeHistory(): Flow<List<ShaktiHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(event: ShaktiHistoryEntity)
}
