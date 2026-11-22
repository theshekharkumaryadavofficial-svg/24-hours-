package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

import com.example.naamshakti.NaamShaktiDao
import com.example.naamshakti.NaamShaktiProfileEntity
import com.example.naamshakti.ShaktiHistoryEntity

@Database(
    entities = [
        SleepDayEntity::class,
        SleepGoalEntity::class,
        ActiveSleepSessionEntity::class,
        NaamJapDayEntity::class,
        NaamJapGoalEntity::class,
        DailyPlanBlockEntity::class,
        StudyModeConfigEntity::class,
        DailyDisciplineConfigEntity::class,
        NaamShaktiProfileEntity::class,
        ShaktiHistoryEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class SleepDatabase : RoomDatabase() {
    abstract fun sleepDao(): SleepDao
    abstract fun naamJapDao(): NaamJapDao
    abstract fun plansDao(): PlansDao
    abstract fun naamShaktiDao(): NaamShaktiDao

    companion object {
        @Volatile
        private var INSTANCE: SleepDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `naam_jap_days` (
                        `dateKey` TEXT NOT NULL,
                        `displayDate` TEXT NOT NULL,
                        `dayName` TEXT NOT NULL,
                        `count` INTEGER NOT NULL,
                        `goalAtDay` INTEGER NOT NULL,
                        `lastUpdatedEpoch` INTEGER NOT NULL,
                        PRIMARY KEY(`dateKey`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `naam_jap_goal` (
                        `id` INTEGER NOT NULL,
                        `dailyGoal` INTEGER NOT NULL,
                        `hapticEnabled` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `daily_plan_blocks` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `startHour` INTEGER NOT NULL,
                        `startMinute` INTEGER NOT NULL,
                        `endHour` INTEGER NOT NULL,
                        `endMinute` INTEGER NOT NULL,
                        `notes` TEXT NOT NULL,
                        `enablesStudyBlock` INTEGER NOT NULL,
                        `completedDateKey` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `study_mode_config` (
                        `id` INTEGER NOT NULL,
                        `startHour` INTEGER NOT NULL,
                        `startMinute` INTEGER NOT NULL,
                        `endHour` INTEGER NOT NULL,
                        `endMinute` INTEGER NOT NULL,
                        `autoStudyEnabled` INTEGER NOT NULL,
                        `blockSocialMedia` INTEGER NOT NULL,
                        `isActiveNow` INTEGER NOT NULL,
                        `blockedCountToday` INTEGER NOT NULL,
                        `blockedDateKey` TEXT NOT NULL,
                        `allowedStudyKeywordsCsv` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `daily_discipline_config` (
                        `id` INTEGER NOT NULL,
                        `waterGoalMl` INTEGER NOT NULL,
                        `waterConsumedMl` INTEGER NOT NULL,
                        `trackerDateKey` TEXT NOT NULL,
                        `lightMealsTarget` INTEGER NOT NULL,
                        `lightMealsLogged` INTEGER NOT NULL,
                        `naamJapReminderTarget` INTEGER NOT NULL,
                        `reminderEnabled` INTEGER NOT NULL,
                        `reminderIntervalMinutes` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `daily_plan_blocks` ADD COLUMN `strictAppLockEnabled` INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "ALTER TABLE `daily_plan_blocks` ADD COLUMN `allowedPackagesCsv` TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `naam_shakti_profile` (
                        `id` INTEGER NOT NULL,
                        `selectedWarriorId` TEXT NOT NULL,
                        `unlockedWarriorsCsv` TEXT NOT NULL,
                        `unlockedBattlefield` INTEGER NOT NULL,
                        `koRecoveryActive` INTEGER NOT NULL,
                        `currentShaktiEnergy` INTEGER NOT NULL,
                        `lifetimeShaktiEarned` INTEGER NOT NULL,
                        `lifetimeShaktiSpent` INTEGER NOT NULL,
                        `syncedAllTimeNaam` INTEGER NOT NULL,
                        `koBaselineAllTimeNaam` INTEGER NOT NULL,
                        `battlesWon` INTEGER NOT NULL,
                        `battlesPlayed` INTEGER NOT NULL,
                        `highestMissionCleared` INTEGER NOT NULL,
                        `attackLevel` INTEGER NOT NULL,
                        `defenseLevel` INTEGER NOT NULL,
                        `auraLevel` INTEGER NOT NULL,
                        `transformationStage` INTEGER NOT NULL,
                        `warriorXp` INTEGER NOT NULL,
                        `lastUpdatedEpoch` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `shakti_history_events` (
                        `eventId` TEXT NOT NULL,
                        `eventType` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `subtitle` TEXT NOT NULL,
                        `shaktiDelta` INTEGER NOT NULL,
                        `naamCountSnapshot` INTEGER NOT NULL,
                        `warriorId` TEXT NOT NULL,
                        `createdEpoch` INTEGER NOT NULL,
                        PRIMARY KEY(`eventId`)
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): SleepDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SleepDatabase::class.java,
                    "twentyfour_hrs_sleep.db"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
